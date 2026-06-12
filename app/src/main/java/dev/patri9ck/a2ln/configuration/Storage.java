package dev.patri9ck.a2ln.configuration;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Storage {

    private static final String FILE_NAME = "configuration.json";

    private Gson gson = new Gson();

    private File file;

    public Storage(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public Configuration loadConfiguration() {
        try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(fileReader); Stream<String> lines = bufferedReader.lines()) {
            return gson.fromJson(lines.collect(Collectors.joining("\n")), Configuration.class);
        } catch (IOException exception) {
            Log.e("A2LN", "Failed to load configuration", exception);
        }

        return new Configuration(new ArrayList<>(), new ArrayList<>());
    }

    public void saveConfiguration(Configuration configuration) {
        try (FileWriter fileWriter = new FileWriter(file);) {
            fileWriter.write(gson.toJson(configuration));
        } catch (IOException exception) {
            Log.e("A2LN", "Failed to save configuration", exception);
        }
    }
}
