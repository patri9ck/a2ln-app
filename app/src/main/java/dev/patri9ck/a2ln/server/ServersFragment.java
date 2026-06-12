/*
 * Android 2 Linux Notifications - A way to display Android phone notifications on Linux
 * Copyright (C) 2023  patri9ck and contributors
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
import dev.patri9ck.a2ln.log.LogDialogBuilder;
import dev.patri9ck.a2ln.pairing.Pairing;
import dev.patri9ck.a2ln.util.Storage;
import dev.patri9ck.a2ln.util.Util;

public class ServersFragment extends Fragment {

    private static final String TAG = "A2LN";

    private Storage storage;
    private List<Server> servers;
    private FragmentServersBinding fragmentServersBinding;

    private ServersAdapter serversAdapter;

    private final ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result -> {
        String address = result.getContents();

        if (address == null) {
            return;
        }

        String[] parts = address.split(":");

        if (parts.length != 2) {
            Snackbar.make(fragmentServersBinding.getRoot(), getString(R.string.invalid_qr_code), Snackbar.LENGTH_SHORT).show();

            return;
        }

        validate(parts[0], parts[1], true).ifPresent(this::startPairing);
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        storage = new Storage(requireContext(), requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE));
        servers = storage.loadServers();
        fragmentServersBinding = FragmentServersBinding.inflate(inflater, container, false);

        fragmentServersBinding.pairButton.setOnClickListener(pairButtonView -> {
            DialogPairBinding dialogPairBinding = DialogPairBinding.inflate(inflater);

            AlertDialog.Builder pairDialogBuilder = new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.pair_dialog_title)
                    .setView(dialogPairBinding.getRoot())
                    .setPositiveButton(R.string.pair, (pairDialog, which) -> validate(dialogPairBinding.serverIpEditText.getText().toString(), dialogPairBinding.pairingPortEditText.getText().toString(), true).ifPresent(destination -> {
                        pairDialog.dismiss();

                        startPairing(destination);
                    }))
                    .setNegativeButton(R.string.cancel, null);

            AlertDialog pairDialog = pairDialogBuilder.show();

            dialogPairBinding.qrCodeButton.setOnClickListener(qrCodeButtonView -> {
                pairDialog.dismiss();

                launcher.launch(new ScanOptions()
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
    public void onDestroyView() {
        super.onDestroyView();

        fragmentServersBinding = null;
    }

    protected Optional<Destination> validate(String ip, String rawPort, boolean paired) {
        if (paired) {
            for (Server server : servers) {
                if (server.getIp().equals(ip)) {
                    Snackbar.make(fragmentServersBinding.getRoot(), getString(R.string.already_paired), Snackbar.LENGTH_SHORT).show();

                    return Optional.empty();
                }
            }
        }

        if (!Patterns.IP_ADDRESS.matcher(ip).matches() && !Patterns.DOMAIN_NAME.matcher(ip).matches()) {
            Snackbar.make(fragmentServersBinding.getRoot(), getString(R.string.invalid_ip), Snackbar.LENGTH_SHORT).show();

            return Optional.empty();
        }

        return validatePort(rawPort).map(port -> new Destination(ip, port));
    }

    private Optional<Integer> validatePort(String rawPort) {
        Optional<Integer> optionalPort = Util.parsePort(rawPort);

        if (!optionalPort.isPresent()) {
            Snackbar.make(fragmentServersBinding.getRoot(), getString(R.string.invalid_port), Snackbar.LENGTH_SHORT).show();
        }

        return optionalPort;
    }

    private void loadServersRecyclerView() {
        serversAdapter = new ServersAdapter(this, storage, servers);

        fragmentServersBinding.serversRecyclerView.setAdapter(serversAdapter);
        fragmentServersBinding.serversRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        new ItemTouchHelper(new SwipeToDeleteCallback(fragmentServersBinding.getRoot(), storage, servers, serversAdapter)).attachToRecyclerView(fragmentServersBinding.serversRecyclerView);
        new ItemTouchHelper(new DragAndDropCallback(servers, storage, serversAdapter)).attachToRecyclerView(fragmentServersBinding.serversRecyclerView);
    }

    private void startPairing(Destination destination) {
        String ip = getIp();

        if (ip == null) {
            return;
        }

        storage.loadRawPublicKey().ifPresent(rawPublicKey -> {
            DialogPairingBinding dialogPairingBinding = DialogPairingBinding.inflate(getLayoutInflater());

            dialogPairingBinding.ownIpTextView.setText(ip);
            dialogPairingBinding.ownPublicKeyTextView.setText(rawPublicKey);

            AlertDialog pairingDialog = new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.pairing_dialog_title)
                    .setView(dialogPairingBinding.getRoot())
                    .setCancelable(false)
                    .show();

            CompletableFuture.supplyAsync(() -> new Pairing(requireContext(), destination, ip, rawPublicKey).pair()).thenAccept(pairingResult -> requireActivity().runOnUiThread(() -> {
                pairingDialog.dismiss();

                Optional<byte[]> optionalPublicKey = pairingResult.getPublicKey();

                if (!optionalPublicKey.isPresent()) {
                    Snackbar.make(fragmentServersBinding.getRoot(), R.string.pairing_failed, Snackbar.LENGTH_LONG)
                            .setAction(R.string.view_log, view -> {
                                if (isVisible()) {
                                    new LogDialogBuilder(pairingResult.getKeptLog(), getLayoutInflater()).show();
                                }
                            })
                            .show();

                    return;
                }

                byte[] publicKey = optionalPublicKey.get();

                DialogPairedBinding dialogPairedBinding = DialogPairedBinding.inflate(getLayoutInflater());

                dialogPairedBinding.serverIpTextView.setText(destination.getIp());
                dialogPairedBinding.serverPublicKeyTextView.setText(new String(publicKey, StandardCharsets.UTF_8));

                new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                        .setTitle(R.string.paired_dialog_title)
                        .setView(dialogPairedBinding.getRoot())
                        .setPositiveButton(R.string.pair, (pairedDialog, which) -> {
                            Server server = new Server(destination.getIp(), Storage.DEFAULT_PORT, publicKey);

                            int position = servers.size();

                            servers.add(server);
                            serversAdapter.notifyItemInserted(position);

                            storage.saveServers(servers);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }));
        });
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
