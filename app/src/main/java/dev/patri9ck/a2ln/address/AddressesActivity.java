package dev.patri9ck.a2ln.address;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.app.AppsActivity;
import dev.patri9ck.a2ln.configuration.Configuration;
import dev.patri9ck.a2ln.configuration.Storage;
import dev.patri9ck.a2ln.notification.NotificationReceiver;

public class AddressesActivity extends AppCompatActivity {

    private Gson gson = new Gson();

    private Storage storage;
    private Configuration configuration;

    private List<Address> addresses;
    private AddressesAdapter addressesAdapter;

    private NotificationReceiver notificationReceiver;
    private boolean bound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationReceiver = ((NotificationReceiver.NotificationReceiverBinder) service).getNotificationReceiver();

            notificationReceiver.setAddresses(addresses);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationReceiver = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.addresses_activity);

        storage = new Storage(this);
        configuration = gson.fromJson(getIntent().getStringExtra(Configuration.class.getName()), Configuration.class);

        if (configuration == null) {
            configuration = storage.loadConfiguration();
        }

        loadAddressesRecyclerView();
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

    public void onAdd(View view) {
        String host = ((EditText) findViewById(R.id.host_edit_text)).getText().toString();
        String port = ((EditText) findViewById(R.id.port_edit_text)).getText().toString();

        if (host.isEmpty() || port.isEmpty()) {
            return;
        }

        for (Address address : addresses) {
            if (address.getHost().equals(host)) {
                return;
            }
        }

        addresses.add(new Address(host, Integer.parseInt(port)));

        addressesAdapter.notifyItemInserted(addresses.size());

        if (notificationReceiver == null) {
            return;
        }

        notificationReceiver.setAddresses(addresses);
    }

    public void onApps(View view) {
        Intent intent = new Intent(this, AppsActivity.class);

        intent.putExtra(Configuration.class.getName(), gson.toJson(configuration));

        startActivity(intent);
    }

    public void onPermission(View view) {
        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    private void loadAddressesRecyclerView() {
        addresses = configuration.getAddresses();
        addressesAdapter = new AddressesAdapter(addresses);

        RecyclerView addressesRecyclerView = findViewById(R.id.addresses_recycler_view);

        addressesRecyclerView.setAdapter(addressesAdapter);
        addressesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new ItemTouchHelper(new SwipeToDeleteCallback(this, addresses, addressesAdapter)).attachToRecyclerView(addressesRecyclerView);
    }
}
