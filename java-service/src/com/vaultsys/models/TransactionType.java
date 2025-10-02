package com.vaultsys.models;

/**
 * Enum representing the different types of transactions in the VaultSys banking system.
 * Each type has a description and indicates whether it increases or decreases the account balance.
 */
public enum TransactionType {
    DEPOSIT("Deposit", true, "Funds added to account"),
    WITHDRAWAL("Withdrawal", false, "Funds removed from account"),
    TRANSFER("Transfer", false, "Funds transferred to another account"),
    TRANSFER_RECEIVED("Transfer Received", true, "Funds received from another account"),
    INTEREST("Interest", true, "Interest credited to account"),
    FEE("Fee", false, "Service or transaction fee charged"),
    REFUND("Refund", true, "Refund or reversal of previous transaction"),
    PAYMENT("Payment", false, "Bill payment or external payment"),
    ATM_WITHDRAWAL("ATM Withdrawal", false, "Cash withdrawal from ATM"),
    CHECK_DEPOSIT("Check Deposit", true, "Check deposited to account"),
    WIRE_TRANSFER("Wire Transfer", false, "Wire transfer to external account"),
    WIRE_RECEIVED("Wire Received", true, "Wire transfer received from external source"),
    DIRECT_DEPOSIT("Direct Deposit", true, "Direct deposit (payroll, etc.)"),
    OVERDRAFT_FEE("Overdraft Fee", false, "Fee for overdraft usage"),
    MAINTENANCE_FEE("Maintenance Fee", false, "Monthly maintenance fee"),
    REVERSAL("Reversal", true, "Transaction reversal or correction"),
    ADJUSTMENT("Adjustment", true, "Account balance adjustment");

    private final String displayName;
    private final boolean increasesBalance;
    private final String description;

    TransactionType(String displayName, boolean increasesBalance, String description) {
        this.displayName = displayName;
        this.increasesBalance = increasesBalance;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean increasesBalance() {
        return increasesBalance;
    }

    public boolean decreasesBalance() {
        return !increasesBalance;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the transaction type from a string value
     * @param value The string value to parse
     * @return The corresponding TransactionType, or null if not found
     */
    public static TransactionType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (TransactionType type : TransactionType.values()) {
            if (type.name().equalsIgnoreCase(value) || 
                type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if this transaction type requires a destination account
     * @return true if the transaction requires a toAccountId
     */
    public boolean requiresDestinationAccount() {
        return this == TRANSFER || this == WIRE_TRANSFER;
    }

    /**
     * Check if this transaction type is a fee
     * @return true if the transaction is any type of fee
     */
    public boolean isFee() {
        return this == FEE || 
               this == OVERDRAFT_FEE || 
               this == MAINTENANCE_FEE;
    }

    /**
     * Check if this transaction type is an income/credit transaction
     * @return true if the transaction increases balance
     */
    public boolean isCredit() {
        return increasesBalance;
    }

    /**
     * Check if this transaction type is an expense/debit transaction
     * @return true if the transaction decreases balance
     */
    public boolean isDebit() {
        return !increasesBalance;
    }

    /**
     * Get the opposite transaction type (for transfers)
     * @return The corresponding opposite type, or null if not applicable
     */
    public TransactionType getOppositeType() {
        switch (this) {
            case TRANSFER:
                return TRANSFER_RECEIVED;
            case TRANSFER_RECEIVED:
                return TRANSFER;
            case WIRE_TRANSFER:
                return WIRE_RECEIVED;
            case WIRE_RECEIVED:
                return WIRE_TRANSFER;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}