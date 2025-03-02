package com.example.client.store;

import com.example.client.model.AppSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class ConfigStore {
    private static final String CONFIG_FILE_PATH = "/Users/stephenminakian/Software/WORK/ta1-ta2-interface/settings.json";
    private static volatile ConfigStore instance;
    private AppSettings settings;

    private ConfigStore() {
        loadConfiguration();
    }

    public static ConfigStore getInstance() {
        if (instance == null) {
            synchronized (ConfigStore.class) {
                if (instance == null) {
                    instance = new ConfigStore();
                }
            }
        }
        return instance;
    }

    private void loadConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            settings = mapper.readValue(new File(CONFIG_FILE_PATH), AppSettings.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration: ", e);
        }
    }

    public AppSettings getSettings() {
        return settings;
    }
}
