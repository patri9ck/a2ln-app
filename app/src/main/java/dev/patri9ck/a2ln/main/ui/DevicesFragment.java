package dev.patri9ck.a2ln.main.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.FragmentDevicesBinding;
import dev.patri9ck.a2ln.device.Device;
import dev.patri9ck.a2ln.device.DevicesAdapter;
import dev.patri9ck.a2ln.device.SwipeToDeleteCallback;
import dev.patri9ck.a2ln.notification.NotificationReceiver;

public class DevicesFragment extends Fragment {

    private static final String TAG = "A2LN";

    private static final int TIMEOUT_SECONDS = 30;

    private List<Device> devices;
    private DevicesAdapter devicesAdapter;

    private SharedPreferences sharedPreferences;

    private NotificationReceiver notificationReceiver;
    private boolean bound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationReceiver = ((NotificationReceiver.NotificationReceiverBinder) service).getNotificationReceiver();

            notificationReceiver.setDevices(devices);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationReceiver = null;
        }
    };

    private FragmentDevicesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);

        binding.pairButton.setOnClickListener(view -> {
            View pairDialogView = getLayoutInflater().inflate(R.layout.pair_dialog, null);

            new AlertDialog.Builder(requireContext(), R.style.Dialog)
                    .setView(pairDialogView)
                    .setPositiveButton(R.string.pair, (dialog, which) -> {
                        dialog.dismiss();

                        startPairing(pairDialogView);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        devices = Device.fromJson(sharedPreferences.getString(getString(R.string.preferences_devices), null));

        loadAddressesRecyclerView();

        bound = requireContext().bindService(new Intent(requireContext(), NotificationReceiver.class), serviceConnection, 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        sharedPreferences.edit().putString(getString(R.string.preferences_devices), Device.toJson(devices)).apply();

        if (bound) {
            requireContext().unbindService(serviceConnection);

            bound = false;
        }
    }

    private void startPairing(View view) {
        String serverIp = ((EditText) view.findViewById(R.id.server_ip_edit_text)).getText().toString();

        if (serverIp.isEmpty()) {
            return;
        }

        int serverPort;

        try {
            serverPort = Integer.parseInt(((EditText) view.findViewById(R.id.server_port_edit_text)).getText().toString());
        } catch (NumberFormatException exception) {
            return;
        }

        for (Device device : devices) {
            if (device.getServerIp().equals(serverIp)) {
                Toast.makeText(requireContext(), R.string.already_paired, Toast.LENGTH_LONG).show();

                return;
            }
        }

        String clientIp = getClientIp();
        String clientPublicKey = sharedPreferences.getString(getString(R.string.preferences_client_public_key), null);

        if (clientIp == null || clientPublicKey == null) {
            return;
        }

        View pairingDialogView = getLayoutInflater().inflate(R.layout.pairing_dialog, null);

        ((TextView) pairingDialogView.findViewById(R.id.client_ip_text_view)).setText(getString(R.string.client_ip, clientIp));
        ((TextView) pairingDialogView.findViewById(R.id.client_public_key_text_view)).setText(getString(R.string.client_public_key, clientPublicKey));

        AlertDialog pairingDialog = new AlertDialog.Builder(requireContext(), R.style.Dialog)
                .setView(pairingDialogView)
                .show();

        pairingDialog.setCanceledOnTouchOutside(false);

        CompletableFuture.supplyAsync(() -> pairDevice(serverIp, serverPort, clientIp, clientPublicKey)).thenAccept(device -> requireActivity().runOnUiThread(() -> {
            pairingDialog.dismiss();

            if (device == null) {
                Toast.makeText(requireContext(), R.string.pairing_failed, Toast.LENGTH_LONG).show();

                return;
            }

            View pairedDialogView = getLayoutInflater().inflate(R.layout.paired_dialog, null);

            ((TextView) pairedDialogView.findViewById(R.id.server_ip_text_view)).setText(getString(R.string.server_ip, serverIp));
            ((TextView) pairedDialogView.findViewById(R.id.server_public_key_text_view)).setText(getString(R.string.server_public_key, device.getServerPublicKey()));

            new AlertDialog.Builder(requireContext(), R.style.Dialog)
                    .setView(pairedDialogView)
                    .setPositiveButton(R.string.pair, (dialog, which) -> {
                        devices.add(device);

                        devicesAdapter.notifyItemInserted(devices.size());

                        if (notificationReceiver == null) {
                            return;
                        }

                        notificationReceiver.setDevices(devices);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                    .show();
        }));
    }

    private Device pairDevice(String serverIp, int serverPort, String clientIp, String clientPublicKey) {
        try (ZContext zContext = new ZContext(); ZMQ.Socket client = zContext.createSocket(SocketType.REQ)) {
            client.setSendTimeOut(TIMEOUT_SECONDS * 1000);
            client.setReceiveTimeOut(TIMEOUT_SECONDS * 1000);

            client.setImmediate(false);

            if (!client.connect("tcp://" + serverIp + ":" + serverPort)) {
                return null;
            }

            ZMsg zMsg = new ZMsg();

            zMsg.add(clientIp);
            zMsg.add(clientPublicKey);

            if (!zMsg.send(client)) {
                return null;
            }

            zMsg = ZMsg.recvMsg(client);

            if (zMsg == null || zMsg.size() != 2) {
                return null;
            }

            try {
                return new Device(serverIp, Integer.parseInt(zMsg.pop().getString(StandardCharsets.UTF_8)), zMsg.pop().getString(StandardCharsets.UTF_8));
            } catch (NumberFormatException exception) {
                return null;
            }
        }
    }

    private String getClientIp() {
        try {
            return InetAddress.getByAddress(ByteBuffer
                    .allocate(Integer.BYTES)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(((WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress())
                    .array()).getHostAddress();
        } catch (UnknownHostException exception) {
            Log.e(TAG, "Failed to get client IP", exception);

            return null;
        }
    }

    private void loadAddressesRecyclerView() {
        devicesAdapter = new DevicesAdapter(devices);

        binding.devicesRecyclerView.setAdapter(devicesAdapter);
        binding.devicesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        new ItemTouchHelper(new SwipeToDeleteCallback(this, devices, devicesAdapter)).attachToRecyclerView(binding.devicesRecyclerView);
    }
}
