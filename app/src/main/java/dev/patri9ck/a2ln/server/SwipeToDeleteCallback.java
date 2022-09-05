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

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final View rootView;
    private final BoundNotificationReceiver boundNotificationReceiver;
    private final List<Server> servers;
    private final ServersAdapter serversAdapter;

    public SwipeToDeleteCallback(View rootView, BoundNotificationReceiver boundNotificationReceiver, List<Server> servers, ServersAdapter serversAdapter) {
        super(0, ItemTouchHelper.LEFT);

        this.rootView = rootView;
        this.boundNotificationReceiver = boundNotificationReceiver;
        this.servers = servers;
        this.serversAdapter = serversAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        Server server = servers.remove(position);

        serversAdapter.notifyItemRemoved(position);

        boundNotificationReceiver.updateNotificationReceiver();

        Snackbar.make(rootView, R.string.removed_server, Snackbar.LENGTH_LONG)
                .setAction(R.string.removed_server_undo, buttonView -> {
                    servers.add(position, server);
                    serversAdapter.notifyItemInserted(position);

                    boundNotificationReceiver.updateNotificationReceiver();
                }).show();
    }
}
