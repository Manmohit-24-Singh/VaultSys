package com.vaultsys.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a savings account with interest calculation and minimum balance requirements.
 * Extends the base Account class with savings-specific features.
 */
public class SavingsAccount extends Account {
    private BigDecimal interestRate; // Annual interest rate as decimal (e.g., 0.025 for 2.5%)
    private BigDecimal minimumBalance;
    private int maxWithdrawalsPerMonth;
    private int currentMonthWithdrawals;
    private LocalDateTime lastInterestCalculation;

    // Default values
    private static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("0.025"); // 2.5%
    private static final BigDecimal DEFAULT_MINIMUM_BALANCE = new BigDecimal("100.00");
    private static final int DEFAULT_MAX_WITHDRAWALS = 6;

    // Constructors
    public SavingsAccount() {
        super();
        this.accountType = "SAVINGS";
        this.interestRate = DEFAULT_INTEREST_RATE;
        this.minimumBalance = DEFAULT_MINIMUM_BALANCE;
        this.maxWithdrawalsPerMonth = DEFAULT_MAX_WITHDRAWALS;
        this.currentMonthWithdrawals = 0;
        this.lastInterestCalculation = LocalDateTime.now();
    }

    public SavingsAccount(Long userId, String accountNumber) {
        super(userId, accountNumber, "SAVINGS");
        this.interestRate = DEFAULT_INTEREST_RATE;
        this.minimumBalance = DEFAULT_MINIMUM_BALANCE;
        this.maxWithdrawalsPerMonth = DEFAULT_MAX_WITHDRAWALS;
        this.currentMonthWithdrawals = 0;
        this.lastInterestCalculation = LocalDateTime.now();
    }

    public SavingsAccount(Long userId, String accountNumber, BigDecimal interestRate, 
                         BigDecimal minimumBalance) {
        super(userId, accountNumber, "SAVINGS");
        this.interestRate = interestRate;
        this.minimumBalance = minimumBalance;
        this.maxWithdrawalsPerMonth = DEFAULT_MAX_WITHDRAWALS;
        this.currentMonthWithdrawals = 0;
        this.lastInterestCalculation = LocalDateTime.now();
    }

    // Implement abstract methods from Account
    @Override
    public boolean canWithdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Check if withdrawal limit reached
        if (currentMonthWithdrawals >= maxWithdrawalsPerMonth) {
            return false;
        }
        
        // Check if sufficient balance after withdrawal
        BigDecimal balanceAfterWithdrawal = balance.subtract(amount);
        return balanceAfterWithdrawal.compareTo(minimumBalance) >= 0;
    }

    @Override
    public BigDecimal getAvailableBalance() {
        BigDecimal available = balance.subtract(minimumBalance);
        return available.compareTo(BigDecimal.ZERO) > 0 ? available : BigDecimal.ZERO;
    }

    @Override
    public String getAccountTypeDescription() {
        return "Savings Account with " + 
               interestRate.multiply(new BigDecimal("100")).setScale(2) + 
               "% annual interest rate";
    }

    // Savings-specific methods
    @Override
    public void withdraw(BigDecimal amount) {
        super.withdraw(amount);
        currentMonthWithdrawals++;
    }

    public BigDecimal calculateInterest() {
        // Simple interest calculation based on current balance
        // In a real system, this would be more complex with compounding
        BigDecimal interest = balance.multiply(interestRate)
                                    .divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
        return interest;
    }

    public void applyInterest() {
        BigDecimal interest = calculateInterest();
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            deposit(interest);
            lastInterestCalculation = LocalDateTime.now();
        }
    }

    public void resetMonthlyWithdrawals() {
        this.currentMonthWithdrawals = 0;
    }

    public boolean hasReachedWithdrawalLimit() {
        return currentMonthWithdrawals >= maxWithdrawalsPerMonth;
    }

    public int getRemainingWithdrawals() {
        return Math.max(0, maxWithdrawalsPerMonth - currentMonthWithdrawals);
    }

    public boolean meetsMinimumBalance() {
        return balance.compareTo(minimumBalance) >= 0;
    }

    // Getters and Setters
    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        if (interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        this.interestRate = interestRate;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        if (minimumBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum balance cannot be negative");
        }
        this.minimumBalance = minimumBalance;
    }

    public int getMaxWithdrawalsPerMonth() {
        return maxWithdrawalsPerMonth;
    }

    public void setMaxWithdrawalsPerMonth(int maxWithdrawalsPerMonth) {
        if (maxWithdrawalsPerMonth < 0) {
            throw new IllegalArgumentException("Max withdrawals cannot be negative");
        }
        this.maxWithdrawalsPerMonth = maxWithdrawalsPerMonth;
    }

    public int getCurrentMonthWithdrawals() {
        return currentMonthWithdrawals;
    }

    public void setCurrentMonthWithdrawals(int currentMonthWithdrawals) {
        this.currentMonthWithdrawals = currentMonthWithdrawals;
    }

    public LocalDateTime getLastInterestCalculation() {
        return lastInterestCalculation;
    }

    public void setLastInterestCalculation(LocalDateTime lastInterestCalculation) {
        this.lastInterestCalculation = lastInterestCalculation;
    }

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", interestRate=" + interestRate +
                ", minimumBalance=" + minimumBalance +
                ", currentMonthWithdrawals=" + currentMonthWithdrawals +
                ", maxWithdrawalsPerMonth=" + maxWithdrawalsPerMonth +
                ", status=" + status +
                '}';
    }
}