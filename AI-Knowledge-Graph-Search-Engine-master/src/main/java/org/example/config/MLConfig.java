package org.example.config;

public class MLConfig {
    private static MLConfig instance;
    private final AppConfig appConfig;

    private MLConfig() {
        this.appConfig = AppConfig.getInstance();
    }

    public static MLConfig getInstance() {
        if (instance == null) {
            instance = new MLConfig();
        }
        return instance;
    }

    public String getServiceUrl() {
        return appConfig.getProperty("ml.service.url", "http://localhost:5000");
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(
                appConfig.getProperty("ml.service.enabled", "true")
        );
    }

    public int getTimeout() {
        return Integer.parseInt(
                appConfig.getProperty("ml.service.timeout", "10000")
        );
    }

    public double getConfidenceThreshold() {
        return Double.parseDouble(
                appConfig.getProperty("ticket.confidence.threshold", "0.75")
        );
    }

    public boolean isAutoClassifyEnabled() {
        return Boolean.parseBoolean(
                appConfig.getProperty("ticket.auto.classify", "true")
        );
    }
}