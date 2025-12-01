package com.vaultsys;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Connection Helper
 * Demonstrates externalized configuration (no hardcoded credentials)
 * Loads database settings from db.properties file
 */
public class DatabaseHelper {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    // Static initialization block - runs once when class is loaded
    // Difference between static vs instance initialization
    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration from db.properties", e);
        }
    }

    /**
     * Creates a new database connection
     * Every call creates a new connection (no pooling in this simple version)
     * In production, you'd use connection pooling (HikariCP, C3P0) for better
     * performance
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
