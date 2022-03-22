package dev.patri9ck.a2ln.address;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dev.patri9ck.a2ln.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private Activity addressesActivity;
    private List<String> addresses;
    private AddressesAdapter addressesAdapter;

    public SwipeToDeleteCallback(Activity addressesActivity, List<String> addresses, AddressesAdapter addressesAdapter) {
        super(0, ItemTouchHelper.LEFT);

        this.addressesActivity = addressesActivity;
        this.addresses = addresses;
        this.addressesAdapter = addressesAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        String address = addresses.remove(position);

        addressesAdapter.notifyItemRemoved(position);

        Snackbar.make(addressesActivity.findViewById(android.R.id.content), R.string.removed_address, Snackbar.LENGTH_LONG)
                .setAction(R.string.removed_address_undo, v -> {
                    addresses.add(position, address);

                    addressesAdapter.notifyItemInserted(position);
                }).show();
    }
}
