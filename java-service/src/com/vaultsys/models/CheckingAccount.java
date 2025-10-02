package com.vaultsys.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a checking account with overdraft protection and transaction fees.
 * Extends the base Account class with checking-specific features.
 */
public class CheckingAccount extends Account {
    private BigDecimal overdraftLimit;
    private BigDecimal transactionFee;
    private int freeTransactionsPerMonth;
    private int currentMonthTransactions;
    private boolean hasDebitCard;
    private String debitCardNumber;
    private LocalDateTime debitCardExpiryDate;

    // Default values
    private static final BigDecimal DEFAULT_OVERDRAFT_LIMIT = new BigDecimal("500.00");
    private static final BigDecimal DEFAULT_TRANSACTION_FEE = new BigDecimal("0.25");
    private static final int DEFAULT_FREE_TRANSACTIONS = 20;

    // Constructors
    public CheckingAccount() {
        super();
        this.accountType = "CHECKING";
        this.overdraftLimit = DEFAULT_OVERDRAFT_LIMIT;
        this.transactionFee = DEFAULT_TRANSACTION_FEE;
        this.freeTransactionsPerMonth = DEFAULT_FREE_TRANSACTIONS;
        this.currentMonthTransactions = 0;
        this.hasDebitCard = false;
    }

    public CheckingAccount(Long userId, String accountNumber) {
        super(userId, accountNumber, "CHECKING");
        this.overdraftLimit = DEFAULT_OVERDRAFT_LIMIT;
        this.transactionFee = DEFAULT_TRANSACTION_FEE;
        this.freeTransactionsPerMonth = DEFAULT_FREE_TRANSACTIONS;
        this.currentMonthTransactions = 0;
        this.hasDebitCard = false;
    }

    public CheckingAccount(Long userId, String accountNumber, 
                          BigDecimal overdraftLimit, int freeTransactionsPerMonth) {
        super(userId, accountNumber, "CHECKING");
        this.overdraftLimit = overdraftLimit;
        this.transactionFee = DEFAULT_TRANSACTION_FEE;
        this.freeTransactionsPerMonth = freeTransactionsPerMonth;
        this.currentMonthTransactions = 0;
        this.hasDebitCard = false;
    }

    // Implement abstract methods from Account
    @Override
    public boolean canWithdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Calculate available balance including overdraft
        BigDecimal totalAvailable = balance.add(overdraftLimit);
        
        // Consider transaction fee if applicable
        BigDecimal totalNeeded = amount;
        if (shouldChargeFee()) {
            totalNeeded = totalNeeded.add(transactionFee);
        }
        
        return totalAvailable.compareTo(totalNeeded) >= 0;
    }

    @Override
    public BigDecimal getAvailableBalance() {
        // For checking accounts, available balance includes overdraft limit
        return balance.add(overdraftLimit);
    }

    @Override
    public String getAccountTypeDescription() {
        return "Checking Account with $" + overdraftLimit + " overdraft protection";
    }

    // Checking-specific methods
    @Override
    public void withdraw(BigDecimal amount) {
        // Charge transaction fee if applicable
        if (shouldChargeFee()) {
            super.withdraw(transactionFee);
        }
        
        super.withdraw(amount);
        currentMonthTransactions++;
    }

    @Override
    public void deposit(BigDecimal amount) {
        // Charge transaction fee if applicable
        if (shouldChargeFee()) {
            BigDecimal netDeposit = amount.subtract(transactionFee);
            if (netDeposit.compareTo(BigDecimal.ZERO) > 0) {
                super.deposit(netDeposit);
            }
        } else {
            super.deposit(amount);
        }
        currentMonthTransactions++;
    }

    private boolean shouldChargeFee() {
        return currentMonthTransactions >= freeTransactionsPerMonth;
    }

    public void resetMonthlyTransactions() {
        this.currentMonthTransactions = 0;
    }

    public int getRemainingFreeTransactions() {
        return Math.max(0, freeTransactionsPerMonth - currentMonthTransactions);
    }

    public boolean isOverdrawn() {
        return balance.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getOverdraftAmount() {
        if (isOverdrawn()) {
            return balance.abs();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getRemainingOverdraftLimit() {
        if (isOverdrawn()) {
            return overdraftLimit.subtract(balance.abs());
        }
        return overdraftLimit;
    }

    public void issueDebitCard(String cardNumber, LocalDateTime expiryDate) {
        this.hasDebitCard = true;
        this.debitCardNumber = cardNumber;
        this.debitCardExpiryDate = expiryDate;
    }

    public void cancelDebitCard() {
        this.hasDebitCard = false;
        this.debitCardNumber = null;
        this.debitCardExpiryDate = null;
    }

    public boolean isDebitCardValid() {
        if (!hasDebitCard || debitCardExpiryDate == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(debitCardExpiryDate);
    }

    // Getters and Setters
    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        if (overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative");
        }
        this.overdraftLimit = overdraftLimit;
    }

    public BigDecimal getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(BigDecimal transactionFee) {
        if (transactionFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction fee cannot be negative");
        }
        this.transactionFee = transactionFee;
    }

    public int getFreeTransactionsPerMonth() {
        return freeTransactionsPerMonth;
    }

    public void setFreeTransactionsPerMonth(int freeTransactionsPerMonth) {
        if (freeTransactionsPerMonth < 0) {
            throw new IllegalArgumentException("Free transactions cannot be negative");
        }
        this.freeTransactionsPerMonth = freeTransactionsPerMonth;
    }

    public int getCurrentMonthTransactions() {
        return currentMonthTransactions;
    }

    public void setCurrentMonthTransactions(int currentMonthTransactions) {
        this.currentMonthTransactions = currentMonthTransactions;
    }

    public boolean hasDebitCard() {
        return hasDebitCard;
    }

    public void setHasDebitCard(boolean hasDebitCard) {
        this.hasDebitCard = hasDebitCard;
    }

    public String getDebitCardNumber() {
        return debitCardNumber;
    }

    public void setDebitCardNumber(String debitCardNumber) {
        this.debitCardNumber = debitCardNumber;
    }

    public LocalDateTime getDebitCardExpiryDate() {
        return debitCardExpiryDate;
    }

    public void setDebitCardExpiryDate(LocalDateTime debitCardExpiryDate) {
        this.debitCardExpiryDate = debitCardExpiryDate;
    }

    @Override
    public String toString() {
        return "CheckingAccount{" +
                "accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", overdraftLimit=" + overdraftLimit +
                ", currentMonthTransactions=" + currentMonthTransactions +
                ", freeTransactionsPerMonth=" + freeTransactionsPerMonth +
                ", hasDebitCard=" + hasDebitCard +
                ", isOverdrawn=" + isOverdrawn() +
                ", status=" + status +
                '}';
    }
}