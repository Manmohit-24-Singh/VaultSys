package com.vaultsys.services;

import com.vaultsys.dao.interfaces.IAccountDAO;
import com.vaultsys.models.*;
import com.vaultsys.exceptions.*;
import com.vaultsys.utils.Logger;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

/**
 * Service class for account management operations.
 * Handles account creation, updates, balance operations, and account queries.
 */
public class AccountService {
    
    private final IAccountDAO accountDAO;
    private static final Logger logger = Logger.getInstance();
    private static final SecureRandom random = new SecureRandom();
    
    // Account number configuration
    private static final int ACCOUNT_NUMBER_LENGTH = 12;
    private static final String SAVINGS_PREFIX = "SAV";
    private static final String CHECKING_PREFIX = "CHK";
    
    // Balance limits
    private static final BigDecimal MAX_BALANCE = new BigDecimal("1000000.00");
    private static final BigDecimal MIN_OPENING_BALANCE = new BigDecimal("0.00");
    
    public AccountService(IAccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }
    
    /**
     * Create a new savings account
     * @param userId The user ID
     * @param initialDeposit The initial deposit amount
     * @return The created savings account
     * @throws AccountException if account creation fails
     */
    public SavingsAccount createSavingsAccount(Long userId, BigDecimal initialDeposit) 
            throws AccountException {
        try {
            logger.info("Creating savings account for user: " + userId);
            
            // Validate initial deposit
            validateInitialDeposit(initialDeposit);
            
            // Generate unique account number
            String accountNumber = generateAccountNumber(SAVINGS_PREFIX);
            
            // Create savings account
            SavingsAccount account = new SavingsAccount(userId, accountNumber);
            account.setBalance(initialDeposit);
            
            // Save to database
            Account createdAccount = accountDAO.create(account);
            
            logger.info("Savings account created: " + accountNumber);
            return (SavingsAccount) createdAccount;
            
        } catch (DatabaseException e) {
            logger.error("Error creating savings account: " + e.getMessage());
            throw new AccountException("Failed to create savings account: " + e.getMessage());
        }
    }
    
    /**
     * Create a new checking account
     * @param userId The user ID
     * @param initialDeposit The initial deposit amount
     * @return The created checking account
     * @throws AccountException if account creation fails
     */
    public CheckingAccount createCheckingAccount(Long userId, BigDecimal initialDeposit) 
            throws AccountException {
        try {
            logger.info("Creating checking account for user: " + userId);
            
            // Validate initial deposit
            validateInitialDeposit(initialDeposit);
            
            // Generate unique account number
            String accountNumber = generateAccountNumber(CHECKING_PREFIX);
            
            // Create checking account
            CheckingAccount account = new CheckingAccount(userId, accountNumber);
            account.setBalance(initialDeposit);
            
            // Save to database
            Account createdAccount = accountDAO.create(account);
            
            logger.info("Checking account created: " + accountNumber);
            return (CheckingAccount) createdAccount;
            
        } catch (DatabaseException e) {
            logger.error("Error creating checking account: " + e.getMessage());
            throw new AccountException("Failed to create checking account: " + e.getMessage());
        }
    }
    
    /**
     * Get account by ID
     * @param accountId The account ID
     * @return The account
     * @throws AccountNotFoundException if account not found
     */
    public Account getAccountById(Long accountId) throws AccountNotFoundException {
        try {
            Optional<Account> accountOpt = accountDAO.findById(accountId);
            if (!accountOpt.isPresent()) {
                throw new AccountNotFoundException("Account not found: " + accountId);
            }
            return accountOpt.get();
        } catch (DatabaseException e) {
            logger.error("Error retrieving account: " + e.getMessage());
            throw new AccountNotFoundException("Failed to retrieve account: " + e.getMessage());
        }
    }
    
