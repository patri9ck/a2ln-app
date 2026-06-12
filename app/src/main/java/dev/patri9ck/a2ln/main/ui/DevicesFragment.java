package dev.patri9ck.a2ln.main.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.address.Address;
import dev.patri9ck.a2ln.address.AddressesAdapter;
import dev.patri9ck.a2ln.address.SwipeToDeleteCallback;
import dev.patri9ck.a2ln.databinding.FragmentDevicesBinding;
import dev.patri9ck.a2ln.main.MainActivity;
import dev.patri9ck.a2ln.notification.NotificationReceiver;

public class DevicesFragment extends Fragment {

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

    private FragmentDevicesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);

        loadAddressesRecyclerView();

        binding.floatingActionButton.setOnClickListener(view -> {
            View dialogView = getLayoutInflater().inflate(R.layout.add_dialog, null);

            new AlertDialog.Builder(view.getContext())
                    .setView(dialogView)
                    .setPositiveButton(R.string.add, (dialog, which) -> onAdd(dialogView))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                    .show();
        });

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

    private void onAdd(View view) {
        String host = ((EditText) view.findViewById(R.id.host_edit_text)).getText().toString();
        String port = ((EditText) view.findViewById(R.id.port_edit_text)).getText().toString();

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

    private void loadAddressesRecyclerView() {
        addresses = MainActivity.configuration.getAddresses();
        addressesAdapter = new AddressesAdapter(addresses);

        binding.addressesRecyclerView.setAdapter(addressesAdapter);
        binding.addressesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        new ItemTouchHelper(new SwipeToDeleteCallback(getActivity(), addresses, addressesAdapter)).attachToRecyclerView(binding.addressesRecyclerView);
    }
}
