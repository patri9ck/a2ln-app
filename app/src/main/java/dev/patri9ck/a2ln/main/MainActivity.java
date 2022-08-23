/*
 * Copyright (C) 2022 Patrick Zwick and contributors
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
package dev.patri9ck.a2ln.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.zeromq.ZCert;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.ActivityMainBinding;
import dev.patri9ck.a2ln.databinding.DialogPermissionRequestBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(activityMainBinding.getRoot());

        generateKeys();
        loadNavigationBar();
        requestPermission();
    }

    private void generateKeys() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        if (sharedPreferences.contains(getString(R.string.preferences_own_public_key)) && sharedPreferences.contains(getString(R.string.preferences_own_secret_key))) {
            return;
        }

        ZCert zCert = new ZCert();

        sharedPreferences.edit()
                .putString(getString(R.string.preferences_own_public_key), zCert.getPublicKeyAsZ85())
                .putString(getString(R.string.preferences_own_secret_key), zCert.getSecretKeyAsZ85())
                .apply();
    }

    private void loadNavigationBar() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment_container_view);

        if (navHostFragment == null) {
            return;
        }

        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, new AppBarConfiguration.Builder(R.id.navigation_servers, R.id.navigation_apps, R.id.navigation_settings)
                .build());
        NavigationUI.setupWithNavController(activityMainBinding.mainBottomNavigationView, navController);
    }

    private void requestPermission() {
        if (NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
            return;
        }

        DialogPermissionRequestBinding dialogPermissionRequestBinding = DialogPermissionRequestBinding.inflate(getLayoutInflater());

        dialogPermissionRequestBinding.permissionRequestTextView.setText(R.string.permission_request_dialog_information);

        new MaterialAlertDialogBuilder(this, R.style.Dialog)
                .setTitle(R.string.permission_request_dialog_title)
                .setView(dialogPermissionRequestBinding.getRoot())
                .setPositiveButton(R.string.grant, (requestPermissionDialog, which) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)))
                .setNegativeButton(R.string.deny, null)
                .show();
    }
}
