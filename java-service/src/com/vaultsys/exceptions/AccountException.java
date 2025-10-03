package com.vaultsys.exceptions;

/**
 * Exception thrown when account operations fail.
 * Used for general account-related errors that are not specifically
 * about account not being found.
 */
public class AccountException extends Exception {
    
    /**
     * Constructor with message only
     * @param message Error message
     */
    public AccountException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message Error message
     * @param cause The underlying cause
     */
    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }
}