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
package dev.patri9ck.a2ln.log;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class KeptLog {

    private final List<String> messages = new ArrayList<>();

    private final String tag;

    public KeptLog(String tag) {
        this.tag = tag;
    }

    public void log(String message) {
        messages.add(message);

        Log.v(tag, message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
