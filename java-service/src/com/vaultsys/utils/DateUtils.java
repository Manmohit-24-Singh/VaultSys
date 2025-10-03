package com.vaultsys.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Utility class for date and time operations.
 * Provides helper methods for date formatting, parsing, and calculations.
 */
public class DateUtils {
    
    // Common date formats
    public static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMAT = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    public static final DateTimeFormatter ISO_FORMAT = 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Get current date and time
     * @return Current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * Get current date
     * @return Current LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    /**
     * Format LocalDateTime to string
     * @param dateTime LocalDateTime to format
     * @return Formatted string
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMAT);
    }
    
    /**
     * Format LocalDateTime with custom format
     * @param dateTime LocalDateTime to format
     * @param formatter DateTimeFormatter to use
     * @return Formatted string
     */
    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) return "";
        return dateTime.format(formatter);
    }
    
    /**
     * Format LocalDate to string
     * @param date LocalDate to format
     * @return Formatted string
     */
    public static String format(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT);
    }
    
    /**
     * Format LocalDate with custom format
     * @param date LocalDate to format
     * @param formatter DateTimeFormatter to use
     * @return Formatted string
     */
    public static String format(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) return "";
        return date.format(formatter);
    }
    
    /**
     * Parse string to LocalDateTime
     * @param dateTimeStr String to parse
     * @return LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parse string to LocalDate
     * @param dateStr String to parse
     * @return LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Convert LocalDateTime to SQL Timestamp
     * @param dateTime LocalDateTime to convert
     * @return SQL Timestamp
     */
    public static Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return Timestamp.valueOf(dateTime);
    }
    
    /**
     * Convert SQL Timestamp to LocalDateTime
     * @param timestamp SQL Timestamp to convert
     * @return LocalDateTime
     */
    public static LocalDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime();
    }
    
    /**
     * Convert LocalDateTime to java.util.Date
     * @param dateTime LocalDateTime to convert
     * @return java.util.Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Convert java.util.Date to LocalDateTime
     * @param date java.util.Date to convert
     * @return LocalDateTime
     */
    public static LocalDateTime fromDate(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
    
    /**
     * Add days to a date
     * @param date Base date
     * @param days Number of days to add (can be negative)
     * @return New LocalDate
     */
    public static LocalDate addDays(LocalDate date, int days) {
        if (date == null) return null;
        return date.plusDays(days);
    }
    
    /**
     * Add days to a datetime
     * @param dateTime Base datetime
     * @param days Number of days to add (can be negative)
     * @return New LocalDateTime
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        if (dateTime == null) return null;
        return dateTime.plusDays(days);
    }
    
    /**
     * Add months to a date
     * @param date Base date
     * @param months Number of months to add (can be negative)
     * @return New LocalDate
     */
    public static LocalDate addMonths(LocalDate date, int months) {
        if (date == null) return null;
        return date.plusMonths(months);
    }
    
    /**
     * Add years to a date
     * @param date Base date
     * @param years Number of years to add (can be negative)
     * @return New LocalDate
     */
    public static LocalDate addYears(LocalDate date, int years) {
        if (date == null) return null;
        return date.plusYears(years);
    }
    
    /**
     * Calculate days between two dates
     * @param start Start date
     * @param end End date
     * @return Number of days between dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * Calculate days between two datetimes
     * @param start Start datetime
     * @param end End datetime
     * @return Number of days between datetimes
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * Calculate months between two dates
     * @param start Start date
     * @param end End date
     * @return Number of months between dates
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.MONTHS.between(start, end);
    }
    
    /**
     * Calculate years between two dates
     * @param start Start date
     * @param end End date
     * @return Number of years between dates
     */
    public static long yearsBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.YEARS.between(start, end);
    }
    
    /**
     * Check if a date is in the past
     * @param date Date to check
     * @return true if date is in the past
     */
    public static boolean isPast(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Check if a datetime is in the past
     * @param dateTime DateTime to check
     * @return true if datetime is in the past
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * Check if a date is in the future
     * @param date Date to check
     * @return true if date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        if (date == null) return false;
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * Check if a datetime is in the future
     * @param dateTime DateTime to check
     * @return true if datetime is in the future
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Check if a date is today
     * @param date Date to check
     * @return true if date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.equals(LocalDate.now());
    }
    
    /**
     * Get start of day for a datetime
     * @param dateTime DateTime to process
     * @return DateTime at start of day (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalDate().atStartOfDay();
    }
    
    /**
     * Get end of day for a datetime
     * @param dateTime DateTime to process
     * @return DateTime at end of day (23:59:59)
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalDate().atTime(23, 59, 59);
    }
    
    /**
     * Get start of month for a date
     * @param date Date to process
     * @return First day of the month
     */
    public static LocalDate startOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }
    
    /**
     * Get end of month for a date
     * @param date Date to process
     * @return Last day of the month
     */
    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    
    /**
     * Get start of year for a date
     * @param date Date to process
     * @return First day of the year
     */
    public static LocalDate startOfYear(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.firstDayOfYear());
    }
    
    /**
     * Get end of year for a date
     * @param date Date to process
     * @return Last day of the year
     */
    public static LocalDate endOfYear(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.lastDayOfYear());
    }
    
    /**
     * Check if date is within a range
     * @param date Date to check
     * @param start Start of range (inclusive)
     * @param end End of range (inclusive)
     * @return true if date is within range
     */
    public static boolean isInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) return false;
        return !date.isBefore(start) && !date.isAfter(end);
    }
    
    /**
     * Check if datetime is within a range
     * @param dateTime DateTime to check
     * @param start Start of range (inclusive)
     * @param end End of range (inclusive)
     * @return true if datetime is within range
     */
    public static boolean isInRange(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) return false;
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
    
    /**
     * Get age from birthdate
     * @param birthDate Birth date
     * @return Age in years
     */
    public static int getAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }
    
    /**
     * Format duration in human-readable format
     * @param start Start datetime
     * @param end End datetime
     * @return Formatted duration string (e.g., "2 hours, 30 minutes")
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";
        
        Duration duration = Duration.between(start, end);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append(" day").append(days > 1 ? "s" : "").append(", ");
        if (hours > 0) result.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(", ");
        if (minutes > 0) result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        
        String str = result.toString();
        if (str.endsWith(", ")) str = str.substring(0, str.length() - 2);
        
        return str.isEmpty() ? "0 minutes" : str;
    }
}