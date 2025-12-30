package org.example.config;

public class Neo4jConfig {
    private static Neo4jConfig instance;

    private String uri;
    private String username;
    private String password;
    private int maxConnectionPoolSize;
    private long connectionTimeoutMs;
    private boolean encrypted;

    private Neo4jConfig() {
        // Default configuration
        this.uri = "bolt://localhost:7687";
        this.username = "neo4j";
        this.password = "11111111";
        this.maxConnectionPoolSize = 50;
        this.connectionTimeoutMs = 30000;
        this.encrypted = false;
    }

    public static Neo4jConfig getInstance() {
        if (instance == null) {
            instance = new Neo4jConfig();
        }
        return instance;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public void setMaxConnectionPoolSize(int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

    public long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(long connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void loadFromAppConfig() {
        AppConfig appConfig = AppConfig.getInstance();
        this.uri = appConfig.getProperty("neo4j.uri", this.uri);
        this.username = appConfig.getProperty("neo4j.username", this.username);
        this.password = appConfig.getProperty("neo4j.password", this.password);
        this.maxConnectionPoolSize = appConfig.getIntProperty("neo4j.maxPoolSize", this.maxConnectionPoolSize);
        this.connectionTimeoutMs = appConfig.getIntProperty("neo4j.timeout", (int) this.connectionTimeoutMs);
        this.encrypted = appConfig.getBooleanProperty("neo4j.encrypted", this.encrypted);
    }

    @Override
    public String toString() {
        return "Neo4jConfig{" +
                "uri='" + uri + '\'' +
                ", username='" + username + '\'' +
                ", maxConnectionPoolSize=" + maxConnectionPoolSize +
                ", connectionTimeoutMs=" + connectionTimeoutMs +
                ", encrypted=" + encrypted +
                '}';
    }
}
