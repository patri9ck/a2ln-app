package dev.patri9ck.a2ln.notification;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BoundNotificationReceiver {

    private NotificationReceiver notificationReceiver;

    private boolean bound;

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

    private final NotificationReceiverUpdater notificationReceiverUpdater;
    private final Context context;

    public BoundNotificationReceiver(NotificationReceiverUpdater notificationReceiverUpdater, Context context) {
        this.notificationReceiverUpdater = notificationReceiverUpdater;
        this.context = context;
    }

    public void updateNotificationReceiver() {
        if (notificationReceiver == null) {
            return;
        }

        notificationReceiverUpdater.update(notificationReceiver);
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
