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
package dev.patri9ck.a2ln.notification.spam;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

import dev.patri9ck.a2ln.notification.ParsedNotification;
import dev.patri9ck.a2ln.util.Util;

public class NotificationSpamHandler {

    private Cache<StrippedNotification, Object> strippedNotifications;

    private float similarity;

    public NotificationSpamHandler(float similarity, int duration) {
        this.similarity = similarity;

        setDuration(duration);
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public void setDuration(int duration) {
        strippedNotifications = CacheBuilder.newBuilder()
                .expireAfterWrite(duration, TimeUnit.SECONDS)
                .build();
    }

    public boolean isSpammed(ParsedNotification parsedNotification) {
        StrippedNotification strippedNotification = new StrippedNotification(parsedNotification);

        if (strippedNotifications.asMap().containsKey(strippedNotification)) {
            return true;
        }

        if (similarity < 1F) {
            for (StrippedNotification spammedStrippedNotification : strippedNotifications.asMap().keySet()) {
                if (spammedStrippedNotification.getAppName().equals(strippedNotification.getAppName())
                        && spammedStrippedNotification.getTitle().equals(strippedNotification.getTitle())
                        && Util.getSimilarity(spammedStrippedNotification.getText(), strippedNotification.getText()) >= similarity) {
                    strippedNotifications.put(spammedStrippedNotification, new Object());

                    return true;
                }
            }
        }

        strippedNotifications.put(strippedNotification, new Object());

        return false;
    }
}
