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
package dev.patri9ck.a2ln.util;

import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Util {

    private static final int MINIMUM_PORT = 1;
    private static final int MAXIMUM_PORT = 65535;

    private static final Gson GSON = new Gson();

    private Util() {
    }

    public static String toJson(List<?> data) {
        return GSON.toJson(data);
    }

    public static <T> List<T> fromJson(String json, Class<T> type) {
        if (json == null) {
            return new ArrayList<>();
        }

        return GSON.fromJson(json, TypeToken.getParameterized(ArrayList.class, type).getType());
    }

    public static Optional<Integer> parsePort(String port) {
        try {
            int parsedPort = Integer.parseInt(port);

            if (parsedPort >= MINIMUM_PORT && parsedPort <= MAXIMUM_PORT) {
                return Optional.of(parsedPort);
            }
        } catch (NumberFormatException ignored) {
        }

        return Optional.empty();
    }

    public static Optional<String> getAppName(PackageManager packageManager, String packageName) {
        try {
            return Optional.of((String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)));
        } catch (PackageManager.NameNotFoundException exception) {
            return Optional.empty();
        }
    }
}
