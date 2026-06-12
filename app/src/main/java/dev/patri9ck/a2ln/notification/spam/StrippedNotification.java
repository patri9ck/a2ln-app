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

import java.util.Objects;

import dev.patri9ck.a2ln.notification.ParsedNotification;

public class StrippedNotification {

    private final String appName;
    private final String title;
    private final String text;

    public StrippedNotification(ParsedNotification parsedNotification) {
        this.appName = parsedNotification.getAppName();
        this.title = parsedNotification.getTitle();
        this.text = parsedNotification.getText();
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        StrippedNotification strippedNotification = (StrippedNotification) object;

        return appName.equals(strippedNotification.appName) && title.equals(strippedNotification.title) && text.equals(strippedNotification.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, title, text);
    }
}
