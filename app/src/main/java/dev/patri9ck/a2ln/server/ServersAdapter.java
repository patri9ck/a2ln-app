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

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.DialogEditServerBinding;
import dev.patri9ck.a2ln.databinding.ItemServerBinding;
import dev.patri9ck.a2ln.util.Storage;

public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ServerViewHolder> {

    private final ServersFragment serversFragment;
    private final Storage storage;
    private final List<Server> servers;

    public ServersAdapter(ServersFragment serversFragment, Storage storage, List<Server> servers) {
        this.serversFragment = serversFragment;
        this.storage = storage;
        this.servers = servers;
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ServerViewHolder(ItemServerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ServerViewHolder holder, int position) {
        Server server = servers.get(position);

        holder.addressTextView.setText(server.getAlias().orElse(server.getAddress()));

        holder.addressTextView.setOnClickListener(view -> {
            DialogEditServerBinding dialogEditServerBinding = DialogEditServerBinding.inflate(serversFragment.getLayoutInflater());

            dialogEditServerBinding.editServerIpEditText.setText(server.getIp());
            dialogEditServerBinding.editServerPortEditText.setText(Integer.toString(server.getPort()));

            server.getAlias().ifPresent(dialogEditServerBinding.editServerAliasEditText::setText);

            new MaterialAlertDialogBuilder(serversFragment.requireContext(), R.style.Dialog)
                    .setTitle(R.string.edit_server_dialog_title)
                    .setView(dialogEditServerBinding.getRoot())
                    .setPositiveButton(R.string.apply, (editPortDialog, which) -> {
                        String ip = dialogEditServerBinding.editServerIpEditText.getText().toString();

                        serversFragment.validate(ip, dialogEditServerBinding.editServerPortEditText.getText().toString(), !ip.equals(server.getIp())).ifPresent(destination -> {
                            String alias = dialogEditServerBinding.editServerAliasEditText.getText().toString();

                            server.setAlias(alias.trim().isEmpty() ? null : alias);
                            server.setIp(destination.getIp());
                            server.setPort(destination.getPort());

                            notifyItemChanged(position);

                            storage.saveServers(servers);
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        holder.serverCheckBox.setOnCheckedChangeListener((serverCheckBoxView, isChecked) -> {
            server.setEnabled(isChecked);

            storage.saveServers(servers);
        });

        holder.serverCheckBox.setChecked(server.isEnabled());
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    protected static class ServerViewHolder extends RecyclerView.ViewHolder {

        private final TextView addressTextView;
        private final CheckBox serverCheckBox;

        public ServerViewHolder(ItemServerBinding itemServerBinding) {
            super(itemServerBinding.getRoot());

            addressTextView = itemServerBinding.addressTextView;
            serverCheckBox = itemServerBinding.serverCheckBox;
        }
    }
}
