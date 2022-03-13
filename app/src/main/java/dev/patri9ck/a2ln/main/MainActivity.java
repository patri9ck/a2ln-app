package dev.patri9ck.a2ln.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.gson.Gson;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.configuration.Configuration;
import dev.patri9ck.a2ln.configuration.Storage;
import dev.patri9ck.a2ln.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static Configuration configuration;

    private Gson gson = new Gson();
    private Storage storage;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = new Storage(this);
        configuration = gson.fromJson(getIntent().getStringExtra(Configuration.class.getName()), Configuration.class);

        if (configuration == null) {
            configuration = storage.loadConfiguration();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        loadNavBar();
    }

    private void loadNavBar() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment == null) {
            return;
        }

        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, new AppBarConfiguration.Builder(R.id.navigation_devices, R.id.navigation_apps, R.id.navigation_settings)
                .build());
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    protected void onStop() {
        super.onStop();

        storage.saveConfiguration(configuration);
    }
}
