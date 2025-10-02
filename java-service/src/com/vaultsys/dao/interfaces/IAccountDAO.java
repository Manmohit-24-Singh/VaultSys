package com.vaultsys.dao.interfaces;

import com.vaultsys.models.Account;
import com.vaultsys.models.AccountStatus;
import com.vaultsys.exceptions.DatabaseException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Account entity operations.
 * Defines the contract for all account-related database operations.
 */
public interface IAccountDAO {
    
    /**
     * Create a new account in the database
     * @param account The account object to create
     * @return The created account with generated ID
     * @throws DatabaseException if database operation fails
     */
    Account create(Account account) throws DatabaseException;
    
    /**
     * Find an account by its unique ID
     * @param accountId The account ID to search for
     * @return Optional containing the account if found, empty otherwise
     * @throws DatabaseException if database operation fails
     */
    Optional<Account> findById(Long accountId) throws DatabaseException;
    
    /**
     * Find an account by its account number
     * @param accountNumber The account number to search for
     * @return Optional containing the account if found, empty otherwise
     * @throws DatabaseException if database operation fails
     */
    Optional<Account> findByAccountNumber(String accountNumber) throws DatabaseException;
    
    /**
     * Find all accounts belonging to a specific user
     * @param userId The user ID
     * @return List of accounts for the user
     * @throws DatabaseException if database operation fails
     */
    List<Account> findByUserId(Long userId) throws DatabaseException;
    
    /**
     * Update an existing account's information
     * @param account The account object with updated information
     * @return The updated account
     * @throws DatabaseException if database operation fails
     */
    Account update(Account account) throws DatabaseException;
    
    /**
     * Delete an account by its ID
     * @param accountId The ID of the account to delete
     * @return true if deletion was successful, false otherwise
     * @throws DatabaseException if database operation fails
     */
    boolean delete(Long accountId) throws DatabaseException;
    
    /**
     * Retrieve all accounts from the database
     * @return List of all accounts
     * @throws DatabaseException if database operation fails
     */
    List<Account> findAll() throws DatabaseException;
    
    /**
     * Find all accounts with a specific status
     * @param status The account status to filter by
     * @return List of accounts with the specified status
     * @throws DatabaseException if database operation fails
     */
    List<Account> findByStatus(AccountStatus status) throws DatabaseException;
    
    /**
     * Find all accounts of a specific type
     * @param accountType The account type (e.g., "SAVINGS", "CHECKING")
     * @return List of accounts of the specified type
     * @throws DatabaseException if database operation fails
     */
    List<Account> findByType(String accountType) throws DatabaseException;
    
    /**
     * Update account balance
     * @param accountId The ID of the account
     * @param newBalance The new balance amount
     * @return true if update was successful
     * @throws DatabaseException if database operation fails
     */
    boolean updateBalance(Long accountId, BigDecimal newBalance) throws DatabaseException;
    
    /**
     * Update account status
     * @param accountId The ID of the account
     * @param status The new status
     * @return true if update was successful
     * @throws DatabaseException if database operation fails
     */
    boolean updateStatus(Long accountId, AccountStatus status) throws DatabaseException;
    
    /**
     * Check if an account number already exists
     * @param accountNumber The account number to check
     * @return true if account number exists, false otherwise
     * @throws DatabaseException if database operation fails
     */
    boolean accountNumberExists(String accountNumber) throws DatabaseException;
    
    /**
     * Get the total balance across all user accounts
     * @param userId The user ID
     * @return The total balance
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getTotalBalanceByUserId(Long userId) throws DatabaseException;
    
    /**
     * Find accounts with balance greater than specified amount
     * @param amount The minimum balance
     * @return List of accounts with balance greater than amount
     * @throws DatabaseException if database operation fails
     */
    List<Account> findAccountsWithBalanceGreaterThan(BigDecimal amount) throws DatabaseException;
    
    /**
     * Find accounts with balance less than specified amount
     * @param amount The maximum balance
     * @return List of accounts with balance less than amount
     * @throws DatabaseException if database operation fails
     */
    List<Account> findAccountsWithBalanceLessThan(BigDecimal amount) throws DatabaseException;
    
    /**
     * Find all active accounts for a user
     * @param userId The user ID
     * @return List of active accounts
     * @throws DatabaseException if database operation fails
     */
    List<Account> findActiveAccountsByUserId(Long userId) throws DatabaseException;
    
    /**
     * Get count of accounts by type
     * @param accountType The account type
     * @return The count of accounts
     * @throws DatabaseException if database operation fails
     */
    long getAccountCountByType(String accountType) throws DatabaseException;
    
    /**
     * Get total number of accounts in the system
     * @return The total account count
     * @throws DatabaseException if database operation fails
     */
    long getTotalAccountCount() throws DatabaseException;
    
    /**
     * Get total value of all deposits in the system
     * @return The total deposit value
     * @throws DatabaseException if database operation fails
     */
    BigDecimal getTotalSystemBalance() throws DatabaseException;
    
    /**
     * Close an account (set status to CLOSED)
     * @param accountId The ID of the account to close
     * @return true if closure was successful
     * @throws DatabaseException if database operation fails
     */
    boolean closeAccount(Long accountId) throws DatabaseException;
    
    /**
     * Suspend an account (set status to SUSPENDED)
     * @param accountId The ID of the account to suspend
     * @return true if suspension was successful
     * @throws DatabaseException if database operation fails
     */
    boolean suspendAccount(Long accountId) throws DatabaseException;
    
    /**
     * Activate an account (set status to ACTIVE)
     * @param accountId The ID of the account to activate
     * @return true if activation was successful
     * @throws DatabaseException if database operation fails
     */
    boolean activateAccount(Long accountId) throws DatabaseException;
    
    /**
     * Find dormant accounts (no transactions in specified days)
     * @param days Number of days of inactivity
     * @return List of dormant accounts
     * @throws DatabaseException if database operation fails
     */
    List<Account> findDormantAccounts(int days) throws DatabaseException;
}