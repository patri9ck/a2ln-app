package dev.patri9ck.a2ln.notification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;

import dev.patri9ck.a2ln.R;

public class NotificationReceiver extends NotificationListenerService {

    private static final String TAG = "A2LNNR";

    private NotificationReceiverBinder notificationReceiverBinder = new NotificationReceiverBinder();

    private NotificationSpamHandler notificationSpamHandler = new NotificationSpamHandler();

    private boolean initialized;

    private NotificationSender notificationSender;
    private List<String> disabledApps;

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Log.v(TAG, "Notification posted");

        notificationSpamHandler.cleanUp();

        String packageName = statusBarNotification.getPackageName();

        ParsedNotification parsedNotification = ParsedNotification.parseNotification(statusBarNotification.getNotification(), this);

        if (getPackageManager().getLaunchIntentForPackage(packageName) == null || disabledApps.contains(packageName) || parsedNotification == null || notificationSpamHandler.isSpammed(parsedNotification)) {
            Log.v(TAG, "Notification will not be sent (not an actual app/disabled/incomplete/spam)");

            return;
        }

        notificationSpamHandler.addParsedNotification(parsedNotification);

        Executors.newSingleThreadExecutor().execute(() -> notificationSender.sendParsedNotification(parsedNotification));

        Log.v(TAG, "Notification given to NotificationSender");
    }

    @Override
    public void onListenerConnected() {
        Log.v(TAG, "NotificationReceiver connected");

        initialize();
    }

    @Override
    public void onListenerDisconnected() {
        Log.v(TAG, "NotificationReceiver disconnected");

        notificationSender.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return super.onBind(intent);
        }

        initialize();

        return notificationReceiverBinder;
    }

    private synchronized void initialize() {
        if (initialized) {
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences_key), Context.MODE_PRIVATE);

        notificationSender = new NotificationSender(new ArrayList<>(sharedPreferences.getStringSet(getString(R.string.preferences_addresses_key), new HashSet<>())));
        disabledApps = new ArrayList<>(sharedPreferences.getStringSet(getString(R.string.preferences_disabled_apps_key), new HashSet<>()));

        initialized = true;
    }

    public void setAddresses(List<String> addresses) {
        notificationSender.setAddresses(addresses);
    }

    public void setDisabledApps(List<String> disabledApps) {
        this.disabledApps = disabledApps;
    }

    public class NotificationReceiverBinder extends Binder {

        public NotificationReceiver getNotificationReceiver() {
            return NotificationReceiver.this;
        }
    }
}
