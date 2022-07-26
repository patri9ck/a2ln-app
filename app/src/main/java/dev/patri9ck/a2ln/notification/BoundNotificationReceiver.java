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
