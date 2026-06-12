package dev.patri9ck.a2ln.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import dev.patri9ck.a2ln.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(inflater, container, false);

        binding.permissionsButton.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        return binding.getRoot();
    }
}
