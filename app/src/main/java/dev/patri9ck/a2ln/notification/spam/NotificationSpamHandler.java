/*
 * Copyright (C) 2023  Patrick Zwick and contributors
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

    private Cache<SimpleNotification, Object> simpleNotifications;

    private float similarity;

    public NotificationSpamHandler(float similarity, int duration) {
        this.similarity = similarity;

        setDuration(duration);
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public void setDuration(int duration) {
        simpleNotifications = CacheBuilder.newBuilder()
                .expireAfterWrite(duration, TimeUnit.SECONDS)
                .build();
    }

    public boolean isSpammed(ParsedNotification parsedNotification) {
        SimpleNotification simpleNotification = new SimpleNotification(parsedNotification);

        if (simpleNotifications.asMap().containsKey(simpleNotification)) {
            return true;
        }

        boolean spammed = false;

        if (similarity < 1F) {
            for (SimpleNotification spammedSimpleNotification : simpleNotifications.asMap().keySet()) {
                if (spammedSimpleNotification.getAppName().equals(simpleNotification.getAppName())
                        && spammedSimpleNotification.getTitle().equals(simpleNotification.getTitle())
                        && Util.getSimilarity(spammedSimpleNotification.getText(), simpleNotification.getText()) >= similarity) {
                    spammed = true;

                    break;
                }
            }
        }

        simpleNotifications.put(simpleNotification, new Object());

        return spammed;
    }
}
