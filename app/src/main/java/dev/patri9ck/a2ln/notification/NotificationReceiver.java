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
package dev.patri9ck.a2ln.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Display;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.notification.spam.NotificationSpamHandler;
import dev.patri9ck.a2ln.util.Storage;
import dev.patri9ck.a2ln.util.Util;

public class NotificationReceiver extends NotificationListenerService {

    private static final String TAG = "A2LNNR";

    private boolean initialized;

    private SharedPreferences sharedPreferences;
    private Storage storage;

    private NotificationSender notificationSender;

    private NotificationSpamHandler notificationSpamHandler;
    private List<String> disabledApps;
    private boolean display;

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if (key.equals(getString(R.string.preferences_servers))) {
            notificationSender.setServers(storage.loadServers());

            return;
        }

        if (key.equals(getString(R.string.preferences_similarity))) {
            notificationSpamHandler.setSimilarity(storage.loadSimilarityOrDefault());

            return;
        }

        if (key.equals(getString(R.string.preferences_duration))) {
            notificationSpamHandler.setDuration(storage.loadDurationOrDefault());

            return;
        }

        if (key.equals(getString(R.string.preferences_disabled_apps))) {
            disabledApps = storage.loadDisabledApps();

            return;
        }

        if (key.equals(getString(R.string.preferences_display))) {
            display = storage.loadDisplay();
        }
    };

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if (!initialized) {
            return;
        }

        PackageManager packageManager = getPackageManager();

        String packageName = statusBarNotification.getPackageName();

        Log.v(TAG, "Notification posted (" + packageName + ")");

        if (packageManager.getLaunchIntentForPackage(packageName) == null) {
            Log.v(TAG, "Not from an actual app");

            return;
        }

        if (disabledApps.contains(packageName)) {
            Log.v(TAG, "App is disabled");

            return;
        }

        if (display) {
            for (Display display : ((DisplayManager) getSystemService(Context.DISPLAY_SERVICE)).getDisplays()) {
                if (display.getState() == Display.STATE_ON) {
                    Log.v(TAG, "Display is on");

                    return;
                }
            }
        }

        ParsedNotification parsedNotification = ParsedNotification.parseNotification(Util.getAppName(packageManager, packageName).orElse(""),
                statusBarNotification.getNotification(),
                this);

        if (parsedNotification == null) {
            Log.v(TAG, "Notification cannot be parsed");

            return;
        }

        if (notificationSpamHandler.isSpammed(parsedNotification)) {
            Log.v(TAG, "Notification is spammed");

            return;
        }

        CompletableFuture.runAsync(() -> notificationSender.sendParsedNotification(parsedNotification));

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

        uninitialize();
    }

    private synchronized void initialize() {
        if (initialized) {
            return;
        }

        sharedPreferences = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        storage = new Storage(this, sharedPreferences);

        NotificationSender.fromStorage(this, storage).ifPresent(notificationSender -> {
            this.notificationSender = notificationSender;

            notificationSpamHandler = new NotificationSpamHandler(storage.loadSimilarityOrDefault(), storage.loadDurationOrDefault());
            disabledApps = storage.loadDisabledApps();
            display = storage.loadDisplay();

            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

            initialized = true;
        });
    }

    private synchronized void uninitialize() {
        if (!initialized) {
            return;
        }

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
