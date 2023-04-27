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
package dev.patri9ck.a2ln.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.primitives.Ints;

import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.BuildConfig;
import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.FragmentSettingsBinding;
import dev.patri9ck.a2ln.log.LogsDialogBuilder;
import dev.patri9ck.a2ln.notification.NotificationSender;
import dev.patri9ck.a2ln.notification.ParsedNotification;
import dev.patri9ck.a2ln.util.Storage;

public class SettingsFragment extends Fragment {

    private Storage storage;

    private boolean sending;

    private FragmentSettingsBinding fragmentSettingsBinding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        storage = new Storage(requireContext(), requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE));

        fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

        fragmentSettingsBinding.permissionButton.setOnClickListener(permissionButtonView -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        fragmentSettingsBinding.notificationButton.setOnClickListener(notificationButtonView -> sendNotification());

        storage.loadSimilarity().ifPresent(similarity -> fragmentSettingsBinding.similarityEditText.setText(Integer.toString((int) (similarity * 100F))));
        storage.loadDuration().ifPresent(duration -> fragmentSettingsBinding.similarityEditText.setText(Integer.toString(duration)));

        fragmentSettingsBinding.similarityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // Ignored
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                // Ignored
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String rawSimilarity = editable.toString();

                if (rawSimilarity.trim().isEmpty()) {
                    storage.removeSimilarity();

                    return;
                }

                Integer similarity = Ints.tryParse(rawSimilarity);

                if (similarity == null || similarity < 0F || similarity > 100F) {
                    fragmentSettingsBinding.similarityEditText.getText().clear();

                    return;
                }

                storage.saveSimilarity(similarity / 100F);
            }
        });

        fragmentSettingsBinding.durationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // Ignored
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                // Ignored
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String rawDuration = editable.toString();

                if (rawDuration.trim().isEmpty()) {
                    storage.removeDuration();

                    return;
                }

                Integer duration = Ints.tryParse(rawDuration);

                if (duration == null || duration < 0) {
                    fragmentSettingsBinding.durationEditText.getText().clear();

                    return;
                }

                storage.saveDuration(duration);
            }
        });

        fragmentSettingsBinding.displayCheckBox.setChecked(storage.loadDisplay());
        fragmentSettingsBinding.displayCheckBox.setOnCheckedChangeListener((displayCheckBoxView, isChecked) -> storage.saveDisplay(isChecked));

        fragmentSettingsBinding.versionTextView.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        fragmentSettingsBinding.helpTextView.setMovementMethod(LinkMovementMethod.getInstance());

        return fragmentSettingsBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        fragmentSettingsBinding = null;
    }

    private void sendNotification() {
        if (sending) {
            return;
        }

        fragmentSettingsBinding.sendingProgressIndicator.setVisibility(View.VISIBLE);

        sending = true;

        NotificationSender.fromStorage(requireContext(), storage).ifPresent(notificationSender -> CompletableFuture.supplyAsync(() -> notificationSender.sendParsedNotification(new ParsedNotification(getString(R.string.app_name),
                getString(R.string.notification_title),
                getString(R.string.notification_text)))).thenAccept(keptLog -> requireActivity().runOnUiThread(() -> {
            fragmentSettingsBinding.sendingProgressIndicator.setVisibility(View.INVISIBLE);

            sending = false;

            Snackbar.make(fragmentSettingsBinding.getRoot(), R.string.notification_sent, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.view_logs, view -> {
                        if (isVisible()) {
                            new LogsDialogBuilder(keptLog, getLayoutInflater()).show();
                        }
                    }).show();
        })));
    }
}
