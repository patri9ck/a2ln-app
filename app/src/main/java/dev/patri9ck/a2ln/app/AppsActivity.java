package dev.patri9ck.a2ln.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.address.AddressesActivity;
import dev.patri9ck.a2ln.configuration.Configuration;
import dev.patri9ck.a2ln.configuration.Storage;
import dev.patri9ck.a2ln.notification.NotificationReceiver;

public class AppsActivity extends Activity {

    private Gson gson = new Gson();

    private Storage storage;
    private Configuration configuration;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.apps_activity);

        storage = new Storage(this);
        configuration = gson.fromJson(getIntent().getStringExtra(Configuration.class.getName()), Configuration.class);

        if (configuration == null) {
            configuration = storage.loadConfiguration();
        }

        loadAppsRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        bound = bindService(new Intent(this, NotificationReceiver.class), serviceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!bound) {
            return;
        }

        unbindService(serviceConnection);
        bound = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        storage.saveConfiguration(configuration);
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, AddressesActivity.class);

        intent.putExtra(Configuration.class.getName(), gson.toJson(configuration));

        startActivity(intent);
    }

    private void loadAppsRecyclerView() {
        disabledApps = configuration.getDisabledApps();
        appsAdapter = new AppsAdapter(disabledApps, null, getPackageManager());

        RecyclerView appsRecyclerView = findViewById(R.id.apps_recycler_view);

        appsRecyclerView.setAdapter(appsAdapter);
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
