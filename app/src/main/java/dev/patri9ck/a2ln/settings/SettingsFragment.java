package dev.patri9ck.a2ln.settings;

import android.content.Context;
import android.content.Intent;
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

    private NotificationSender notificationSender;

    private FragmentSettingsBinding fragmentSettingsBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

        fragmentSettingsBinding.permissionButton.setOnClickListener(permissionButtonView -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        fragmentSettingsBinding.helpButton.setOnClickListener(helpButtonView -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url)))));
        fragmentSettingsBinding.notificationButton.setOnClickListener(notificationButtonView -> sendNotification());

        fragmentSettingsBinding.versionTextView.setText(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        return fragmentSettingsBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        notificationSender = NotificationSender.fromSharedPreferences(requireContext(), requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE));
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
