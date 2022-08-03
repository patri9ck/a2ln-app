package dev.patri9ck.a2ln.notification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.device.Device;
import dev.patri9ck.a2ln.util.JsonListConverter;

public class NotificationReceiver extends NotificationListenerService {

    private static final String TAG = "A2LNNR";

    private final NotificationReceiverBinder notificationReceiverBinder = new NotificationReceiverBinder();
    private final NotificationSpamHandler notificationSpamHandler = new NotificationSpamHandler();

    private boolean initialized;

    private NotificationSender notificationSender;
    private List<String> disabledApps;

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if (!initialized) {
            return;
        }

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

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        notificationSender = NotificationSender.fromSharedPreferences(this, sharedPreferences);

        if (notificationSender == null) {
            return;
        }

        disabledApps = JsonListConverter.fromJson(sharedPreferences.getString(getString(R.string.preferences_disabled_apps), null), String.class);
        initialized = true;
    }

    public void setDevices(List<Device> devices) {
        if (!initialized) {
            return;
        }

        notificationSender.setDevices(devices);
    }

    public void setDisabledApps(List<String> disabledApps) {
        if (!initialized) {
            return;
        }

        this.disabledApps = disabledApps;
    }

    public class NotificationReceiverBinder extends Binder {

        public NotificationReceiver getNotificationReceiver() {
            return NotificationReceiver.this;
        }
    }
}
