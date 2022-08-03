package dev.patri9ck.a2ln.device;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.DialogPairBinding;
import dev.patri9ck.a2ln.databinding.DialogPairedBinding;
import dev.patri9ck.a2ln.databinding.DialogPairingBinding;
import dev.patri9ck.a2ln.databinding.DialogPermissionRequestBinding;
import dev.patri9ck.a2ln.databinding.FragmentDevicesBinding;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;
import dev.patri9ck.a2ln.util.CameraScanner;
import dev.patri9ck.a2ln.util.JsonListConverter;
import dev.patri9ck.a2ln.util.Pairing;

public class DevicesFragment extends Fragment {

    private static final String TAG = "A2LN";

    private SharedPreferences sharedPreferences;

    private List<Device> devices;
    private DevicesAdapter devicesAdapter;

    private BoundNotificationReceiver boundNotificationReceiver;

    private CameraScanner cameraScanner;

    private FragmentDevicesBinding fragmentDevicesBinding;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        if (granted) {
            startCamera();
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentDevicesBinding = FragmentDevicesBinding.inflate(inflater, container, false);

        fragmentDevicesBinding.cancelButton.setOnClickListener(cancelButtonView -> stopCamera());

        fragmentDevicesBinding.pairButton.setOnClickListener(pairButtonView -> {
            DialogPairBinding dialogPairBinding = DialogPairBinding.inflate(inflater);

            AlertDialog pairDialog = new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.pair_dialog_title)
                    .setView(dialogPairBinding.getRoot())
                    .setPositiveButton(R.string.pair, (pairDialog2, which) -> {
                        pairDialog2.dismiss();

                        String deviceIp = dialogPairBinding.deviceIpEditText.getText().toString();

                        if (deviceIp.isEmpty()) {
                            return;
                        }

                        try {
                            startPairing(deviceIp, Integer.parseInt(dialogPairBinding.devicePortEditText.getText().toString()));
                        } catch (NumberFormatException ignored) {}
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();

            dialogPairBinding.qrCodeButton.setOnClickListener(qrCodeButtonView -> {
                pairDialog.dismiss();

                if (requestPermission()) {
                    startCamera();
                }
            });
        });

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        devices = JsonListConverter.fromJson(sharedPreferences.getString(getString(R.string.preferences_devices), null), Device.class);

        boundNotificationReceiver = new BoundNotificationReceiver(notificationReceiver -> notificationReceiver.setDevices(devices), requireContext());

        cameraScanner = new CameraScanner(this, fragmentDevicesBinding.previewView.getSurfaceProvider(), requireContext(), barcode -> {
            stopCamera();

            String address = barcode.getRawValue();

            if (address == null) {
                return;
            }

            String[] parts = address.split(":");

            if (parts.length != 2) {
                return;
            }

            try {
                startPairing(parts[0], Integer.parseInt(parts[1]));
            } catch (NumberFormatException ignored) {}
        });

        loadDevicesRecyclerView();

        return fragmentDevicesBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        boundNotificationReceiver.bind();
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

        new ItemTouchHelper(new SwipeToDeleteCallback(fragmentDevicesBinding.getRoot(), boundNotificationReceiver, devices, devicesAdapter)).attachToRecyclerView(fragmentDevicesBinding.devicesRecyclerView);
    }

    private void startCamera() {
        cameraScanner.startCamera();

        fragmentDevicesBinding.previewView.setVisibility(View.VISIBLE);
        fragmentDevicesBinding.cancelButton.setVisibility(View.VISIBLE);

        fragmentDevicesBinding.devicesRecyclerView.setVisibility(View.INVISIBLE);
        fragmentDevicesBinding.pairButton.setVisibility(View.INVISIBLE);
    }

    private void stopCamera() {
        cameraScanner.stopCamera();

        fragmentDevicesBinding.previewView.setVisibility(View.INVISIBLE);
        fragmentDevicesBinding.cancelButton.setVisibility(View.INVISIBLE);

        fragmentDevicesBinding.devicesRecyclerView.setVisibility(View.VISIBLE);
        fragmentDevicesBinding.pairButton.setVisibility(View.VISIBLE);
    }

    private boolean requestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            DialogPermissionRequestBinding dialogPermissionRequestBinding = DialogPermissionRequestBinding.inflate(getLayoutInflater());

            dialogPermissionRequestBinding.permissionRequestTextView.setText(R.string.permission_request_dialog_information_camera);

            new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.permission_request_dialog_title)
                    .setView(dialogPermissionRequestBinding.getRoot())
                    .setPositiveButton(R.string.grant, (requestPermissionDialog, which) -> launchRequestPermissionLauncher())
                    .setNegativeButton(R.string.deny, null)
                    .show();

            return false;
        }

        launchRequestPermissionLauncher();

        return false;
    }

    private void launchRequestPermissionLauncher() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void startPairing(String deviceIp, int devicePort) {
        for (Device device : devices) {
            if (device.getIp().equals(deviceIp)) {
                Snackbar.make(fragmentDevicesBinding.getRoot(), R.string.already_paired, Snackbar.LENGTH_SHORT).show();

                return;
            }
        }

        String clientIp = getIp();
        String clientPublicKey = sharedPreferences.getString(getString(R.string.preferences_client_public_key), null);

        if (clientIp == null || clientPublicKey == null) {
            return;
        }

        DialogPairingBinding dialogPairingBinding = DialogPairingBinding.inflate(getLayoutInflater());

        dialogPairingBinding.clientIpTextView.setText(getString(R.string.ip, clientIp));
        dialogPairingBinding.clientPublicKeyTextView.setText(getString(R.string.public_key, clientPublicKey));

        AlertDialog pairingDialog = new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                .setTitle(R.string.pairing_dialog_title)
                .setView(dialogPairingBinding.getRoot())
                .setCancelable(false)
                .show();

        new Pairing(deviceIp, devicePort, clientIp, clientPublicKey).pair().thenAccept(device -> requireActivity().runOnUiThread(() -> {
            pairingDialog.dismiss();

            if (device == null) {
                Snackbar.make(fragmentDevicesBinding.getRoot(), R.string.pairing_failed, Snackbar.LENGTH_SHORT).show();

                return;
            }

            DialogPairedBinding dialogPairedBinding = DialogPairedBinding.inflate(getLayoutInflater());

            dialogPairedBinding.deviceIpTextView.setText(getString(R.string.ip, deviceIp));
            dialogPairedBinding.devicePublicKeyTextView.setText(getString(R.string.public_key, new String(device.getPublicKey(), StandardCharsets.UTF_8)));

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
