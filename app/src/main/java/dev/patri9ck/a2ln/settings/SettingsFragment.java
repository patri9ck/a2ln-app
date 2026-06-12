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
package dev.patri9ck.a2ln.settings;

import android.Manifest;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.patri9ck.a2ln.BuildConfig;
import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.DialogPermissionRequestBinding;
import dev.patri9ck.a2ln.databinding.FragmentSettingsBinding;
import dev.patri9ck.a2ln.log.LogDialogBuilder;
import dev.patri9ck.a2ln.notification.NotificationSender;
import dev.patri9ck.a2ln.notification.ParsedNotification;
import dev.patri9ck.a2ln.util.Storage;
import dev.patri9ck.a2ln.util.Util;

public class SettingsFragment extends Fragment {

    private static final int TIMEOUT_SECONDS = 5;

    private SharedPreferences sharedPreferences;
    private Storage storage;
    private NotificationManagerCompat notificationManagerCompat;
    private FragmentSettingsBinding fragmentSettingsBinding;

    private boolean sending;

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if (!getString(R.string.preferences_log).equals(key) || !sending) {
            return;
        }

        notificationManagerCompat.cancel(0);

        storage.loadLog().ifPresent(this::succeed);
    };

    private final ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            sendNotification();

            return;
        }

        sendNotificationDirectly();
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        storage = new Storage(requireContext(), sharedPreferences);
        notificationManagerCompat = NotificationManagerCompat.from(requireContext());
        fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

        fragmentSettingsBinding.permissionButton.setOnClickListener(permissionButtonView -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        fragmentSettingsBinding.notificationButton.setOnClickListener(notificationButtonView -> sendNotification());

        storage.loadSimilarity().ifPresent(similarity -> fragmentSettingsBinding.similarityEditText.setText(String.format(Locale.getDefault(), "%d", (int) (similarity * 100F))));
        storage.loadDuration().ifPresent(duration -> fragmentSettingsBinding.durationEditText.setText(String.format(Locale.getDefault(), "%d", duration)));

        fragmentSettingsBinding.similarityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String rawSimilarity = editable.toString();

                if (rawSimilarity.trim().isEmpty()) {
                    storage.removeSimilarity();

                    return;
                }

                Optional<Integer> optionalSimilarity = Util.parseInteger(rawSimilarity).filter(similarity -> similarity >= 0 && similarity <= 100);

                if (!optionalSimilarity.isPresent()) {
                    fragmentSettingsBinding.similarityEditText.getText().clear();

                    return;
                }

                storage.saveSimilarity(optionalSimilarity.get() / 100F);
            }
        });

        fragmentSettingsBinding.durationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String rawDuration = editable.toString();

                if (rawDuration.trim().isEmpty()) {
                    storage.removeDuration();

                    return;
                }

                Optional<Integer> optionalDuration = Util.parseInteger(rawDuration).filter(duration -> duration >= 0);

                if (!optionalDuration.isPresent()) {
                    fragmentSettingsBinding.durationEditText.getText().clear();

                    return;
                }

                storage.saveDuration(optionalDuration.get());
            }
        });

        fragmentSettingsBinding.displayCheckBox.setChecked(storage.loadDisplay());
        fragmentSettingsBinding.displayCheckBox.setOnCheckedChangeListener((displayCheckBoxView, isChecked) -> storage.saveDisplay(isChecked));

        fragmentSettingsBinding.noAppCheckBox.setChecked(storage.loadNoApp());
        fragmentSettingsBinding.noAppCheckBox.setOnCheckedChangeListener((noAppCheckBoxView, isChecked) -> storage.saveNoApp(isChecked));

        fragmentSettingsBinding.versionTextView.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        fragmentSettingsBinding.helpTextView.setMovementMethod(LinkMovementMethod.getInstance());

        return fragmentSettingsBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        fragmentSettingsBinding = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private void sendNotification() {
        if (sending) {
            return;
        }

        NotificationChannel notificationChannel = notificationManagerCompat.getNotificationChannel(getString(R.string.channel_id));

        if (notificationChannel == null || notificationChannel.getImportance() == NotificationManagerCompat.IMPORTANCE_NONE || !notificationManagerCompat.areNotificationsEnabled()) {
            sendNotificationDirectly();

            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManagerCompat.notify(0, new NotificationCompat.Builder(requireContext(), getString(R.string.channel_id))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .build());

            sending = true;

            fragmentSettingsBinding.sendingProgressIndicator.setVisibility(View.VISIBLE);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> requireActivity().runOnUiThread(() -> {
                if (sending) {
                    sending = false;

                    fragmentSettingsBinding.sendingProgressIndicator.setVisibility(View.INVISIBLE);

                    notificationManagerCompat.cancel(0);

                    Snackbar.make(fragmentSettingsBinding.getRoot(), R.string.notification_timed_out, Snackbar.LENGTH_SHORT).show();
                }
            }), TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return;
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            DialogPermissionRequestBinding dialogPermissionRequestBinding = DialogPermissionRequestBinding.inflate(getLayoutInflater());

            dialogPermissionRequestBinding.permissionRequestTextView.setText(R.string.permission_request_dialog_notification_information);

            new MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                    .setTitle(R.string.permission_request_dialog_title)
                    .setView(dialogPermissionRequestBinding.getRoot())
                    .setPositiveButton(R.string.grant, (requestPermissionDialog, which) -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS))
                    .setNegativeButton(R.string.cancel, (requestPermissionDialog, which) -> sendNotificationDirectly())
                    .show();

            return;
        }

        launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void sendNotificationDirectly() {
        NotificationSender.fromStorage(requireContext(), storage).ifPresent(notificationSender -> {
            sending = true;

            fragmentSettingsBinding.sendingProgressIndicator.setVisibility(View.VISIBLE);

            CompletableFuture.supplyAsync(() -> notificationSender.sendParsedNotification(new ParsedNotification(getString(R.string.app_name),
                    getString(R.string.notification_title),
                    getString(R.string.notification_text)))).thenAccept(keptLog -> requireActivity().runOnUiThread(() -> succeed(keptLog.format())));
        });
    }

    private void succeed(String log) {
        sending = false;

        fragmentSettingsBinding.sendingProgressIndicator.setVisibility(View.INVISIBLE);

        Snackbar.make(fragmentSettingsBinding.getRoot(), R.string.notification_sent, Snackbar.LENGTH_LONG)
                .setAction(R.string.view_log, view -> {
                    if (isVisible()) {
                        new LogDialogBuilder(log, getLayoutInflater()).show();
                    }
                })
                .show();
    }


}
