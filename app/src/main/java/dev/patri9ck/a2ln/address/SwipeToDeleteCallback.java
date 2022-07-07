package dev.patri9ck.a2ln.address;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.main.ui.DevicesFragment;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private DevicesFragment devicesFragment;
    private List<Device> devices;
    private DevicesAdapter devicesAdapter;

    public SwipeToDeleteCallback(DevicesFragment devicesFragment, List<Device> devices, DevicesAdapter devicesAdapter) {
        super(0, ItemTouchHelper.LEFT);

        this.devicesFragment = devicesFragment;
        this.devices = devices;
        this.devicesAdapter = devicesAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        Device device = devices.remove(position);

        devicesAdapter.notifyItemRemoved(position);

        Snackbar.make(devicesFragment.requireActivity().findViewById(android.R.id.content), R.string.removed_address, Snackbar.LENGTH_LONG)
                .setAction(R.string.removed_address_undo, v -> {
                    devices.add(position, device);

                    devicesAdapter.notifyItemInserted(position);
                }).show();
    }
}
