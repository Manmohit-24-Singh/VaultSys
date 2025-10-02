package com.vaultsys.dao.interfaces;

import com.vaultsys.models.Transaction;
import com.vaultsys.models.TransactionType;
import com.vaultsys.exceptions.DatabaseException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Transaction entity operations.
 * Defines the contract for all transaction-related database operations.
 */
public interface ITransactionDAO {
    
    /**
     * Create a new transaction in the database
     * @param transaction The transaction object to create
     * @return The created transaction with generated ID
     * @throws DatabaseException if database operation fails
     */
    Transaction create(Transaction transaction) throws DatabaseException;
    
    /**
     * Find a transaction by its unique ID
     * @param transactionId The transaction ID to search for
     * @return Optional containing the transaction if found, empty otherwise
     * @throws DatabaseException if database operation fails
     */
    Optional<Transaction> findById(Long transactionId) throws DatabaseException;
    
    /**
     * Find a transaction by its reference number
     * @param referenceNumber The reference number to search for
     * @return Optional containing the transaction if found, empty otherwise
     * @throws DatabaseException if database operation fails
     */
    Optional<Transaction> findByReferenceNumber(String referenceNumber) throws DatabaseException;
    
    /**
     * Find all transactions for a specific account
     * @param accountId The account ID
     * @return List of transactions for the account
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByAccountId(Long accountId) throws DatabaseException;
    
    /**
     * Find all transactions for a specific account within a date range
     * @param accountId The account ID
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of transactions within the date range
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByAccountIdAndDateRange(Long accountId, 
                                                   LocalDateTime startDate, 
                                                   LocalDateTime endDate) throws DatabaseException;
    
    /**
     * Update an existing transaction's information
     * @param transaction The transaction object with updated information
     * @return The updated transaction
     * @throws DatabaseException if database operation fails
     */
    Transaction update(Transaction transaction) throws DatabaseException;
    
    /**
     * Delete a transaction by its ID
     * @param transactionId The ID of the transaction to delete
     * @return true if deletion was successful, false otherwise
     * @throws DatabaseException if database operation fails
     */
    boolean delete(Long transactionId) throws DatabaseException;
    
    /**
     * Retrieve all transactions from the database
     * @return List of all transactions
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findAll() throws DatabaseException;
    
    /**
     * Find all transactions of a specific type
     * @param type The transaction type
     * @return List of transactions of the specified type
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByType(TransactionType type) throws DatabaseException;
    
    /**
     * Find all transactions with a specific status
     * @param status The transaction status (e.g., "PENDING", "COMPLETED")
     * @return List of transactions with the specified status
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByStatus(String status) throws DatabaseException;
    
    /**
     * Find all pending transactions
     * @return List of pending transactions
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findPendingTransactions() throws DatabaseException;
    
    /**
     * Find all failed transactions
     * @return List of failed transactions
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findFailedTransactions() throws DatabaseException;
    
    /**
     * Find transactions initiated by a specific user
     * @param username The username who initiated the transactions
     * @return List of transactions initiated by the user
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByInitiator(String username) throws DatabaseException;
    
    /**
     * Get recent transactions for an account (last N transactions)
     * @param accountId The account ID
     * @param limit Maximum number of transactions to return
     * @return List of recent transactions
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> getRecentTransactions(Long accountId, int limit) throws DatabaseException;
    
    /**
     * Find transactions within a date range
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of transactions within the date range
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByDateRange(LocalDateTime startDate, 
                                      LocalDateTime endDate) throws DatabaseException;
    
    /**
     * Find transactions with amount greater than specified value
     * @param amount The minimum transaction amount
     * @return List of transactions with amount greater than specified
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByAmountGreaterThan(BigDecimal amount) throws DatabaseException;
    
    /**
     * Find transactions with amount less than specified value
     * @param amount The maximum transaction amount
     * @return List of transactions with amount less than specified
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findByAmountLessThan(BigDecimal amount) throws DatabaseException;
    
    /**
     * Find all transfer transactions between two accounts
     * @param fromAccountId The source account ID
     * @param toAccountId The destination account ID
     * @return List of transfer transactions
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findTransfersBetweenAccounts(Long fromAccountId, 
                                                    Long toAccountId) throws DatabaseException;
    
    /**
     * Get the sum of all transactions for an account by type
     * @param accountId The account ID
     * @param type The transaction type
     * @return The total amount for the transaction type
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getSumByAccountAndType(Long accountId, TransactionType type) throws DatabaseException;
    
    /**
     * Get the sum of all deposits for an account
     * @param accountId The account ID
     * @return The total deposit amount
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getTotalDeposits(Long accountId) throws DatabaseException;
    
    /**
     * Get the sum of all withdrawals for an account
     * @param accountId The account ID
     * @return The total withdrawal amount
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getTotalWithdrawals(Long accountId) throws DatabaseException;
    
    /**
     * Get count of transactions for an account
     * @param accountId The account ID
     * @return The transaction count
     * @throws DatabaseException if database operation fails
     */
    long getTransactionCount(Long accountId) throws DatabaseException;
    
    /**
     * Get count of transactions by type for an account
     * @param accountId The account ID
     * @param type The transaction type
     * @return The transaction count for the type
     * @throws DatabaseException if database operation fails
     */
    long getTransactionCountByType(Long accountId, TransactionType type) throws DatabaseException;
    
    /**
     * Mark a transaction as completed
     * @param transactionId The transaction ID
     * @param balanceBefore The balance before transaction
     * @param balanceAfter The balance after transaction
     * @return true if update was successful
     * @throws DatabaseException if database operation fails
     */
    boolean markAsCompleted(Long transactionId, BigDecimal balanceBefore, 
                           BigDecimal balanceAfter) throws DatabaseException;
    
    /**
     * Mark a transaction as failed
     * @param transactionId The transaction ID
     * @param failureReason The reason for failure
     * @return true if update was successful
     * @throws DatabaseException if database operation fails
     */
    boolean markAsFailed(Long transactionId, String failureReason) throws DatabaseException;
    
    /**
     * Reverse a transaction
     * @param transactionId The transaction ID to reverse
     * @return true if reversal was successful
     * @throws DatabaseException if database operation fails
     */
    boolean reverseTransaction(Long transactionId) throws DatabaseException;
    
    /**
     * Get daily transaction summary for an account
     * @param accountId The account ID
     * @param date The date for the summary
     * @return Map with transaction statistics
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getDailyTransactionVolume(Long accountId, LocalDateTime date) throws DatabaseException;
    
    /**
     * Get monthly transaction summary for an account
     * @param accountId The account ID
     * @param year The year
     * @param month The month (1-12)
     * @return Total transaction volume for the month
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getMonthlyTransactionVolume(Long accountId, int year, int month) throws DatabaseException;
    
    /**
     * Find suspicious transactions (large amounts, unusual patterns)
     * @param threshold The amount threshold for suspicious transactions
     * @return List of potentially suspicious transactions
     * @throws DatabaseException if database operation fails
     */
    List<Transaction> findSuspiciousTransactions(BigDecimal threshold) throws DatabaseException;
    
    /**
     * Get total system transaction count
     * @return The total number of transactions
     * @throws DatabaseException if database operation fails
     */
    long getTotalTransactionCount() throws DatabaseException;
    
    /**
     * Get total system transaction volume
     * @return The total transaction volume
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getTotalTransactionVolume() throws DatabaseException;
}