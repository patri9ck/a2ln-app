package dev.patri9ck.a2ln.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.zeromq.ZCert;

import java.util.Base64;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        generateCertificates();

        loadNavBar();
    }

    private void loadNavBar() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment_container_view);

        if (navHostFragment == null) {
            return;
        }

        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, new AppBarConfiguration.Builder(R.id.navigation_devices, R.id.navigation_apps, R.id.navigation_settings)
                .build());
        NavigationUI.setupWithNavController(binding.mainNavigationView, navController);
    }

    private void generateCertificates() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        if (sharedPreferences.contains(getString(R.string.preferences_client_public_key)) && sharedPreferences.contains(getString(R.string.preferences_client_secret_key))) {
            return;
        }

        ZCert zCert = new ZCert();

        sharedPreferences.edit()
                .putString(getString(R.string.preferences_client_public_key), Base64.getEncoder().encodeToString(zCert.getPublicKey()))
                .putString(getString(R.string.preferences_client_secret_key), Base64.getEncoder().encodeToString(zCert.getSecretKey()))
                .apply();
    }
}
