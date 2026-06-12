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
package dev.patri9ck.a2ln.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class JsonListConverter {

    private static final Gson GSON = new Gson();

    private JsonListConverter() {}

    public static String toJson(List<?> data) {
        return GSON.toJson(data);
    }

    public static <T> List<T> fromJson(String json, Class<T> type) {
        if (json == null) {
            return new ArrayList<>();
        }

        return GSON.fromJson(json, TypeToken.getParameterized(ArrayList.class, type).getType());
    }
}