    /**
     * Get account by account number
     * @param accountNumber The account number
     * @return The account
     * @throws AccountNotFoundException if account not found
     */
    public Account getAccountByNumber(String accountNumber) throws AccountNotFoundException {
        try {
            Optional<Account> accountOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accountOpt.isPresent()) {
                throw new AccountNotFoundException("Account not found: " + accountNumber);
            }
            return accountOpt.get();
        } catch (DatabaseException e) {
            logger.error("Error retrieving account: " + e.getMessage());
            throw new AccountNotFoundException("Failed to retrieve account: " + e.getMessage());
        }
    }
    
    /**
     * Get all accounts for a user
     * @param userId The user ID
     * @return List of user's accounts
     * @throws AccountException if retrieval fails
     */
    public List<Account> getUserAccounts(Long userId) throws AccountException {
        try {
            return accountDAO.findByUserId(userId);
        } catch (DatabaseException e) {
            logger.error("Error retrieving user accounts: " + e.getMessage());
            throw new AccountException("Failed to retrieve accounts: " + e.getMessage());
        }
    }
    
    /**
     * Get all active accounts for a user
     * @param userId The user ID
     * @return List of active accounts
     * @throws AccountException if retrieval fails
     */
    public List<Account> getActiveUserAccounts(Long userId) throws AccountException {
        try {
            return accountDAO.findActiveAccountsByUserId(userId);
        } catch (DatabaseException e) {
            logger.error("Error retrieving active accounts: " + e.getMessage());
            throw new AccountException("Failed to retrieve active accounts: " + e.getMessage());
        }
    }
    
    /**
     * Get account balance
     * @param accountId The account ID
     * @return The current balance
     * @throws AccountNotFoundException if account not found
     */
    public BigDecimal getBalance(Long accountId) throws AccountNotFoundException {
        Account account = getAccountById(accountId);
        return account.getBalance();
    }
    
    /**
     * Get available balance (considering overdraft, minimum balance, etc.)
     * @param accountId The account ID
     * @return The available balance
     * @throws AccountNotFoundException if account not found
     */
    public BigDecimal getAvailableBalance(Long accountId) throws AccountNotFoundException {
        Account account = getAccountById(accountId);
        return account.getAvailableBalance();
    }
    
    /**
     * Update account status
     * @param accountId The account ID
     * @param status The new status
     * @return true if update successful
     * @throws AccountException if update fails
     */
    public boolean updateAccountStatus(Long accountId, AccountStatus status) 
            throws AccountException {
        try {
            logger.info("Updating account status - Account: " + accountId + ", Status: " + status);
            
            // Verify account exists
            getAccountById(accountId);
            
            return accountDAO.updateStatus(accountId, status);
        } catch (DatabaseException e) {
            logger.error("Error updating account status: " + e.getMessage());
            throw new AccountException("Failed to update account status: " + e.getMessage());
        }
    }
    
    /**
     * Close an account
     * @param accountId The account ID
     * @return true if closed successfully
     * @throws AccountException if closure fails
     */
    public boolean closeAccount(Long accountId) throws AccountException {
        try {
            logger.info("Closing account: " + accountId);
            
            Account account = getAccountById(accountId);
            
            // Check if account has zero balance
            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                throw new AccountException("Cannot close account with non-zero balance");
            }
            
            return accountDAO.closeAccount(accountId);
        } catch (DatabaseException e) {
            logger.error("Error closing account: " + e.getMessage());
            throw new AccountException("Failed to close account: " + e.getMessage());
        }
    }
    
    /**
     * Suspend an account
     * @param accountId The account ID
     * @return true if suspended successfully
     * @throws AccountException if suspension fails
     */
    public boolean suspendAccount(Long accountId) throws AccountException {
        try {
            logger.info("Suspending account: " + accountId);
            return accountDAO.suspendAccount(accountId);
        } catch (DatabaseException e) {
            logger.error("Error suspending account: " + e.getMessage());
            throw new AccountException("Failed to suspend account: " + e.getMessage());
        }
    }
    
    /**
     * Activate an account
     * @param accountId The account ID
     * @return true if activated successfully
     * @throws AccountException if activation fails
     */
    public boolean activateAccount(Long accountId) throws AccountException {
        try {
            logger.info("Activating account: " + accountId);
            return accountDAO.activateAccount(accountId);
        } catch (DatabaseException e) {
            logger.error("Error activating account: " + e.getMessage());
            throw new AccountException("Failed to activate account: " + e.getMessage());
        }
    }
    
    /**
     * Get total balance across all user accounts
     * @param userId The user ID
     * @return The total balance
     * @throws AccountException if calculation fails
     */
    public BigDecimal getTotalUserBalance(Long userId) throws AccountException {
        try {
            return accountDAO.getTotalBalanceByUserId(userId);
        } catch (DatabaseException e) {
            logger.error("Error calculating total balance: " + e.getMessage());
            throw new AccountException("Failed to calculate total balance: " + e.getMessage());
        }
    }
    
    /**
     * Check if account can perform withdrawal
     * @param accountId The account ID
     * @param amount The withdrawal amount
     * @return true if withdrawal is allowed
     * @throws AccountNotFoundException if account not found
     */
    public boolean canWithdraw(Long accountId, BigDecimal amount) 
            throws AccountNotFoundException {
        Account account = getAccountById(accountId);
        
        if (!account.isActive()) {
            return false;
        }
        
        if (!account.getStatus().allowsWithdrawals()) {
            return false;
        }
        
        return account.canWithdraw(amount);
    }
    
    /**
     * Validate account for transaction
     * @param accountId The account ID
     * @param transactionType The transaction type
     * @throws InvalidTransactionException if validation fails
     */
    public void validateAccountForTransaction(Long accountId, TransactionType transactionType) 
            throws InvalidTransactionException, AccountNotFoundException {
        Account account = getAccountById(accountId);
        
        if (!account.isActive()) {
            throw new InvalidTransactionException("Account is not active");
        }
        
        if (!account.getStatus().allowsTransaction(transactionType)) {
            throw new InvalidTransactionException(
                "Transaction type not allowed for account status: " + account.getStatus());
        }
    }
    
    /**
     * Get accounts by type
     * @param accountType The account type (SAVINGS, CHECKING)
     * @return List of accounts
     * @throws AccountException if retrieval fails
     */
    public List<Account> getAccountsByType(String accountType) throws AccountException {
        try {
            return accountDAO.findByType(accountType);
        } catch (DatabaseException e) {
            logger.error("Error retrieving accounts by type: " + e.getMessage());
            throw new AccountException("Failed to retrieve accounts: " + e.getMessage());
        }
    }
    
    /**
     * Get accounts by status
     * @param status The account status
     * @return List of accounts
     * @throws AccountException if retrieval fails
     */
    public List<Account> getAccountsByStatus(AccountStatus status) throws AccountException {
        try {
            return accountDAO.findByStatus(status);
        } catch (DatabaseException e) {
            logger.error("Error retrieving accounts by status: " + e.getMessage());
            throw new AccountException("Failed to retrieve accounts: " + e.getMessage());
        }
    }
    
    /**
     * Find dormant accounts
     * @param inactiveDays Number of days of inactivity
     * @return List of dormant accounts
     * @throws AccountException if retrieval fails
     */
    public List<Account> findDormantAccounts(int inactiveDays) throws AccountException {
        try {
            return accountDAO.findDormantAccounts(inactiveDays);
        } catch (DatabaseException e) {
            logger.error("Error finding dormant accounts: " + e.getMessage());
            throw new AccountException("Failed to find dormant accounts: " + e.getMessage());
        }
    }
    
    /**
     * Get system statistics
     * @return Total system balance
     * @throws AccountException if calculation fails
     */
    public BigDecimal getTotalSystemBalance() throws AccountException {
        try {
            return accountDAO.getTotalSystemBalance();
        } catch (DatabaseException e) {
            logger.error("Error calculating system balance: " + e.getMessage());
            throw new AccountException("Failed to calculate system balance: " + e.getMessage());
        }
    }
    
    /**
     * Get account count by type
     * @param accountType The account type
     * @return The count
     * @throws AccountException if retrieval fails
     */
    public long getAccountCountByType(String accountType) throws AccountException {
        try {
            return accountDAO.getAccountCountByType(accountType);
        } catch (DatabaseException e) {
            logger.error("Error getting account count: " + e.getMessage());
            throw new AccountException("Failed to get account count: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    /**
     * Generate unique account number
     * @param prefix The account type prefix
     * @return A unique account number
     */
    private String generateAccountNumber(String prefix) throws DatabaseException {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            StringBuilder accountNumber = new StringBuilder(prefix);
            
            // Generate random digits
            for (int j = 0; j < ACCOUNT_NUMBER_LENGTH - prefix.length(); j++) {
                accountNumber.append(random.nextInt(10));
            }
            
            String number = accountNumber.toString();
            
            // Check if account number already exists
            if (!accountDAO.accountNumberExists(number)) {
                return number;
            }
        }
        
        throw new DatabaseException("Failed to generate unique account number after " + 
                                   maxAttempts + " attempts");
    }
    
    /**
     * Validate initial deposit amount
     * @param amount The deposit amount
     * @throws AccountException if validation fails
     */
    private void validateInitialDeposit(BigDecimal amount) throws AccountException {
        if (amount == null) {
            throw new AccountException("Initial deposit amount is required");
        }
        
        if (amount.compareTo(MIN_OPENING_BALANCE) < 0) {
            throw new AccountException("Initial deposit must be at least " + MIN_OPENING_BALANCE);
        }
        
        if (amount.compareTo(MAX_BALANCE) > 0) {
            throw new AccountException("Initial deposit cannot exceed " + MAX_BALANCE);
        }
    }
}