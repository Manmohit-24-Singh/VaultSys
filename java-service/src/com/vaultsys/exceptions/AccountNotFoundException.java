package com.vaultsys.exceptions;

/**
 * Exception thrown when an account cannot be found in the system.
 * Used for database lookup failures.
 */
public class AccountNotFoundException extends Exception {
    
    private String accountNumber;
    
    /**
     * Constructor with message only
     * @param message Error message
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor with account details
     * @param message Error message
     * @param accountNumber Account number that was not found
     */
    public AccountNotFoundException(String message, String accountNumber) {
        super(message);
        this.accountNumber = accountNumber;
    }
    
    /**
     * Get the account number that was not found
     * @return account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }
}