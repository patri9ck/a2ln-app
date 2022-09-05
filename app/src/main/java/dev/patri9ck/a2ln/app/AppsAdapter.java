/*
 * Copyright (C) 2022  Patrick Zwick and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.patri9ck.a2ln.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.patri9ck.a2ln.databinding.ItemAppBinding;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {

    private final List<String> disabledApps;
    private final BoundNotificationReceiver boundNotificationReceiver;
    private final List<App> apps;

    public AppsAdapter(List<String> disabledApps, List<App> apps, BoundNotificationReceiver boundNotificationReceiver) {
        this.disabledApps = disabledApps;
        this.apps = apps;
        this.boundNotificationReceiver = boundNotificationReceiver;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppViewHolder(ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        App app = apps.get(position);

        holder.appCheckBox.setOnCheckedChangeListener((appCheckBoxView, isChecked) -> {
            app.setEnabled(isChecked);

            String packageName = app.getPackageName();

            if (isChecked) {
                disabledApps.remove(packageName);
            } else if (!disabledApps.contains(packageName)) {
                disabledApps.add(packageName);
            }

            boundNotificationReceiver.updateNotificationReceiver();
        });

        holder.appCheckBox.setChecked(app.isEnabled());
        holder.nameTextView.setText(app.getName());
        holder.iconImageView.setImageDrawable(app.getIcon());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    protected static class AppViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final CheckBox appCheckBox;
        private final ImageView iconImageView;

        public AppViewHolder(ItemAppBinding itemAppBinding) {
            super(itemAppBinding.getRoot());

            nameTextView = itemAppBinding.nameTextView;
            appCheckBox = itemAppBinding.appCheckBox;
            iconImageView = itemAppBinding.iconImageView;
        }
    }
}
