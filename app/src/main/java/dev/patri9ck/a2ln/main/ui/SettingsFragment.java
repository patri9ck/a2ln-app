package dev.patri9ck.a2ln.main.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.concurrent.CompletableFuture;

import dev.patri9ck.a2ln.BuildConfig;
import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.FragmentSettingsBinding;
import dev.patri9ck.a2ln.notification.NotificationSender;
import dev.patri9ck.a2ln.notification.ParsedNotification;

public class SettingsFragment extends Fragment {

    private NotificationSender notificationSender;

    private void sendTestNotification() {
        if (notificationSender == null) {
            return;
        }

        CompletableFuture.runAsync(() -> notificationSender.sendParsedNotification(new ParsedNotification(getString(R.string.test_title),
                getString(R.string.test_text))));

        Toast.makeText(requireContext(), R.string.test_done, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsBinding fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

        fragmentSettingsBinding.permissionsButton.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        fragmentSettingsBinding.helpButton.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url)))));
        fragmentSettingsBinding.testButton.setOnClickListener(view -> sendTestNotification());

        fragmentSettingsBinding.versionTextView.setText(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        return fragmentSettingsBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        notificationSender = NotificationSender.fromSharedPreferences(requireContext(), requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE));
    }

    @Override
    public void onStop() {
        super.onStop();

        notificationSender.close();
    }
}
