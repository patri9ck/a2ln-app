package dev.patri9ck.a2ln.device;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final View rootView;
    private final BoundNotificationReceiver boundNotificationReceiver;
    private final List<Device> devices;
    private final DevicesAdapter devicesAdapter;

    public SwipeToDeleteCallback(View rootView, BoundNotificationReceiver boundNotificationReceiver, List<Device> devices, DevicesAdapter devicesAdapter) {
        super(0, ItemTouchHelper.LEFT);

        this.rootView = rootView;
        this.boundNotificationReceiver = boundNotificationReceiver;
        this.devices = devices;
        this.devicesAdapter = devicesAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        Device device = devices.remove(position);

        devicesAdapter.notifyItemRemoved(position);

        boundNotificationReceiver.updateNotificationReceiver();

        Snackbar.make(rootView, R.string.removed_device, Snackbar.LENGTH_LONG)
                .setAction(R.string.removed_device_undo, buttonView -> {
                    devices.add(position, device);

                    devicesAdapter.notifyItemInserted(position);

                    boundNotificationReceiver.updateNotificationReceiver();
                }).show();
    }
}
