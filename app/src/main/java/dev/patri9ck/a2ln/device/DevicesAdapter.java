package dev.patri9ck.a2ln.device;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.patri9ck.a2ln.R;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.AddressViewHolder> {

    private List<Device> devices;

    public DevicesAdapter(List<Device> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false));
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        holder.addressTextView.setText(devices.get(position).getAddress());
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
