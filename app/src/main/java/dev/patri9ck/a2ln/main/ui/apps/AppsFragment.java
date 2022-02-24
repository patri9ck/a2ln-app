package dev.patri9ck.a2ln.main.ui.apps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import dev.patri9ck.a2ln.app.AppsAdapter;
import dev.patri9ck.a2ln.databinding.FragmentAppsBinding;
import dev.patri9ck.a2ln.main.MainActivity;
import dev.patri9ck.a2ln.notification.NotificationReceiver;

public class AppsFragment extends Fragment {

    private List<String> disabledApps;
    private AppsAdapter appsAdapter;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater, container, false);

        loadAppsRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        bound = getContext().bindService(new Intent(getContext(), NotificationReceiver.class), serviceConnection, 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!bound) {
            return;
        }

        getContext().unbindService(serviceConnection);
        bound = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadAppsRecyclerView() {
        disabledApps = MainActivity.configuration.getDisabledApps();
        appsAdapter = new AppsAdapter(disabledApps, null, getContext().getPackageManager());

        binding.appsRecyclerView.setAdapter(appsAdapter);
        binding.appsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}