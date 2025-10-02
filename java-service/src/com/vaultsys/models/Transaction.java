package com.vaultsys.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a financial transaction in the VaultSys banking system.
 * Tracks all account operations including deposits, withdrawals, and transfers.
 */
public class Transaction {
    private Long transactionId;
    private Long accountId;
    private Long toAccountId; // For transfers, null for deposits/withdrawals
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String currency;
    private LocalDateTime transactionDate;
    private String description;
    private String referenceNumber;
    private String status; // PENDING, COMPLETED, FAILED, REVERSED
    private String initiatedBy; // Username or system
    private String failureReason;
    private LocalDateTime processedAt;
    private String metadata; // JSON string for additional data

    // Constructors
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.currency = "USD";
        this.status = "PENDING";
        this.referenceNumber = generateReferenceNumber();
    }

    public Transaction(Long accountId, TransactionType type, BigDecimal amount) {
        this();
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
    }

    public Transaction(Long accountId, TransactionType type, BigDecimal amount, String description) {
        this(accountId, type, amount);
        this.description = description;
    }

    public Transaction(Long accountId, Long toAccountId, TransactionType type, 
                      BigDecimal amount, String description) {
        this(accountId, type, amount, description);
        this.toAccountId = toAccountId;
    }

    // Full constructor for database retrieval
    public Transaction(Long transactionId, Long accountId, Long toAccountId, 
                      TransactionType type, BigDecimal amount, BigDecimal balanceBefore,
                      BigDecimal balanceAfter, String currency, LocalDateTime transactionDate,
                      String description, String referenceNumber, String status,
                      String initiatedBy, String failureReason, LocalDateTime processedAt,
                      String metadata) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.toAccountId = toAccountId;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.status = status;
        this.initiatedBy = initiatedBy;
        this.failureReason = failureReason;
        this.processedAt = processedAt;
        this.metadata = metadata;
    }

    // Business methods
    public boolean isSuccessful() {
        return "COMPLETED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isReversed() {
        return "REVERSED".equals(status);
    }

    public boolean isTransfer() {
        return type == TransactionType.TRANSFER && toAccountId != null;
    }

    public void markAsCompleted(BigDecimal balanceBefore, BigDecimal balanceAfter) {
        this.status = "COMPLETED";
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = "FAILED";
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsReversed() {
        this.status = "REVERSED";
        this.processedAt = LocalDateTime.now();
    }

    private String generateReferenceNumber() {
        // Generate a unique reference number
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return "TXN" + timestamp + random;
    }

    public BigDecimal getBalanceChange() {
        if (balanceBefore != null && balanceAfter != null) {
            return balanceAfter.subtract(balanceBefore);
        }
        return BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    // Override methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) &&
               Objects.equals(referenceNumber, that.referenceNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, referenceNumber);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", accountId=" + accountId +
                ", toAccountId=" + toAccountId +
                ", type=" + type +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", transactionDate=" + transactionDate +
                ", description='" + description + '\'' +
                '}';
    }
}