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

import java.util.HashMap;
import java.util.Map;

public class NotificationSpamHandler {

    private static final long BLOCK_SECONDS = 5;

    private final Map<String, Long> parsedNotifications = new HashMap<>();

    public void cleanUp() {
        parsedNotifications.keySet().removeIf(parsedNotification -> !isSpammed(parsedNotification));
    }

    public void addParsedNotification(ParsedNotification parsedNotification) {
        parsedNotifications.put(getKey(parsedNotification), System.currentTimeMillis() + BLOCK_SECONDS * 1000);
    }

    public boolean isSpammed(ParsedNotification parsedNotification) {
        return isSpammed(getKey(parsedNotification));
    }

    private boolean isSpammed(String key) {
        return parsedNotifications.containsKey(key) && parsedNotifications.get(key) > System.currentTimeMillis();
    }

    private String getKey(ParsedNotification parsedNotification) {
        return parsedNotification.getTitle() + parsedNotification.getText();
    }
}
