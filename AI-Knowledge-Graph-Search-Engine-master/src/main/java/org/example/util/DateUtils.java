package org.example.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : "";
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "";
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public static long getMinutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static long getHoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long getDaysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = getMinutesBetween(dateTime, now);

        if (minutes < 1)
            return "just now";
        if (minutes < 60)
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";

        long hours = minutes / 60;
        if (hours < 24)
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";

        long days = hours / 24;
        if (days < 30)
            return days + " day" + (days > 1 ? "s" : "") + " ago";

        long months = days / 30;
        if (months < 12)
            return months + " month" + (months > 1 ? "s" : "") + " ago";

        long years = months / 12;
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }

    public static boolean isOverdue(LocalDateTime deadline) {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, int minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, int hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    public static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        return dateTime != null ? dateTime.plusDays(days) : null;
    }
}
