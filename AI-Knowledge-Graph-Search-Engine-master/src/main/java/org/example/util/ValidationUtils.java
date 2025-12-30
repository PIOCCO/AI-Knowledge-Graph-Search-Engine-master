package org.example.util;

import org.example.exception.ValidationException;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
    }

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    public static void validateEmail(String email) {
        validateNotEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone number format");
        }
    }

    public static void validateUsername(String username) {
        validateNotEmpty(username, "Username");
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException(
                    "Username must be 3-20 characters and contain only letters, numbers, and underscores");
        }
    }

    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        validateNotNull(value, fieldName);
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ValidationException(
                    fieldName + " must be between " + minLength + " and " + maxLength + " characters");
        }
    }

    public static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    public static void validateRange(int value, String fieldName, int min, int max) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max);
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
}
