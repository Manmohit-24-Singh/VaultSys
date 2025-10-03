package com.vaultsys.exceptions;

import java.math.BigDecimal;

/**
 * Exception thrown when an account has insufficient funds for a transaction.
 * Demonstrates custom exception handling in OOP.
 */
public class InsufficientFundsException extends Exception {
    
    private BigDecimal availableBalance;
    private BigDecimal requestedAmount;
    
    /**
     * Constructor with message only
     * @param message Error message
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    /**
     * Constructor with balance details (BigDecimal)
     * @param message Error message
     * @param availableBalance Current account balance
     * @param requestedAmount Amount attempted to withdraw
     */
    public InsufficientFundsException(String message, BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(message);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }
    
    /**
     * Constructor with balance details (double) - for backward compatibility
     * @param message Error message
     * @param availableBalance Current account balance
     * @param requestedAmount Amount attempted to withdraw
     */
    public InsufficientFundsException(String message, double availableBalance, double requestedAmount) {
        super(message);
        this.availableBalance = BigDecimal.valueOf(availableBalance);
        this.requestedAmount = BigDecimal.valueOf(requestedAmount);
    }
    
    /**
     * Get available balance
     * @return available balance
     */
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    /**
     * Get requested amount
     * @return requested amount
     */
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    /**
     * Get shortage amount
     * @return how much money is short
     */
    public BigDecimal getShortfall() {
        if (requestedAmount != null && availableBalance != null) {
            return requestedAmount.subtract(availableBalance);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Get available balance as double (for backward compatibility)
     * @return available balance as double
     */
    public double getAvailableBalanceAsDouble() {
        return availableBalance != null ? availableBalance.doubleValue() : 0.0;
    }
    
    /**
     * Get requested amount as double (for backward compatibility)
     * @return requested amount as double
     */
    public double getRequestedAmountAsDouble() {
        return requestedAmount != null ? requestedAmount.doubleValue() : 0.0;
    }
    
    /**
     * Get shortfall as double (for backward compatibility)
     * @return shortfall as double
     */
    public double getShortfallAsDouble() {
        return getShortfall().doubleValue();
    }
}