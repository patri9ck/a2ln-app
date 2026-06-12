/*
 * Copyright (C) 2022  Patrick Zwick and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.patri9ck.a2ln.server;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.DialogPairBinding;
import dev.patri9ck.a2ln.databinding.DialogPairedBinding;
import dev.patri9ck.a2ln.databinding.DialogPairingBinding;
import dev.patri9ck.a2ln.databinding.FragmentServersBinding;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;
import dev.patri9ck.a2ln.log.LogsDialogBuilder;
import dev.patri9ck.a2ln.pairing.Pairing;
import dev.patri9ck.a2ln.util.Util;

public class ServersFragment extends Fragment {

    private static final String TAG = "A2LN";

    private SharedPreferences sharedPreferences;

    private List<Server> servers;
    private ServersAdapter serversAdapter;

    private BoundNotificationReceiver boundNotificationReceiver;

    private FragmentServersBinding fragmentServersBinding;

    private final ActivityResultLauncher<ScanOptions> qrCodeScannerLauncher = registerForActivityResult(new ScanContract(), result -> {
        String address = result.getContents();

        if (address == null) {
            return;
        }

        String[] parts = address.split(":");

        if (parts.length != 2) {
            Snackbar.make(fragmentServersBinding.getRoot(), getString(R.string.invalid_qr_code), Snackbar.LENGTH_SHORT).show();

            return;
        }

        String serverIp = parts[0];

        if (validateAlreadyPaired(serverIp)) {
            return;
        }

        Optional<Integer> pairingPort = validateIpAndPort(serverIp, parts[1]);

        if (!pairingPort.isPresent()) {
            return;
        }

        startPairing(serverIp, pairingPort.get());
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        servers = Util.fromJson(sharedPreferences.getString(getString(R.string.preferences_servers), null), Server.class);

        boundNotificationReceiver = new BoundNotificationReceiver(notificationReceiver -> notificationReceiver.setServers(servers), requireContext());

        fragmentServersBinding = FragmentServersBinding.inflate(inflater, container, false);

        fragmentServersBinding.pairButton.setOnClickListener(pairButtonView -> {
            DialogPairBinding dialogPairBinding = DialogPairBinding.inflate(inflater);

            AlertDialog.Builder pairDialogBuilder = new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.pair_dialog_title)
                    .setView(dialogPairBinding.getRoot())
                    .setPositiveButton(R.string.pair, (pairDialog, which) -> {
                        String serverIp = dialogPairBinding.serverIpEditText.getText().toString();

                        if (validateAlreadyPaired(serverIp)) {
                            return;
                        }

                        Optional<Integer> pairingPort = validateIpAndPort(serverIp, dialogPairBinding.pairingPortEditText.getText().toString());

                        if (!pairingPort.isPresent()) {
                            return;
                        }

                        pairDialog.dismiss();

                        startPairing(serverIp, pairingPort.get());
                    })
                    .setNegativeButton(R.string.cancel, null);

            AlertDialog pairDialog = pairDialogBuilder.show();

            dialogPairBinding.qrCodeButton.setOnClickListener(qrCodeButtonView -> {
                pairDialog.dismiss();

                qrCodeScannerLauncher.launch(new ScanOptions()
                        .addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN)
                        .setOrientationLocked(false)
                        .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                        .setBeepEnabled(false)
                        .setPrompt(""));
            });
        });

        loadServersRecyclerView();

        return fragmentServersBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        boundNotificationReceiver.bind();
    }

    @Override
    public void onStop() {
        super.onStop();

        sharedPreferences.edit().putString(getString(R.string.preferences_servers), Util.toJson(servers)).apply();

        boundNotificationReceiver.unbind();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        fragmentServersBinding = null;
    }

    protected Optional<Integer> validateIpAndPort(String ip, String port) {
        if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
            Snackbar.make(fragmentServersBinding.getRoot(), getString(R.string.invalid_ip), Snackbar.LENGTH_SHORT).show();

            return Optional.empty();
        }

        Optional<Integer> parsedPort = Util.parsePort(port);

        if (!parsedPort.isPresent()) {
            Snackbar.make(fragmentServersBinding.getRoot(), getString(R.string.invalid_port), Snackbar.LENGTH_SHORT).show();
        }

        return parsedPort;
    }

    protected boolean validateAlreadyPaired(String ip) {
        for (Server server : servers) {
            if (server.getIp().equals(ip)) {
                Snackbar.make(fragmentServersBinding.getRoot(), R.string.already_paired, Snackbar.LENGTH_SHORT).show();

                return true;
            }
        }

        return false;
    }

    private void loadServersRecyclerView() {
        serversAdapter = new ServersAdapter(this, boundNotificationReceiver, servers);

        fragmentServersBinding.serversRecyclerView.setAdapter(serversAdapter);
        fragmentServersBinding.serversRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        new ItemTouchHelper(new SwipeToDeleteCallback(fragmentServersBinding.getRoot(), boundNotificationReceiver, servers, serversAdapter)).attachToRecyclerView(fragmentServersBinding.serversRecyclerView);
    }

    private void startPairing(String serverIp, int pairingPort) {
        String ownIp = getIp();
        String ownPublicKey = sharedPreferences.getString(getString(R.string.preferences_own_public_key), null);

        if (ownIp == null || ownPublicKey == null) {
            return;
        }

        DialogPairingBinding dialogPairingBinding = DialogPairingBinding.inflate(getLayoutInflater());

        dialogPairingBinding.ownIpTextView.setText(ownIp);
        dialogPairingBinding.ownPublicKeyTextView.setText(ownPublicKey);

        AlertDialog pairingDialog = new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                .setTitle(R.string.pairing_dialog_title)
                .setView(dialogPairingBinding.getRoot())
                .setCancelable(false)
                .show();

        CompletableFuture.supplyAsync(() -> new Pairing(serverIp, pairingPort, ownIp, ownPublicKey).pair()).thenAccept(pairingResult -> requireActivity().runOnUiThread(() -> {
            pairingDialog.dismiss();

            Server server = pairingResult.getServer().orElse(null);

            if (server == null) {
                Snackbar.make(fragmentServersBinding.getRoot(), R.string.pairing_failed, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.view_logs, view -> new LogsDialogBuilder(requireContext(), pairingResult.getKeptLog(), getLayoutInflater()).show())
                        .show();

                return;
            }

            DialogPairedBinding dialogPairedBinding = DialogPairedBinding.inflate(getLayoutInflater());

            dialogPairedBinding.serverIpTextView.setText(serverIp);
            dialogPairedBinding.serverPublicKeyTextView.setText(new String(server.getPublicKey(), StandardCharsets.UTF_8));

            new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.paired_dialog_title)
                    .setView(dialogPairedBinding.getRoot())
                    .setPositiveButton(R.string.pair, (pairedDialog, which) -> {
                        int position = servers.size();

                        servers.add(server);
                        serversAdapter.notifyItemInserted(position);

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
