package com.vaultsys.exceptions;

/**
 * Exception thrown when authentication or authorization fails.
 * Used for login failures, invalid credentials, or unauthorized access attempts.
 */
public class AuthenticationException extends Exception {
    
    private String username;
    private AuthenticationFailureType failureType;
    
    /**
     * Enum for different types of authentication failures
     */
    public enum AuthenticationFailureType {
        INVALID_CREDENTIALS,
        ACCOUNT_LOCKED,
        ACCOUNT_DISABLED,
        SESSION_EXPIRED,
        UNAUTHORIZED_ACCESS
    }
    
    /**
     * Constructor with message only
     * @param message Error message
     */
    public AuthenticationException(String message) {
        super(message);
        this.failureType = AuthenticationFailureType.INVALID_CREDENTIALS;
    }
    
    /**
     * Constructor with authentication details
     * @param message Error message
     * @param username Username that failed authentication
     * @param failureType Type of authentication failure
     */
    public AuthenticationException(String message, String username, AuthenticationFailureType failureType) {
        super(message);
        this.username = username;
        this.failureType = failureType;
    }
    
    /**
     * Get username
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Get failure type
     * @return authentication failure type
     */
    public AuthenticationFailureType getFailureType() {
        return failureType;
    }
}