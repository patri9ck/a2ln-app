package dev.patri9ck.a2ln.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.util.Storage;

public class AppSettingsFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_settings, container, false);
    }

    @Override
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        assert args != null;

        String packageName = args.getString("package_name");

        assert packageName != null;

        Context context = requireContext();
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo appInfo;

        try {
            appInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        View appDescView = view.findViewById(R.id.app_desc_view);
        TextView nameTextView = appDescView.findViewById(R.id.name_text_view);
        ImageView iconImageView = appDescView.findViewById(R.id.icon_image_view);

        nameTextView.setText(appInfo.loadLabel(packageManager));
        iconImageView.setImageDrawable(appInfo.loadIcon(packageManager));

        Storage storage = new Storage(context, context.getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE));
        List<String> disabledApps = storage.loadDisabledApps();
        List<String> contentHiddenApps = storage.loadContentHiddenApps();
        Switch enabledCheckbox = view.findViewById(R.id.app_enabled_switch);
        Switch hideContentCheckbox = view.findViewById(R.id.app_hide_content_switch);
        boolean isEnabled = !disabledApps.contains(packageName);

        enabledCheckbox.setChecked(isEnabled);
        hideContentCheckbox.setEnabled(isEnabled);
        hideContentCheckbox.setChecked(contentHiddenApps.contains(packageName));

        enabledCheckbox.setOnCheckedChangeListener((CompoundButton checkBox, boolean isChecked) -> {
            hideContentCheckbox.setEnabled(isChecked);

            if (isChecked) {
                disabledApps.add(packageName);
            } else {
                disabledApps.remove(packageName);
            }

            storage.saveDisabledApps(disabledApps);
        });

        hideContentCheckbox.setOnCheckedChangeListener((CompoundButton checkBox, boolean isChecked) -> {
            if (isChecked) {
                contentHiddenApps.add(packageName);
            } else {
                contentHiddenApps.remove(packageName);
            }

            storage.saveContentHiddenApps(contentHiddenApps);
        });
    }
}