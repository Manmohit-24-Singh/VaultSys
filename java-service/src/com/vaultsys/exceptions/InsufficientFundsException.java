package com.vaultsys.exceptions;

/**
 * Exception thrown when an account has insufficient funds for a transaction.
 * Demonstrates custom exception handling in OOP.
 */
public class InsufficientFundsException extends Exception {
    
    private double availableBalance;
    private double requestedAmount;
    
    /**
     * Constructor with message only
     * @param message Error message
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    /**
     * Constructor with balance details
     * @param message Error message
     * @param availableBalance Current account balance
     * @param requestedAmount Amount attempted to withdraw
     */
    public InsufficientFundsException(String message, double availableBalance, double requestedAmount) {
        super(message);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }
    
    /**
     * Get available balance
     * @return available balance
     */
    public double getAvailableBalance() {
        return availableBalance;
    }
    
    /**
     * Get requested amount
     * @return requested amount
     */
    public double getRequestedAmount() {
        return requestedAmount;
    }
    
    /**
     * Get shortage amount
     * @return how much money is short
     */
    public double getShortfall() {
        return requestedAmount - availableBalance;
    }
}