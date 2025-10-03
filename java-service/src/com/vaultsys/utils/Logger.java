package com.vaultsys.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Singleton logger utility for VaultSys application.
 * Provides logging functionality with different severity levels.
 */
public class Logger {
    
    private static Logger instance;
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private boolean consoleLoggingEnabled = true;
    private boolean fileLoggingEnabled = false;
    private String logFilePath = "vaultsys.log";
    
    // Log levels
    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }
    
    private Level currentLevel = Level.INFO;
    
    // Private constructor for singleton
    private Logger() {}
    
    /**
     * Get singleton instance of Logger
     * @return Logger instance
     */
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    /**
     * Log debug message
     * @param message The message to log
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    /**
     * Log info message
     * @param message The message to log
     */
    public void info(String message) {
        log(Level.INFO, message);
    }
    
    /**
     * Log warning message
     * @param message The message to log
     */
    public void warn(String message) {
        log(Level.WARN, message);
    }
    
    /**
     * Log error message
     * @param message The message to log
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }
    
    /**
     * Log error message with exception
     * @param message The message to log
     * @param throwable The exception
     */
    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message + " - " + throwable.getMessage());
        if (throwable.getStackTrace() != null && throwable.getStackTrace().length > 0) {
            log(Level.ERROR, "Stack trace: " + throwable.getStackTrace()[0].toString());
        }
    }
    
    /**
     * Core logging method
     * @param level Log level
     * @param message Message to log
     */
    private void log(Level level, String message) {
        if (level.ordinal() < currentLevel.ordinal()) {
            return; // Skip if below current log level
        }
        
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // Console logging
        if (consoleLoggingEnabled) {
            if (level == Level.ERROR || level == Level.WARN) {
                System.err.println(logMessage);
            } else {
                System.out.println(logMessage);
            }
        }
        
        // File logging
        if (fileLoggingEnabled) {
            writeToFile(logMessage);
        }
    }
    
    /**
     * Write log message to file
     * @param message The message to write
     */
    private void writeToFile(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            writer.println(message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    // Configuration methods
    
    public void setConsoleLoggingEnabled(boolean enabled) {
        this.consoleLoggingEnabled = enabled;
    }
    
    public void setFileLoggingEnabled(boolean enabled) {
        this.fileLoggingEnabled = enabled;
    }
    
    public void setLogFilePath(String filePath) {
        this.logFilePath = filePath;
    }
    
    public void setLogLevel(Level level) {
        this.currentLevel = level;
    }
    
    public Level getLogLevel() {
        return currentLevel;
    }
}