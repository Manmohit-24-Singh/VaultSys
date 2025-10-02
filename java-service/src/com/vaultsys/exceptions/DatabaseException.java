package com.vaultsys.exceptions;
import java.sql.SQLException;

/**
 * Exception thrown when database operations fail.
 * Wraps SQLException and provides additional context for database errors.
 */
public class DatabaseException extends Exception {
    
    private String operation;
    private SQLException sqlException;
    
    /**
     * Constructor with message only
     * @param message Error message
     */
    public DatabaseException(String message) {
        super(message);
    }
    
    /**
     * Constructor with cause
     * @param message Error message
     * @param cause Original exception
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof SQLException) {
            this.sqlException = (SQLException) cause;
        }
    }
    
    /**
     * Constructor with operation details
     * @param message Error message
     * @param operation Database operation that failed (e.g., "INSERT", "UPDATE")
     * @param cause Original SQLException
     */
    public DatabaseException(String message, String operation, SQLException cause) {
        super(message, cause);
        this.operation = operation;
        this.sqlException = cause;
    }
    
    /**
     * Get database operation that failed
     * @return operation name
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Get SQL error code if available
     * @return SQL error code or -1 if not available
     */
    public int getSqlErrorCode() {
        return sqlException != null ? sqlException.getErrorCode() : -1;
    }
    
    /**
     * Get SQL state if available
     * @return SQL state or null if not available
     */
    public String getSqlState() {
        return sqlException != null ? sqlException.getSQLState() : null;
    }
    
    /**
     * Get the underlying SQLException
     * @return SQLException or null
     */
    public SQLException getSqlException() {
        return sqlException;
    }
}