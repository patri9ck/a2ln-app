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
package dev.patri9ck.a2ln.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.BuildConfig;
import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.FragmentSettingsBinding;
import dev.patri9ck.a2ln.notification.NotificationSender;
import dev.patri9ck.a2ln.notification.ParsedNotification;

public class SettingsFragment extends Fragment {

    private static final String UNKNOWN_INSTALLER = "Unknown Installer/APK";

    private NotificationSender notificationSender;

    private FragmentSettingsBinding fragmentSettingsBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

        fragmentSettingsBinding.permissionButton.setOnClickListener(permissionButtonView -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        fragmentSettingsBinding.helpButton.setOnClickListener(helpButtonView -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url)))));
        fragmentSettingsBinding.notificationButton.setOnClickListener(notificationButtonView -> sendNotification());
        fragmentSettingsBinding.versionTextView.setText(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, getInstaller()));

        return fragmentSettingsBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        notificationSender = NotificationSender.fromSharedPreferences(requireContext(), requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        fragmentSettingsBinding = null;
    }

    private String getInstaller() {
        PackageManager packageManager = requireContext().getPackageManager();

        try {
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageManager.getInstallerPackageName(requireContext().getPackageName()), 0));
        } catch (PackageManager.NameNotFoundException exception) {
            return UNKNOWN_INSTALLER;
        }
    }

    private void sendNotification() {
        if (notificationSender == null) {
            return;
        }

        CompletableFuture.runAsync(() -> notificationSender.sendParsedNotification(new ParsedNotification(getString(R.string.notification_title),
                getString(R.string.notification_text))));

        Snackbar.make(fragmentSettingsBinding.getRoot(), R.string.notification_sent, Snackbar.LENGTH_SHORT).show();
    }
}
