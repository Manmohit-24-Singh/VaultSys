package com.vaultsys.services;

import com.vaultsys.models.Account;
import com.vaultsys.models.Transaction;
import com.vaultsys.models.TransactionType;
import com.vaultsys.dao.interfaces.IAccountDAO;
import com.vaultsys.dao.interfaces.ITransactionDAO;
import com.vaultsys.exceptions.InsufficientFundsException;
import com.vaultsys.exceptions.InvalidTransactionException;
import com.vaultsys.exceptions.AccountNotFoundException;
import com.vaultsys.exceptions.DatabaseException;
import com.vaultsys.utils.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TransactionService handles all transaction-related business logic.
 * Demonstrates Service Layer pattern and business logic separation.
 * 
 * Key OOP Concepts:
 * - Composition (uses DAO objects)
 * - Single Responsibility Principle
 * - Exception handling
 * - Business logic encapsulation
 */
public class TransactionService {
    
    private final IAccountDAO accountDAO;
    private final ITransactionDAO transactionDAO;
    private final ValidationService validationService;
    private static final Logger logger = Logger.getInstance();
    
    /**
     * Constructor with dependency injection
     * Demonstrates Dependency Injection and Composition
     * 
     * @param accountDAO Account data access object
     * @param transactionDAO Transaction data access object
     * @param validationService Validation service
     */
    public TransactionService(IAccountDAO accountDAO, 
                             ITransactionDAO transactionDAO,
                             ValidationService validationService) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.validationService = validationService;
    }
    
    /**
     * Process a deposit transaction
     * 
     * @param accountNumber Account to deposit into
     * @param amount Amount to deposit
     * @return Transaction object
     * @throws InvalidTransactionException if validation fails
     * @throws AccountNotFoundException if account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public Transaction deposit(String accountNumber, BigDecimal amount) 
            throws InvalidTransactionException, AccountNotFoundException, DatabaseException {
        
        logger.info("Processing deposit: Account=" + accountNumber + ", Amount=" + amount);
        
        // Validate amount
        if (!ValidationService.isValidTransactionAmount(amount)) {
            throw new InvalidTransactionException("Invalid deposit amount: " + amount);
        }
        
        // Get account
        Optional<Account> accountOpt = accountDAO.findByAccountNumber(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new AccountNotFoundException("Account not found", accountNumber);
        }
        
        Account account = accountOpt.get();
        BigDecimal balanceBefore = account.getBalance();
        
        // Perform deposit
        try {
            account.deposit(amount);
            accountDAO.update(account);
            
            // Create transaction record
            Transaction transaction = new Transaction(
                account.getAccountId(),
                TransactionType.DEPOSIT,
                amount,
                "Deposit to account"
            );
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(account.getBalance());
            transaction.setStatus("COMPLETED");
            
            transactionDAO.create(transaction);
            
            logger.info("Deposit successful: " + transaction.getReferenceNumber());
            return transaction;
            
        } catch (Exception e) {
            logger.error("Deposit failed: " + e.getMessage());
            throw new DatabaseException("Failed to process deposit", "DEPOSIT", null);
        }
    }
    
    /**
     * Process a withdrawal transaction
     * 
     * @param accountNumber Account to withdraw from
     * @param amount Amount to withdraw
     * @return Transaction object
     * @throws InsufficientFundsException if insufficient funds
     * @throws InvalidTransactionException if validation fails
     * @throws AccountNotFoundException if account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public Transaction withdraw(String accountNumber, BigDecimal amount) 
            throws InsufficientFundsException, InvalidTransactionException, 
                   AccountNotFoundException, DatabaseException {
        
        logger.info("Processing withdrawal: Account=" + accountNumber + ", Amount=" + amount);
        
        // Validate amount
        if (!ValidationService.isValidTransactionAmount(amount)) {
            throw new InvalidTransactionException("Invalid withdrawal amount: " + amount);
        }
        
        // Get account
        Optional<Account> accountOpt = accountDAO.findByAccountNumber(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new AccountNotFoundException("Account not found", accountNumber);
        }
        
        Account account = accountOpt.get();
        
        // Check if account is active
        if (!account.isActive()) {
            throw new InvalidTransactionException("Account is not active");
        }
        
        BigDecimal balanceBefore = account.getBalance();
        
        // Check sufficient funds before withdrawal
        if (account.getBalance().compareTo(amount) < 0) {
            logger.warn("Withdrawal failed - insufficient funds");
            throw new InsufficientFundsException(
                "Insufficient funds for withdrawal",
                account.getBalance(),
                amount
            );
        }
        
        // Perform withdrawal
        try {
            account.withdraw(amount);
            accountDAO.update(account);
            
            // Create transaction record
            Transaction transaction = new Transaction(
                account.getAccountId(),
                TransactionType.WITHDRAWAL,
                amount,
                "Withdrawal from account"
            );
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(account.getBalance());
            transaction.setStatus("COMPLETED");
            
            transactionDAO.create(transaction);
            
            logger.info("Withdrawal successful: " + transaction.getReferenceNumber());
            return transaction;
            
        } catch (Exception e) {
            logger.error("Withdrawal failed: " + e.getMessage());
            throw new DatabaseException("Failed to process withdrawal", "WITHDRAWAL", null);
        }
    }
    
    /**
     * Transfer funds between two accounts
     * Demonstrates transaction atomicity - either both succeed or both fail
     * 
     * @param fromAccountNumber Source account
     * @param toAccountNumber Destination account
     * @param amount Amount to transfer
     * @return Transaction object
     * @throws InsufficientFundsException if source has insufficient funds
     * @throws InvalidTransactionException if validation fails
     * @throws AccountNotFoundException if either account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) 
            throws InsufficientFundsException, InvalidTransactionException, 
                   AccountNotFoundException, DatabaseException {
        
        logger.info("Processing transfer: From=" + fromAccountNumber + 
                   " To=" + toAccountNumber + ", Amount=" + amount);
        
        // Validate amount
        if (!ValidationService.isValidTransactionAmount(amount)) {
            throw new InvalidTransactionException("Invalid transfer amount: " + amount);
        }
        
        // Validate different accounts
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }
        
        // Get both accounts
        Optional<Account> fromAccountOpt = accountDAO.findByAccountNumber(fromAccountNumber);
        if (!fromAccountOpt.isPresent()) {
            throw new AccountNotFoundException("Source account not found", fromAccountNumber);
        }
        
        Optional<Account> toAccountOpt = accountDAO.findByAccountNumber(toAccountNumber);
        if (!toAccountOpt.isPresent()) {
            throw new AccountNotFoundException("Destination account not found", toAccountNumber);
        }
        
        Account fromAccount = fromAccountOpt.get();
        Account toAccount = toAccountOpt.get();
        
        // Check both accounts are active
        if (!fromAccount.isActive() || !toAccount.isActive()) {
            throw new InvalidTransactionException("Both accounts must be active for transfer");
        }
        
        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal toBalanceBefore = toAccount.getBalance();
        
        // Check sufficient funds before transfer
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            logger.warn("Transfer failed - insufficient funds");
            throw new InsufficientFundsException(
                "Insufficient funds for transfer",
                fromAccount.getBalance(),
                amount
            );
        }
        
        // Perform transfer (atomic operation)
        try {
            // Withdraw from source
            fromAccount.withdraw(amount);
            
            // Deposit to destination
            toAccount.deposit(amount);
            
            // Update both accounts in database
            accountDAO.update(fromAccount);
            accountDAO.update(toAccount);
            
            // Create transaction record for sender
            Transaction senderTransaction = new Transaction(
                fromAccount.getAccountId(),
                toAccount.getAccountId(),
                TransactionType.TRANSFER,
                amount,
                "Transfer to " + toAccountNumber
            );
            senderTransaction.setBalanceBefore(fromBalanceBefore);
            senderTransaction.setBalanceAfter(fromAccount.getBalance());
            senderTransaction.setStatus("COMPLETED");
            
            transactionDAO.create(senderTransaction);
            
            // Create transaction record for receiver
            Transaction receiverTransaction = new Transaction(
                toAccount.getAccountId(),
                TransactionType.TRANSFER_RECEIVED,
                amount,
                "Transfer from " + fromAccountNumber
            );
            receiverTransaction.setBalanceBefore(toBalanceBefore);
            receiverTransaction.setBalanceAfter(toAccount.getBalance());
            receiverTransaction.setStatus("COMPLETED");
            
            transactionDAO.create(receiverTransaction);
            
            logger.info("Transfer successful: " + senderTransaction.getReferenceNumber());
            return senderTransaction;
            
        } catch (Exception e) {
            logger.error("Transfer failed: " + e.getMessage());
            // In a real system, we would rollback the transaction here
            throw new DatabaseException("Failed to process transfer", "TRANSFER", null);
        }
    }
    
    /**
     * Get transaction history for an account
     * 
     * @param accountId Account ID
     * @return List of transactions
     * @throws AccountNotFoundException if account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public List<Transaction> getTransactionHistory(Long accountId) 
            throws AccountNotFoundException, DatabaseException {
        
        logger.info("Retrieving transaction history for account: " + accountId);
        
        // Verify account exists
        Optional<Account> accountOpt = accountDAO.findById(accountId);
        if (!accountOpt.isPresent()) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        
        try {
            return transactionDAO.findByAccountId(accountId);
        } catch (Exception e) {
            logger.error("Failed to retrieve transaction history: " + e.getMessage());
            throw new DatabaseException("Failed to retrieve transaction history", "SELECT", null);
        }
    }
    
    /**
     * Get a specific transaction by ID
     * 
     * @param transactionId Transaction ID
     * @return Transaction object or null if not found
     * @throws DatabaseException if database operation fails
     */
    public Transaction getTransactionById(Long transactionId) throws DatabaseException {
        logger.info("Retrieving transaction: " + transactionId);
        
        try {
            Optional<Transaction> transactionOpt = transactionDAO.findById(transactionId);
            return transactionOpt.orElse(null);
        } catch (Exception e) {
            logger.error("Failed to retrieve transaction: " + e.getMessage());
            throw new DatabaseException("Failed to retrieve transaction", "SELECT", null);
        }
    }
    
    /**
     * Calculate total deposits for an account
     * 
     * @param accountId Account ID
     * @return Total deposit amount
     * @throws AccountNotFoundException if account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public BigDecimal getTotalDeposits(Long accountId) 
            throws AccountNotFoundException, DatabaseException {
        
        List<Transaction> transactions = getTransactionHistory(accountId);
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate total withdrawals for an account
     * 
     * @param accountId Account ID
     * @return Total withdrawal amount
     * @throws AccountNotFoundException if account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public BigDecimal getTotalWithdrawals(Long accountId) 
            throws AccountNotFoundException, DatabaseException {
        
        List<Transaction> transactions = getTransactionHistory(accountId);
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get transactions by type
     * 
     * @param accountId Account ID
     * @param type Transaction type
     * @return List of transactions
     * @throws AccountNotFoundException if account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public List<Transaction> getTransactionsByType(Long accountId, TransactionType type) 
            throws AccountNotFoundException, DatabaseException {
        
        try {
            // Get all transactions for account and filter by type
            List<Transaction> allTransactions = transactionDAO.findByAccountId(accountId);
            return allTransactions.stream()
                    .filter(t -> t.getType() == type)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions by type: " + e.getMessage());
            throw new DatabaseException("Failed to retrieve transactions by type", "SELECT", null);
        }
    }
    
    /**
     * Get transactions within a date range
     * 
     * @param accountId Account ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions
     * @throws AccountNotFoundException if account doesn't exist
     * @throws DatabaseException if database operation fails
     */
    public List<Transaction> getTransactionsByDateRange(Long accountId, 
                                                        LocalDateTime startDate, 
                                                        LocalDateTime endDate) 
            throws AccountNotFoundException, DatabaseException {
        
        try {
            return transactionDAO.findByAccountIdAndDateRange(accountId, startDate, endDate);
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions by date: " + e.getMessage());
            throw new DatabaseException("Failed to retrieve transactions by date", "SELECT", null);
        }
    }
    
    /**
     * Validate a transaction (can be used before processing)
     * 
     * @param accountNumber Account number
     * @param amount Transaction amount
     * @param type Transaction type
     * @return true if valid, false otherwise
     */
    public boolean validateTransaction(String accountNumber, BigDecimal amount, TransactionType type) {
        try {
            // Validate amount
            if (!ValidationService.isValidTransactionAmount(amount)) {
                return false;
            }
            
            // Validate account exists
            Optional<Account> accountOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accountOpt.isPresent() || !accountOpt.get().isActive()) {
                return false;
            }
            
            Account account = accountOpt.get();
            
            // For withdrawals and transfers, check sufficient funds
            if (type == TransactionType.WITHDRAWAL || type == TransactionType.TRANSFER) {
                return account.getBalance().compareTo(amount) >= 0;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Transaction validation failed: " + e.getMessage());
            return false;
        }
    }
}