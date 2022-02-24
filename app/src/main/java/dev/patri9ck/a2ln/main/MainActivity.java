package dev.patri9ck.a2ln.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.gson.Gson;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.configuration.Configuration;
import dev.patri9ck.a2ln.configuration.Storage;
import dev.patri9ck.a2ln.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private Gson gson = new Gson();

    private Storage storage;
    public static Configuration configuration;

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

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_devices, R.id.navigation_apps, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    protected void onStop() {
        super.onStop();

        storage.saveConfiguration(configuration);
    }
}