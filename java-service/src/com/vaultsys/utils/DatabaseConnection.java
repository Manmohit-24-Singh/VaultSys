package com.vaultsys.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility for PostgreSQL.
 * Manages database connections using connection pooling pattern.
 */
public class DatabaseConnection {
    
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_DRIVER = "org.postgresql.Driver";
    
    // Connection pool settings
    private static final int MAX_POOL_SIZE = 10;
    private static Connection[] connectionPool = new Connection[MAX_POOL_SIZE];
    private static boolean[] connectionInUse = new boolean[MAX_POOL_SIZE];
    private static int poolIndex = 0;
    
    private static boolean initialized = false;
    
    static {
        try {
            // Load PostgreSQL JDBC driver
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
    }
    
    /**
     * Initialize database connection settings
     * @param url Database URL
     * @param user Database username
     * @param password Database password
     */
    public static void initialize(String url, String user, String password) {
        DB_URL = url;
        DB_USER = user;
        DB_PASSWORD = password;
        initialized = true;
    }
    
    /**
     * Initialize from configuration loader
     * @param config ConfigLoader instance
     */
    public static void initialize(ConfigLoader config) {
        DB_URL = config.getProperty("db.url", "jdbc:postgresql://localhost:5432/vaultsys");
        DB_USER = config.getProperty("db.user", "postgres");
        DB_PASSWORD = config.getProperty("db.password", "");
        initialized = true;
    }
    
    /**
     * Get a database connection
     * @return Database connection
     * @throws SQLException if connection fails
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            // Default initialization for development
            DB_URL = "jdbc:postgresql://localhost:5432/vaultsys";
            DB_USER = "postgres";
            DB_PASSWORD = "postgres";
            initialized = true;
        }
        
        // Try to find an available connection in pool
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            if (connectionPool[i] != null && !connectionInUse[i]) {
                if (isConnectionValid(connectionPool[i])) {
                    connectionInUse[i] = true;
                    return connectionPool[i];
                } else {
                    // Connection is stale, create new one
                    connectionPool[i] = createNewConnection();
                    connectionInUse[i] = true;
                    return connectionPool[i];
                }
            }
        }
        
        // If no available connection, create new one and add to pool
        if (poolIndex < MAX_POOL_SIZE) {
            Connection conn = createNewConnection();
            connectionPool[poolIndex] = conn;
            connectionInUse[poolIndex] = true;
            poolIndex++;
            return conn;
        }
        
        // Pool is full, create temporary connection
        return createNewConnection();
    }
    
    /**
     * Create a new database connection
     * @return New connection
     * @throws SQLException if connection fails
     */
    private static Connection createNewConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("ssl", "false");
        props.setProperty("autoReconnect", "true");
        
        Connection conn = DriverManager.getConnection(DB_URL, props);
        conn.setAutoCommit(true);
        
        return conn;
    }
    
    /**
     * Check if connection is valid
     * @param conn Connection to check
     * @return true if valid
     */
    private static boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Release a connection back to the pool
     * @param conn Connection to release
     */
    public static synchronized void releaseConnection(Connection conn) {
        if (conn == null) return;
        
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            if (connectionPool[i] == conn) {
                connectionInUse[i] = false;
                return;
            }
        }
        
        // If not in pool, close it
        try {
            conn.close();
        } catch (SQLException e) {
            // Ignore
        }
    }
    
    /**
     * Close all connections in the pool
     */
    public static synchronized void closeAllConnections() {
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            if (connectionPool[i] != null) {
                try {
                    connectionPool[i].close();
                } catch (SQLException e) {
                    // Ignore
                }
                connectionPool[i] = null;
                connectionInUse[i] = false;
            }
        }
        poolIndex = 0;
    }
    
    /**
     * Test database connection
     * @return true if connection successful
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Get connection with transaction support
     * @return Connection with auto-commit disabled
     * @throws SQLException if connection fails
     */
    public static Connection getTransactionConnection() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }
    
    /**
     * Commit transaction
     * @param conn Connection to commit
     * @throws SQLException if commit fails
     */
    public static void commit(Connection conn) throws SQLException {
        if (conn != null && !conn.getAutoCommit()) {
            conn.commit();
        }
    }
    
    /**
     * Rollback transaction
     * @param conn Connection to rollback
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                }
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Get pool statistics
     * @return String with pool info
     */
    public static String getPoolStats() {
        int active = 0;
        int idle = 0;
        
        for (int i = 0; i < poolIndex; i++) {
            if (connectionInUse[i]) {
                active++;
            } else {
                idle++;
            }
        }
        
        return String.format("Pool Stats - Total: %d, Active: %d, Idle: %d, Max: %d",
                           poolIndex, active, idle, MAX_POOL_SIZE);
    }
}