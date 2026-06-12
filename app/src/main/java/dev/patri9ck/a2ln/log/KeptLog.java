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
package dev.patri9ck.a2ln.log;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KeptLog {

    private final List<String> messages = new ArrayList<>();

    private final Context context;
    private final String tag;

    public KeptLog(Context context, String tag) {
        this.context = context;
        this.tag = tag;
    }

    public void log(int priority, int id, Object... arguments) {
        String message = context.getString(id, arguments);

        messages.add(DateFormat.getTimeInstance().format(new Date()) + ": " + message);

        Log.println(priority, tag, message);
    }

    public String format() {
        return String.join("\n", messages);
    }
}
