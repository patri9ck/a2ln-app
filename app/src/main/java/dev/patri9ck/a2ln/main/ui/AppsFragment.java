package dev.patri9ck.a2ln.main.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.app.AppsAdapter;
import dev.patri9ck.a2ln.databinding.FragmentAppsBinding;
import dev.patri9ck.a2ln.notification.NotificationReceiver;

public class AppsFragment extends Fragment {

    private List<String> disabledApps;
    private AppsAdapter appsAdapter;

    private SharedPreferences sharedPreferences;

    private boolean bound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NotificationReceiver notificationReceiver = ((NotificationReceiver.NotificationReceiverBinder) service).getNotificationReceiver();

            notificationReceiver.setDisabledApps(disabledApps);

            appsAdapter.setNotificationReceiver(notificationReceiver);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            appsAdapter.setNotificationReceiver(null);
        }
    };

    private FragmentAppsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        disabledApps = new ArrayList<>(sharedPreferences.getStringSet(getString(R.string.preferences_disabled_apps), new HashSet<>()));

        loadAppsRecyclerView();

        bound = requireContext().bindService(new Intent(getContext(), NotificationReceiver.class), serviceConnection, 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        sharedPreferences.edit().putStringSet(getString(R.string.preferences_disabled_apps), new HashSet<>(disabledApps)).apply();

        if (bound) {
            requireContext().unbindService(serviceConnection);

            bound = false;
        }
    }

    private void loadAppsRecyclerView() {
        appsAdapter = new AppsAdapter(disabledApps, null, requireContext().getPackageManager());

        binding.appsRecyclerView.setAdapter(appsAdapter);
        binding.appsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
