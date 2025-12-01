package com.vaultsys; // Declares this class belongs to the com.vaultsys package

// Import Java SQL classes for database connectivity
import java.sql.Connection; // Represents a database connection session
import java.sql.DriverManager; // Factory for creating database connections
import java.sql.Statement; // Used to execute SQL queries

/**
 * Database Connection and Schema Management Class
 * Handles PostgreSQL connectivity and table initialization
 * This uses a simple factory pattern for connection management
 */
public class Database {
    // Database connection constants - stored as final to prevent modification
    private static final String URL = "jdbc:postgresql://localhost:5432/vaultsys_student"; // JDBC connection string
    private static final String USER = "student"; // Database username
    private static final String PASS = "student"; // Database password (in production, use environment variables!)

    /**
     * Factory method to create database connections
     * Throws Exception to let caller handle connection failures
     * 
     * @return Active Connection object to PostgreSQL database
     * @throws Exception if connection fails (e.g., wrong credentials, DB down)
     */
    public static Connection connect() throws Exception {
        // DriverManager.getConnection() establishes TCP connection to PostgreSQL
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Initialize database schema (creates tables if they don't exist)
     * Uses "CREATE IF NOT EXISTS" for idempotency - safe to run
     * multiple times
     */
    public static void init() {
        // Try-with-resources: Auto-closes Connection and Statement when done (prevents
        // resource leaks)
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            // Create users table with SERIAL (auto-increment) primary key
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id SERIAL PRIMARY KEY, " + // Auto-incrementing unique ID
                            "username TEXT UNIQUE, " + // Unique username constraint
                            "password TEXT, " + // Password (should be hashed in production!)
                            "balance DECIMAL(15,2) DEFAULT 0.00, " + // Money with 2 decimal precision
                            "is_frozen BOOLEAN DEFAULT FALSE, " + // Account freeze flag for admin control
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"); // Account creation timestamp

            // Create transactions table for audit trail
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS transactions (" +
                            "id SERIAL PRIMARY KEY, " + // Unique transaction ID
                            "user_id INT, " + // Foreign key to users.id (not enforced here)
                            "type TEXT, " + // Transaction type (DEPOSIT, WITHDRAW, etc.)
                            "amount DECIMAL(15,2), " + // Transaction amount
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"); // When transaction occurred

            System.out.println("✅ Database Connected & Ready."); // Success indicator

        } catch (Exception e) {
            e.printStackTrace(); // Print error stack trace for debugging
        }
    }
}