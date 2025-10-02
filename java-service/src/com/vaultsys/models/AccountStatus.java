package com.vaultsys.models;

/**
 * Enum representing the different status states for accounts in the VaultSys banking system.
 * Each status defines what operations are allowed on the account.
 */
public enum AccountStatus {
    ACTIVE("Active", true, true, "Account is active and fully operational"),
    SUSPENDED("Suspended", false, false, "Account is temporarily suspended"),
    CLOSED("Closed", false, false, "Account has been permanently closed"),
    PENDING("Pending", false, false, "Account is pending activation"),
    FROZEN("Frozen", true, false, "Account is frozen - deposits allowed, withdrawals blocked"),
    DORMANT("Dormant", true, true, "Account is inactive but operational"),
    RESTRICTED("Restricted", true, false, "Account has restrictions on withdrawals"),
    UNDER_REVIEW("Under Review", false, false, "Account is under review for compliance");

    private final String displayName;
    private final boolean allowsDeposits;
    private final boolean allowsWithdrawals;
    private final String description;

    AccountStatus(String displayName, boolean allowsDeposits, 
                  boolean allowsWithdrawals, String description) {
        this.displayName = displayName;
        this.allowsDeposits = allowsDeposits;
        this.allowsWithdrawals = allowsWithdrawals;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean allowsDeposits() {
        return allowsDeposits;
    }

    public boolean allowsWithdrawals() {
        return allowsWithdrawals;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the account is fully operational
     * @return true if both deposits and withdrawals are allowed
     */
    public boolean isFullyOperational() {
        return allowsDeposits && allowsWithdrawals;
    }

    /**
     * Check if the account is in a terminal state (cannot be reactivated easily)
     * @return true if the account is closed or in a permanent state
     */
    public boolean isTerminalState() {
        return this == CLOSED;
    }

    /**
     * Check if transactions are completely blocked
     * @return true if neither deposits nor withdrawals are allowed
     */
    public boolean isCompletelyBlocked() {
        return !allowsDeposits && !allowsWithdrawals;
    }

    /**
     * Check if the account can be activated
     * @return true if the account can transition to ACTIVE status
     */
    public boolean canBeActivated() {
        return this == PENDING || this == SUSPENDED || this == DORMANT;
    }

    /**
     * Get the account status from a string value
     * @param value The string value to parse
     * @return The corresponding AccountStatus, or null if not found
     */
    public static AccountStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (AccountStatus status : AccountStatus.values()) {
            if (status.name().equalsIgnoreCase(value) || 
                status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Validate if a transaction type is allowed for this status
     * @param transactionType The type of transaction to validate
     * @return true if the transaction is allowed for this account status
     */
    public boolean allowsTransaction(TransactionType transactionType) {
        if (transactionType == null) {
            return false;
        }

        // Check if deposits are allowed
        if (transactionType.increasesBalance()) {
            return allowsDeposits;
        }

        // Check if withdrawals are allowed
        if (transactionType.decreasesBalance()) {
            return allowsWithdrawals;
        }

        return false;
    }

    /**
     * Get a user-friendly message explaining why a transaction might be blocked
     * @return A message explaining the account status restrictions
     */
    public String getRestrictionMessage() {
        if (isFullyOperational()) {
            return "Account is fully operational.";
        }
        
        if (isCompletelyBlocked()) {
            return "Account is " + displayName.toLowerCase() + 
                   " and all transactions are blocked.";
        }
        
        if (allowsDeposits && !allowsWithdrawals) {
            return "Account is " + displayName.toLowerCase() + 
                   ". Deposits are allowed but withdrawals are blocked.";
        }
        
        if (!allowsDeposits && allowsWithdrawals) {
            return "Account is " + displayName.toLowerCase() + 
                   ". Withdrawals are allowed but deposits are blocked.";
        }
        
        return "Account status: " + displayName;
    }

    /**
     * Get the color code for UI display (useful for frontend)
     * @return A color code representing the status severity
     */
    public String getColorCode() {
        switch (this) {
            case ACTIVE:
            case DORMANT:
                return "GREEN";
            case PENDING:
            case UNDER_REVIEW:
                return "YELLOW";
            case SUSPENDED:
            case FROZEN:
            case RESTRICTED:
                return "ORANGE";
            case CLOSED:
                return "RED";
            default:
                return "GRAY";
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}