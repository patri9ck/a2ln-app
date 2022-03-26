package dev.patri9ck.a2ln.address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.patri9ck.a2ln.R;

public class AddressesAdapter extends RecyclerView.Adapter<AddressesAdapter.AddressViewHolder> {

    private List<String> addresses;

    public AddressesAdapter(List<String> addresses) {
        this.addresses = addresses;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.address_item, parent, false));
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        holder.addressTextView.setText(addresses.get(position));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    protected static class AddressViewHolder extends RecyclerView.ViewHolder {

        private final TextView addressTextView;

        public AddressViewHolder(View itemView) {
            super(itemView);

            addressTextView = itemView.findViewById(R.id.address_text_view);
        }
    }
}
