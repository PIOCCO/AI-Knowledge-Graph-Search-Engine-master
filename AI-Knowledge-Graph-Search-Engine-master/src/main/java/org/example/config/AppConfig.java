package org.example.config;

import java.io.*;
import java.util.Properties;

public class AppConfig {
    private static AppConfig instance;
    private Properties properties;
    private static final String CONFIG_FILE = "application.properties";

    private AppConfig() {
        properties = new Properties();
        loadConfig();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                // Load default properties
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        properties.setProperty("app.name", "AI Knowledge Graph Search Engine");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.theme", "light");
        properties.setProperty("app.language", "en");
        properties.setProperty("app.maxUploadSize", "10485760"); // 10MB
        properties.setProperty("app.sessionTimeout", "3600"); // 1 hour
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Application Configuration");
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
        }
    }

    public String getAppName() {
        return getProperty("app.name", "AI Knowledge Graph");
    }

    public String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }

    public String getTheme() {
        return getProperty("app.theme", "light");
    }

    public void setTheme(String theme) {
        setProperty("app.theme", theme);
    }

    public int getMaxUploadSize() {
        return getIntProperty("app.maxUploadSize", 10485760);
    }

    public int getSessionTimeout() {
        return getIntProperty("app.sessionTimeout", 3600);
    }
}
