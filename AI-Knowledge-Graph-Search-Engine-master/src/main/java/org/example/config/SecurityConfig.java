package org.example.config;

public class SecurityConfig {
    private static SecurityConfig instance;

    private int passwordMinLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;
    private int maxLoginAttempts;
    private long lockoutDurationMs;
    private long sessionDurationMs;
    private boolean enableTwoFactor;

    private SecurityConfig() {
        // Default security settings
        this.passwordMinLength = 8;
        this.requireUppercase = true;
        this.requireLowercase = true;
        this.requireDigit = true;
        this.requireSpecialChar = true;
        this.maxLoginAttempts = 5;
        this.lockoutDurationMs = 900000; // 15 minutes
        this.sessionDurationMs = 3600000; // 1 hour
        this.enableTwoFactor = false;
    }

    public static SecurityConfig getInstance() {
        if (instance == null) {
            instance = new SecurityConfig();
        }
        return instance;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public void setRequireLowercase(boolean requireLowercase) {
        this.requireLowercase = requireLowercase;
    }

    public boolean isRequireDigit() {
        return requireDigit;
    }

    public void setRequireDigit(boolean requireDigit) {
        this.requireDigit = requireDigit;
    }

    public boolean isRequireSpecialChar() {
        return requireSpecialChar;
    }

    public void setRequireSpecialChar(boolean requireSpecialChar) {
        this.requireSpecialChar = requireSpecialChar;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    public long getLockoutDurationMs() {
        return lockoutDurationMs;
    }

    public void setLockoutDurationMs(long lockoutDurationMs) {
        this.lockoutDurationMs = lockoutDurationMs;
    }

    public long getSessionDurationMs() {
        return sessionDurationMs;
    }

    public void setSessionDurationMs(long sessionDurationMs) {
        this.sessionDurationMs = sessionDurationMs;
    }

    public boolean isEnableTwoFactor() {
        return enableTwoFactor;
    }

    public void setEnableTwoFactor(boolean enableTwoFactor) {
        this.enableTwoFactor = enableTwoFactor;
    }

    public String getPasswordRequirements() {
        StringBuilder requirements = new StringBuilder(
                "Password must be at least " + passwordMinLength + " characters");
        if (requireUppercase)
            requirements.append(", contain uppercase letter");
        if (requireLowercase)
            requirements.append(", contain lowercase letter");
        if (requireDigit)
            requirements.append(", contain digit");
        if (requireSpecialChar)
            requirements.append(", contain special character");
        return requirements.toString();
    }

    @Override
    public String toString() {
        return "SecurityConfig{" +
                "passwordMinLength=" + passwordMinLength +
                ", maxLoginAttempts=" + maxLoginAttempts +
                ", sessionDurationMs=" + sessionDurationMs +
                ", enableTwoFactor=" + enableTwoFactor +
                '}';
    }
}
