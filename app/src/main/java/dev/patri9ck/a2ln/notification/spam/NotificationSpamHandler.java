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

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

import dev.patri9ck.a2ln.notification.ParsedNotification;
import dev.patri9ck.a2ln.util.Storage;
import dev.patri9ck.a2ln.util.Util;

public class NotificationSpamHandler {

    private final ExpiringMap<StrippedNotification, Object> strippedNotifications = ExpiringMap.builder()
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    private float similarity;
    private int duration;

    public NotificationSpamHandler(float similarity, int duration) {
        this.similarity = similarity;
        this.duration = duration;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isSpammed(ParsedNotification parsedNotification, boolean simple) {
        StrippedNotification strippedNotification = new StrippedNotification(parsedNotification);

        if (strippedNotifications.containsKey(strippedNotification)) {
            return true;
        }

        if (!simple && similarity < Storage.DEFAULT_SIMILARITY) {
            for (StrippedNotification spammedStrippedNotification : strippedNotifications.keySet()) {
                if (spammedStrippedNotification.getAppName().equals(strippedNotification.getAppName())
                        && spammedStrippedNotification.getTitle().equals(strippedNotification.getTitle())
                        && Util.getSimilarity(spammedStrippedNotification.getText(), strippedNotification.getText()) >= similarity) {
                    strippedNotifications.put(spammedStrippedNotification, new Object(), duration, TimeUnit.SECONDS);

                    return true;
                }
            }
        }

        strippedNotifications.put(strippedNotification, new Object(), simple ? Storage.DEFAULT_DURATION : duration, TimeUnit.SECONDS);

        return false;
    }
}
