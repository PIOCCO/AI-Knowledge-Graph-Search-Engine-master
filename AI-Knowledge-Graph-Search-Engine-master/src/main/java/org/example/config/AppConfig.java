package org.example.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized application configuration
 * Loads from application.properties
 */
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
                System.out.println("Configuration loaded from " + CONFIG_FILE);
            } else {
                System.err.println("Warning: " + CONFIG_FILE + " not found, using defaults");
                setDefaultProperties();
            }
        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        properties.setProperty("app.name", "AI Knowledge Graph Search Engine");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("neo4j.uri", "bolt://127.0.0.1:7687");
        properties.setProperty("neo4j.username", "neo4j");
        properties.setProperty("neo4j.password", "00000000"); // default password
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getAppName() {
        return getProperty("app.name", "AI Knowledge Graph");
    }

    public Properties getProperties() {
        return properties;
    }

    public String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }
}
