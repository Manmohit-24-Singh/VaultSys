package com.vaultsys.exceptions;

/**
 * Exception thrown when a transaction fails validation rules.
 * Examples: negative amounts, invalid account states, business rule violations.
 */
public class InvalidTransactionException extends Exception {
    
    private String transactionId;
    private String reason;
    
    /**
     * Constructor with message only
     * @param message Error message
     */
    public InvalidTransactionException(String message) {
        super(message);
    }
    
    /**
     * Constructor with transaction details
     * @param message Error message
     * @param transactionId Transaction ID that failed
     * @param reason Specific reason for failure
     */
    public InvalidTransactionException(String message, String transactionId, String reason) {
        super(message);
        this.transactionId = transactionId;
        this.reason = reason;
    }
    
    /**
     * Get transaction ID
     * @return transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }
    
    /**
     * Get failure reason
     * @return reason for failure
     */
    public String getReason() {
        return reason;
    }
}