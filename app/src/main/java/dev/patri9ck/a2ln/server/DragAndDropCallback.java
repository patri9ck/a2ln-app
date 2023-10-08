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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.patri9ck.a2ln.util.Storage;

public class DragAndDropCallback extends ItemTouchHelper.SimpleCallback {

    private final List<Server> servers;
    private final Storage storage;
    private final ServersAdapter serversAdapter;

    public DragAndDropCallback(List<Server> servers, Storage storage, ServersAdapter serversAdapter) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);

        this.servers = servers;
        this.storage = storage;
        this.serversAdapter = serversAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();

        Collections.swap(servers, from, to);

        serversAdapter.notifyItemMoved(from, to);

        storage.saveServers(servers);

        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Ignored
    }
}
