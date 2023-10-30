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
package dev.patri9ck.a2ln.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import dev.patri9ck.a2ln.util.Util;

public class ParsedNotification {

    private static final String TAG = "A2LN";

    private final String appName;
    private final String title;
    private final String text;
    private final byte[] icon;

    public ParsedNotification(String appName, String title, String text, byte[] icon) {
        this.appName = appName;
        this.title = title;
        this.text = text;
        this.icon = icon;
    }

    public ParsedNotification(String appName, String title, String text) {
        this(appName, title, text, null);
    }

    public static Optional<ParsedNotification> parseNotification(StatusBarNotification statusBarNotification, Context context) {
        Notification notification = statusBarNotification.getNotification();

        String title = notification.extras.getString(Notification.EXTRA_TITLE);
        String text = notification.extras.getString(Notification.EXTRA_TEXT);

        if (title == null || text == null) {
            return Optional.empty();
        }

        String appName = Util.getAppName(context.getPackageManager(), statusBarNotification.getPackageName()).orElse("");

        Icon largeIcon = notification.getLargeIcon();

        if (largeIcon != null) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                Drawable drawable = largeIcon.loadDrawable(context);

                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                    return Optional.of(new ParsedNotification(appName, title, text, byteArrayOutputStream.toByteArray()));
                }
            } catch (IOException exception) {
                Log.e(TAG, "Failed to convert picture to bytes", exception);
            }
        }

        return Optional.of(new ParsedNotification(appName, title, text));
    }

    public String getAppName() {
        return appName;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public Optional<byte[]> getIcon() {
        return Optional.ofNullable(icon);
    }
}
