package dev.patri9ck.a2ln.app;

import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.notification.BoundNotificationReceiver;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {

    private List<String> disabledApps;
    private BoundNotificationReceiver boundNotificationReceiver;

    private List<App> apps;

    public AppsAdapter(List<String> disabledApps, BoundNotificationReceiver boundNotificationReceiver, PackageManager packageManager) {
        this.disabledApps = disabledApps;
        this.boundNotificationReceiver = boundNotificationReceiver;

        apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .stream()
                .filter(applicationInfo -> packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null)
                .map(applicationInfo -> new App(applicationInfo.loadLabel(packageManager).toString(), applicationInfo.packageName, applicationInfo.loadIcon(packageManager), !disabledApps.contains(applicationInfo.packageName)))
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false));
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        App app = apps.get(position);

        holder.nameTextView.setText(app.getName());

        holder.appCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
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

        holder.iconImageView.setImageDrawable(app.getIcon());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    protected static class AppViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private CheckBox appCheckBox;
        private ImageView iconImageView;

        public AppViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.name_text_view);
            appCheckBox = itemView.findViewById(R.id.app_check_box);
            iconImageView = itemView.findViewById(R.id.icon_image_view);
        }
    }
}
