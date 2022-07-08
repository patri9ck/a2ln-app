package dev.patri9ck.a2ln.main.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.FragmentSettingsBinding;
import dev.patri9ck.a2ln.notification.NotificationSender;
import dev.patri9ck.a2ln.notification.ParsedNotification;

public class SettingsFragment extends Fragment {

    private void sendTestNotification() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getString(R.string.preferences_key), Context.MODE_PRIVATE);

        NotificationSender notificationSender = new NotificationSender(new ArrayList<>(sharedPreferences.getStringSet(getString(R.string.preferences_addresses_key), new HashSet<>())));

        ParsedNotification notification = ParsedNotification.makeTestNotification();

        notificationSender.sendParsedNotification(notification);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(inflater, container, false);

        binding.permissionsButton.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        binding.helpButton.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url)))));
        binding.testButton.setOnClickListener(view -> sendTestNotification());

        return binding.getRoot();
    }
}
