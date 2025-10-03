package com.vaultsys.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration loader for application properties.
 * Loads and manages configuration from properties files.
 */
public class ConfigLoader {
    
    private Properties properties;
    private static ConfigLoader instance;
    private static final String DEFAULT_CONFIG_FILE = "application.properties";
    
    /**
     * Private constructor for singleton pattern
     */
    private ConfigLoader() {
        properties = new Properties();
        loadDefaultConfig();
    }
    
    /**
     * Private constructor with custom config file
     * @param configFile Path to configuration file
     */
    private ConfigLoader(String configFile) {
        properties = new Properties();
        loadConfig(configFile);
    }
    
    /**
     * Get singleton instance with default config
     * @return ConfigLoader instance
     */
    public static synchronized ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }
    
    /**
     * Get instance with custom config file
     * @param configFile Path to configuration file
     * @return ConfigLoader instance
     */
    public static synchronized ConfigLoader getInstance(String configFile) {
        if (instance == null) {
            instance = new ConfigLoader(configFile);
        }
        return instance;
    }
    
    /**
     * Load default configuration file
     */
    private void loadDefaultConfig() {
        try {
            // Try to load from classpath
            InputStream input = getClass().getClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG_FILE);
            
            if (input != null) {
                properties.load(input);
                input.close();
            } else {
                // Try to load from file system
                loadConfig(DEFAULT_CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load default config file. Using defaults.");
            loadDefaults();
        }
    }
    
    /**
     * Load configuration from specified file
     * @param configFile Path to configuration file
     */
private void loadConfig(String configFile) {
    // Use the class loader to find the file in the classpath, 
    // which is where files in the 'resources' folder are placed.
    try (InputStream input = ConfigLoader.class.getClassLoader()
                                              .getResourceAsStream(configFile)) {
        
        if (input != null) {
            properties.load(input);
            Logger.getInstance().info("Configuration loaded from classpath: " + configFile);
        } else {
            // Log a definitive error if the file cannot be found in the classpath
            Logger.getInstance().error("Config file not found in classpath: " + configFile);
        }
    } catch (IOException ex) {
        Logger.getInstance().error("Error loading config file: " + configFile + 
                                   " - " + ex.getMessage());
    }
}
    
    /**
     * Load default values when config file is not available
     */
    private void loadDefaults() {
        // Database defaults
        properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/vaultsys");
        properties.setProperty("db.user", "postgres");
        properties.setProperty("db.password", "postgres");
        properties.setProperty("db.driver", "org.postgresql.Driver");
        properties.setProperty("db.pool.size", "10");
        
        // Application defaults
        properties.setProperty("app.name", "VaultSys");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.environment", "development");
        
        // Server defaults
        properties.setProperty("server.port", "8080");
        properties.setProperty("server.host", "localhost");
        
        // Security defaults
        properties.setProperty("security.jwt.secret", "change-this-secret-key");
        properties.setProperty("security.jwt.expiration", "86400000"); // 24 hours
        properties.setProperty("security.password.min.length", "8");
        properties.setProperty("security.password.max.length", "128");
        
        // Transaction defaults
        properties.setProperty("transaction.max.amount", "50000.00");
        properties.setProperty("transaction.daily.limit", "100000.00");
        
        // Account defaults
        properties.setProperty("account.savings.interest.rate", "0.025");
        properties.setProperty("account.savings.min.balance", "100.00");
        properties.setProperty("account.checking.overdraft.limit", "500.00");
        properties.setProperty("account.checking.transaction.fee", "0.25");
        
        // Logging defaults
        properties.setProperty("logging.level", "INFO");
        properties.setProperty("logging.file.enabled", "false");
        properties.setProperty("logging.file.path", "vaultsys.log");
    }
    
    /**
     * Get property value as String
     * @param key Property key
     * @return Property value or null
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get property value with default
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get property as integer
     * @param key Property key
     * @param defaultValue Default value if key not found or invalid
     * @return Property value as integer
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get property as long
     * @param key Property key
     * @param defaultValue Default value if key not found or invalid
     * @return Property value as long
     */
    public long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get property as double
     * @param key Property key
     * @param defaultValue Default value if key not found or invalid
     * @return Property value as double
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get property as boolean
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value as boolean
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Set property value
     * @param key Property key
     * @param value Property value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Check if property exists
     * @param key Property key
     * @return true if property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Get all properties
     * @return Properties object
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
    
    /**
     * Reload configuration from file
     * @param configFile Path to configuration file
     */
    public void reload(String configFile) {
        properties.clear();
        loadConfig(configFile);
    }
    
    /**
     * Reload default configuration
     */
    public void reload() {
        properties.clear();
        loadDefaultConfig();
    }
    
    /**
     * Get database URL
     * @return Database URL
     */
    public String getDatabaseUrl() {
        return getProperty("db.url");
    }
    
    /**
     * Get database username
     * @return Database username
     */
    public String getDatabaseUser() {
        return getProperty("db.user");
    }
    
    /**
     * Get database password
     * @return Database password
     */
    public String getDatabasePassword() {
        return getProperty("db.password");
    }
    
    /**
     * Get server port
     * @return Server port
     */
    public int getServerPort() {
        return getIntProperty("server.port", 8080);
    }
    
    /**
     * Check if running in development mode
     * @return true if development environment
     */
    public boolean isDevelopment() {
        return "development".equalsIgnoreCase(getProperty("app.environment", "development"));
    }
    
    /**
     * Check if running in production mode
     * @return true if production environment
     */
    public boolean isProduction() {
        return "production".equalsIgnoreCase(getProperty("app.environment", "development"));
    }
}