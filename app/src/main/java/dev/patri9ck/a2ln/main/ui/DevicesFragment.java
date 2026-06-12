package dev.patri9ck.a2ln.main.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.DialogPairBinding;
import dev.patri9ck.a2ln.databinding.DialogPairedBinding;
import dev.patri9ck.a2ln.databinding.DialogPairingBinding;
import dev.patri9ck.a2ln.databinding.FragmentDevicesBinding;
import dev.patri9ck.a2ln.device.Device;
import dev.patri9ck.a2ln.device.DevicesAdapter;
import dev.patri9ck.a2ln.device.SwipeToDeleteCallback;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;
import dev.patri9ck.a2ln.pair.Pairing;
import dev.patri9ck.a2ln.util.JsonListConverter;

public class DevicesFragment extends Fragment {

    private static final String TAG = "A2LN";

    private List<Device> devices;
    private DevicesAdapter devicesAdapter;

    private SharedPreferences sharedPreferences;

    private BoundNotificationReceiver boundNotificationReceiver;

    private FragmentDevicesBinding fragmentDevicesBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentDevicesBinding = FragmentDevicesBinding.inflate(inflater, container, false);

        fragmentDevicesBinding.pairButton.setOnClickListener(view -> {
            DialogPairBinding dialogPairBinding = DialogPairBinding.inflate(inflater);

            new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.pair_dialog_title)
                    .setView(dialogPairBinding.getRoot())
                    .setPositiveButton(R.string.pair, (pairDialog, which) -> {
                        pairDialog.dismiss();

                        startPairing(dialogPairBinding);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        return fragmentDevicesBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        devices = JsonListConverter.fromJson(sharedPreferences.getString(getString(R.string.preferences_devices), null), Device.class);

        boundNotificationReceiver = new BoundNotificationReceiver(notificationReceiver -> notificationReceiver.setDevices(devices), requireContext());

        boundNotificationReceiver.bind();

        loadDevicesRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();

        sharedPreferences.edit().putString(getString(R.string.preferences_devices), JsonListConverter.toJson(devices)).apply();

        boundNotificationReceiver.unbind();
    }

    private void loadDevicesRecyclerView() {
        devicesAdapter = new DevicesAdapter(this, boundNotificationReceiver, devices);

        fragmentDevicesBinding.devicesRecyclerView.setAdapter(devicesAdapter);
        fragmentDevicesBinding.devicesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        new ItemTouchHelper(new SwipeToDeleteCallback(this, boundNotificationReceiver, devices, devicesAdapter)).attachToRecyclerView(fragmentDevicesBinding.devicesRecyclerView);
    }

    private void startPairing(DialogPairBinding dialogPairBinding) {
        String deviceIp = dialogPairBinding.deviceIpEditText.getText().toString();

        if (deviceIp.isEmpty()) {
            return;
        }

        int devicePort;

        try {
            devicePort = Integer.parseInt(dialogPairBinding.devicePortEditText.getText().toString());
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

        DialogPairingBinding dialogPairingBinding = DialogPairingBinding.inflate(getLayoutInflater());

        dialogPairingBinding.clientIpTextView.setText(getString(R.string.client_ip, clientIp));
        dialogPairingBinding.clientPublicKeyTextView.setText(getString(R.string.client_public_key, clientPublicKey));

        AlertDialog pairingDialog = new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                .setTitle(R.string.pairing_dialog_title)
                .setView(dialogPairingBinding.getRoot())
                .setCancelable(false)
                .show();

        CompletableFuture.supplyAsync(() -> new Pairing(deviceIp, devicePort, clientIp, clientPublicKey).pair()).thenAccept(device -> requireActivity().runOnUiThread(() -> {
            pairingDialog.dismiss();

            if (device == null) {
                Toast.makeText(requireContext(), R.string.pairing_failed, Toast.LENGTH_LONG).show();

                return;
            }

            DialogPairedBinding dialogPairedBinding = DialogPairedBinding.inflate(getLayoutInflater());

            dialogPairedBinding.deviceIpTextView.setText(getString(R.string.device_ip, deviceIp));
            dialogPairedBinding.devicePublicKeyTextView.setText(getString(R.string.device_public_key, new String(device.getPublicKey(), StandardCharsets.UTF_8)));

            new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.paired_dialog_title)
                    .setView(dialogPairedBinding.getRoot())
                    .setPositiveButton(R.string.pair, (pairedDialog, which) -> {
                        int position = devices.size();

                        devices.add(device);

                        devicesAdapter.notifyItemInserted(position);

                        boundNotificationReceiver.updateNotificationReceiver();
                    })
                    .setNegativeButton(R.string.cancel, null)
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
