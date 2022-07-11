package dev.patri9ck.a2ln.device;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.main.ui.DevicesFragment;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.AddressViewHolder> {

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
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        Device device = devices.get(position);

        holder.addressTextView.setText(device.getAddress());

        holder.addressTextView.setOnClickListener(view -> {
            View editPortDialogView = devicesFragment.getLayoutInflater().inflate(R.layout.dialog_edit_port, null);

            new AlertDialog.Builder(devicesFragment.requireContext())
                    .setTitle(R.string.edit_port_dialog_title)
                    .setView(editPortDialogView)
                    .setPositiveButton(R.string.apply, (editPortDialog, which) -> {
                        int devicePort;

                        try {
                            devicePort = Integer.parseInt(((EditText) editPortDialogView.findViewById(R.id.device_edit_port_edit_text)).getText().toString());
                        } catch (NumberFormatException exception) {
                            return;
                        }

                        device.setPort(devicePort);

                        notifyItemChanged(position);

                        boundNotificationReceiver.updateNotificationReceiver();
                    })
                    .setNegativeButton(R.string.cancel, (editPortDialog, which) -> editPortDialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    protected static class AddressViewHolder extends RecyclerView.ViewHolder {

        private final TextView addressTextView;

        public AddressViewHolder(View itemView) {
            super(itemView);

            addressTextView = itemView.findViewById(R.id.address_text_view);
        }
    }
}
