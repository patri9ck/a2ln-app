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

package dev.patri9ck.a2ln.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Optional;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.server.Server;

public class Storage {

    private final Context context;
    private final SharedPreferences sharedPreferences;

    public Storage(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }

    public List<Server> loadServers() {
        return Util.fromJson(sharedPreferences.getString(context.getString(R.string.preferences_servers), null), Server.class);
    }

    public void saveServers(List<Server> servers) {
        sharedPreferences.edit().putString(context.getString(R.string.preferences_servers), Util.toJson(servers)).apply();
    }

    public Optional<String> loadRawPublicKey() {
        return Optional.ofNullable(sharedPreferences.getString(context.getString(R.string.preferences_public_key), null));
    }

    public Optional<String> loadRawSecretKey() {
        return Optional.ofNullable(sharedPreferences.getString(context.getString(R.string.preferences_secret_key), null));
    }

    public List<String> loadDisabledApps() {
        return Util.fromJson(sharedPreferences.getString(context.getString(R.string.preferences_disabled_apps), null), String.class);
    }

    public void saveDisabledApps(List<String> disabledApps) {
        sharedPreferences.edit().putString(context.getString(R.string.preferences_disabled_apps), Util.toJson(disabledApps)).apply();
    }

    public Optional<Float> loadSimilarity() {
        float similarity = sharedPreferences.getFloat(context.getString(R.string.preferences_similarity), Float.MIN_VALUE);

        return Optional.ofNullable(similarity == Float.MIN_VALUE ? null : similarity);
    }

    public float loadSimilarityOrDefault() {
        return loadSimilarity().orElse(1F);
    }

    public void saveSimilarity(float similarity) {
        sharedPreferences.edit().putFloat(context.getString(R.string.preferences_similarity), similarity).apply();
    }

    public void removeSimilarity() {
        sharedPreferences.edit().remove(context.getString(R.string.preferences_similarity)).apply();
    }

    public Optional<Integer> loadDuration() {
        int duration = sharedPreferences.getInt(context.getString(R.string.preferences_duration), Integer.MIN_VALUE);

        return Optional.ofNullable(duration == Integer.MIN_VALUE ? null : duration);
    }

    public int loadDurationOrDefault() {
        return loadDuration().orElse(1);
    }

    public void saveDuration(int duration) {
        sharedPreferences.edit().putInt(context.getString(R.string.preferences_duration), duration).apply();
    }

    public void removeDuration() {
        sharedPreferences.edit().remove(context.getString(R.string.preferences_duration)).apply();
    }

    public boolean loadDisplay() {
        return sharedPreferences.getBoolean(context.getString(R.string.preferences_display), false);
    }

    public void saveDisplay(boolean display) {
        sharedPreferences.edit().putBoolean(context.getString(R.string.preferences_display), display).apply();
    }

    public Optional<String> loadLog() {
        return Optional.ofNullable(sharedPreferences.getString(context.getString(R.string.preferences_log), null));
    }

    public void saveLog(String log) {
        sharedPreferences.edit().putString(context.getString(R.string.preferences_log), log).apply();
    }
}
