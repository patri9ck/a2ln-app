/*
 * Android 2 Linux Notifications - A way to display Android phone notifications on Linux
 * Copyright (C) 2023  patri9ck and contributors
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

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.FragmentAppsBinding;
import dev.patri9ck.a2ln.util.Storage;

public class AppsFragment extends Fragment {

    private Storage storage;
    private List<String> disabledApps;
    private FragmentAppsBinding fragmentAppsBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        storage = new Storage(requireContext(), requireContext().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE));
        disabledApps = storage.loadDisabledApps();
        fragmentAppsBinding = FragmentAppsBinding.inflate(inflater, container, false);

        loadAppsRecyclerView();

        return fragmentAppsBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        fragmentAppsBinding = null;
    }

    private void loadAppsRecyclerView() {
        fragmentAppsBinding.loadingProgressIndicator.setVisibility(View.VISIBLE);

        PackageManager packageManager = requireContext().getPackageManager();

        CompletableFuture.supplyAsync(() -> packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                        .stream()
                        .filter(applicationInfo -> packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null)
                        .map(applicationInfo -> new App(applicationInfo.loadLabel(packageManager).toString(), applicationInfo.packageName, applicationInfo.loadIcon(packageManager), !disabledApps.contains(applicationInfo.packageName)))
                        .sorted(Comparator.comparing(App::isEnabled).thenComparing(App::getName))
                        .collect(Collectors.toList()))
                .thenAccept(apps -> requireActivity().runOnUiThread(() -> {
                    fragmentAppsBinding.loadingProgressIndicator.setVisibility(View.INVISIBLE);

                    fragmentAppsBinding.appsRecyclerView.setAdapter(new AppsAdapter(disabledApps, apps, storage));
                    fragmentAppsBinding.appsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                }));
    }
}
