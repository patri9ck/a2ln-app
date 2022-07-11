package dev.patri9ck.a2ln.main.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.app.AppsAdapter;
import dev.patri9ck.a2ln.databinding.FragmentAppsBinding;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;
import dev.patri9ck.a2ln.notification.NotificationReceiver;
import dev.patri9ck.a2ln.notification.NotificationReceiverUpdater;
import dev.patri9ck.a2ln.util.JsonListConverter;

public class AppsFragment extends Fragment implements NotificationReceiverUpdater {

    private List<String> disabledApps;

    private SharedPreferences sharedPreferences;

    private BoundNotificationReceiver boundNotificationReceiver;

    private FragmentAppsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        disabledApps = JsonListConverter.fromJson(sharedPreferences.getString(getString(R.string.preferences_disabled_apps), null), String.class);

        boundNotificationReceiver = new BoundNotificationReceiver(this, requireContext());

        boundNotificationReceiver.bind();

        loadAppsRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();

        sharedPreferences.edit().putString(getString(R.string.preferences_disabled_apps), JsonListConverter.toJson(disabledApps)).apply();

        boundNotificationReceiver.unbind();
    }

    @Override
    public void update(NotificationReceiver notificationReceiver) {
        notificationReceiver.setDisabledApps(disabledApps);
    }

    private void loadAppsRecyclerView() {
        AppsAdapter appsAdapter = new AppsAdapter(disabledApps, boundNotificationReceiver, requireContext().getPackageManager());

        binding.appsRecyclerView.setAdapter(appsAdapter);
        binding.appsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
}
