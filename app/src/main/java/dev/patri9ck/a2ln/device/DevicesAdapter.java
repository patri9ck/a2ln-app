package dev.patri9ck.a2ln.device;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.DialogEditPortBinding;
import dev.patri9ck.a2ln.databinding.ItemDeviceBinding;
import dev.patri9ck.a2ln.main.ui.DevicesFragment;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder> {

    private final DevicesFragment devicesFragment;
    private final BoundNotificationReceiver boundNotificationReceiver;
    private final List<Device> devices;

    public DevicesAdapter(DevicesFragment devicesFragment, BoundNotificationReceiver boundNotificationReceiver, List<Device> devices) {
        this.devicesFragment = devicesFragment;
        this.boundNotificationReceiver = boundNotificationReceiver;
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceViewHolder(ItemDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        Device device = devices.get(position);

        holder.addressTextView.setText(device.getAddress());

        holder.addressTextView.setOnClickListener(view -> {

            DialogEditPortBinding dialogEditPortBinding = DialogEditPortBinding.inflate(devicesFragment.getLayoutInflater());

            new MaterialAlertDialogBuilder(devicesFragment.requireContext(), R.style.Dialog)
                    .setTitle(R.string.edit_port_dialog_title)
                    .setView(dialogEditPortBinding.getRoot())
                    .setPositiveButton(R.string.apply, (editPortDialog, which) -> {
                        int devicePort;

                        try {
                            devicePort = Integer.parseInt(dialogEditPortBinding.deviceEditPortEditText.getText().toString());
                        } catch (NumberFormatException exception) {
                            return;
                        }

                        device.setPort(devicePort);

                        notifyItemChanged(position);

                        boundNotificationReceiver.updateNotificationReceiver();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    protected static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final TextView addressTextView;

        public DeviceViewHolder(ItemDeviceBinding itemDeviceBinding) {
            super(itemDeviceBinding.getRoot());

            addressTextView = itemDeviceBinding.addressTextView;
        }
    }
}
