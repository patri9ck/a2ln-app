package dev.patri9ck.a2ln.main.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;
import dev.patri9ck.a2ln.notification.NotificationReceiver;
import dev.patri9ck.a2ln.notification.NotificationReceiverUpdater;
import dev.patri9ck.a2ln.pair.Pairing;
import dev.patri9ck.a2ln.util.JsonListConverter;

public class DevicesFragment extends Fragment implements NotificationReceiverUpdater {

    private static final String TAG = "A2LN";

    private List<Device> devices;
    private DevicesAdapter devicesAdapter;

    private SharedPreferences sharedPreferences;

    private BoundNotificationReceiver boundNotificationReceiver;

    private FragmentDevicesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);

        binding.pairButton.setOnClickListener(view -> {
            View pairDialogView = getLayoutInflater().inflate(R.layout.dialog_pair, null);

            new AlertDialog.Builder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.pair_dialog_title)
                    .setView(pairDialogView)
                    .setPositiveButton(R.string.pair, (pairDialog, which) -> {
                        pairDialog.dismiss();

                        startPairing(pairDialogView);
                    })
                    .setNegativeButton(R.string.cancel, (pairDialog, which) -> pairDialog.dismiss())
                    .show();
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        devices = JsonListConverter.fromJson(sharedPreferences.getString(getString(R.string.preferences_devices), null), Device.class);

        boundNotificationReceiver = new BoundNotificationReceiver(this, requireContext());

        boundNotificationReceiver.bind();

        loadDevicesRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();

        sharedPreferences.edit().putString(getString(R.string.preferences_devices), JsonListConverter.toJson(devices)).apply();

        boundNotificationReceiver.unbind();
    }

    @Override
    public void update(NotificationReceiver notificationReceiver) {
        notificationReceiver.setDevices(devices);
    }

    private void loadDevicesRecyclerView() {
        devicesAdapter = new DevicesAdapter(this, boundNotificationReceiver, devices);

        binding.devicesRecyclerView.setAdapter(devicesAdapter);
        binding.devicesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        new ItemTouchHelper(new SwipeToDeleteCallback(this, boundNotificationReceiver, devices, devicesAdapter)).attachToRecyclerView(binding.devicesRecyclerView);
    }

    private void startPairing(View pairDialogView) {
        String deviceIp = ((EditText) pairDialogView.findViewById(R.id.device_ip_edit_text)).getText().toString();

        if (deviceIp.isEmpty()) {
            return;
        }

        int devicePort;

        try {
            devicePort = Integer.parseInt(((EditText) pairDialogView.findViewById(R.id.device_port_edit_text)).getText().toString());
        } catch (NumberFormatException exception) {
            return;
        }

        for (Device device : devices) {
            if (device.getIp().equals(deviceIp)) {
                Toast.makeText(requireContext(), R.string.already_paired, Toast.LENGTH_LONG).show();

                return;
            }
        }

        String clientIp = getIp();
        String clientPublicKey = sharedPreferences.getString(getString(R.string.preferences_client_public_key), null);

        if (clientIp == null || clientPublicKey == null) {
            return;
        }

        View pairingDialogView = getLayoutInflater().inflate(R.layout.dialog_pairing, null);

        ((TextView) pairingDialogView.findViewById(R.id.client_ip_text_view)).setText(getString(R.string.client_ip, clientIp));
        ((TextView) pairingDialogView.findViewById(R.id.client_public_key_text_view)).setText(getString(R.string.client_public_key, clientPublicKey));

        AlertDialog pairingDialog = new AlertDialog.Builder(requireContext(), R.style.Dialog)
                .setTitle(R.string.pairing_dialog_title)
                .setCancelable(false)
                .setView(pairingDialogView)
                .show();

        CompletableFuture.supplyAsync(() -> new Pairing(deviceIp, devicePort, clientIp, clientPublicKey).pair()).thenAccept(device -> requireActivity().runOnUiThread(() -> {
            pairingDialog.dismiss();

            if (device == null) {
                Toast.makeText(requireContext(), R.string.pairing_failed, Toast.LENGTH_LONG).show();

                return;
            }

            View pairedDialogView = getLayoutInflater().inflate(R.layout.dialog_paired, null);

            ((TextView) pairedDialogView.findViewById(R.id.device_ip_text_view)).setText(getString(R.string.device_ip, deviceIp));
            ((TextView) pairedDialogView.findViewById(R.id.device_public_key_text_view)).setText(getString(R.string.device_public_key, new String(device.getPublicKey(), StandardCharsets.UTF_8)));

            new AlertDialog.Builder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.paired_dialog_title)
                    .setView(pairedDialogView)
                    .setPositiveButton(R.string.pair, (dialog, which) -> {
                        int position = devices.size();

                        devices.add(device);

                        devicesAdapter.notifyItemInserted(position);

                        boundNotificationReceiver.updateNotificationReceiver();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        }));
    }

    private String getIp() {
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
}
