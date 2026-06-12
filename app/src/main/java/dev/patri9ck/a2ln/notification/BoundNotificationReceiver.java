/*
 * Copyright (C) 2022 Patrick Zwick and contributors
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
package dev.patri9ck.a2ln.notification;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.function.Consumer;

public class BoundNotificationReceiver {

    private final Consumer<NotificationReceiver> notificationReceiverConsumer;
    private final Context context;

    private NotificationReceiver notificationReceiver;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationReceiver = ((NotificationReceiver.NotificationReceiverBinder) service).getNotificationReceiver();

            updateNotificationReceiver();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationReceiver = null;
        }
    };

    private boolean bound;

    public BoundNotificationReceiver(Consumer<NotificationReceiver> notificationReceiverConsumer, Context context) {
        this.notificationReceiverConsumer = notificationReceiverConsumer;
        this.context = context;
    }

    public void updateNotificationReceiver() {
        if (notificationReceiver == null) {
            return;
        }

        notificationReceiverConsumer.accept(notificationReceiver);
    }

    public void bind() {
        bound = context.bindService(new Intent(context, NotificationReceiver.class), serviceConnection, 0);
    }

    public void unbind() {
        if (!bound) {
            return;
        }

        context.unbindService(serviceConnection);

        bound = false;
    }
}
