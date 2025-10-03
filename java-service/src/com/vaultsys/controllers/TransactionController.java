package com.vaultsys.controllers;

import com.vaultsys.models.Transaction;
import com.vaultsys.models.TransactionType;
import com.vaultsys.services.TransactionService;
import com.vaultsys.exceptions.InsufficientFundsException;
import com.vaultsys.exceptions.InvalidTransactionException;
import com.vaultsys.exceptions.AccountNotFoundException;
import com.vaultsys.exceptions.DatabaseException;
import com.vaultsys.utils.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Controller class for handling transaction-related user interactions.
 * Manages deposits, withdrawals, transfers, and transaction history.
 * 
 * Key OOP Concepts:
 * - Separation of Concerns: UI logic separate from business logic
 * - Composition: Uses TransactionService
 * - Exception Handling: Comprehensive error handling
 */
public class TransactionController {
    
    private final TransactionService transactionService;
    private static final Logger logger = Logger.getInstance();
    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructor with dependency injection
     * @param transactionService Transaction service instance
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Display transaction menu and handle user selection
     */
    public void showTransactionMenu() {
        boolean running = true;
        
        while (running) {
            displayTransactionMenuOptions();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        processDeposit();
                        break;
                    case 2:
                        processWithdrawal();
                        break;
                    case 3:
                        processTransfer();
                        break;
                    case 4:
                        viewTransactionHistory();
                        break;
                    case 5:
                        viewTransactionById();
                        break;
                    case 6:
                        viewTransactionsByType();
                        break;
                    case 7:
                        viewTransactionsByDateRange();
                        break;
                    case 8:
                        viewAccountSummary();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Returning to main menu...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                logger.error("Error in transaction menu: " + e.getMessage());
                System.out.println("An error occurred. Please try again.");
            }
        }
    }
    
    /**
     * Display transaction menu options
     */
    private void displayTransactionMenuOptions() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║    TRANSACTION MANAGEMENT MENU     ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. Deposit Funds");
        System.out.println("2. Withdraw Funds");
        System.out.println("3. Transfer Funds");
        System.out.println("4. View Transaction History");
        System.out.println("5. View Transaction by ID");
        System.out.println("6. View Transactions by Type");
        System.out.println("7. View Transactions by Date Range");
        System.out.println("8. View Account Summary");
        System.out.println("0. Back to Main Menu");
        System.out.println("────────────────────────────────────");
    }
    
    /**
     * Process a deposit transaction
     */
    private void processDeposit() {
        try {
            System.out.println("\n=== Deposit Funds ===");
            
            String accountNumber = getStringInput("Enter account number: ");
            BigDecimal amount = getBigDecimalInput("Enter amount to deposit: $");
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("✗ Amount must be greater than zero.");
                return;
            }
            
            // Confirm transaction
            System.out.println("\n── Transaction Details ──");
            System.out.println("Account: " + accountNumber);
            System.out.println("Amount: $" + amount);
            String confirm = getStringInput("Confirm deposit? (yes/no): ");
            
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Deposit cancelled.");
                return;
            }
            
            // Process deposit
            Transaction transaction = transactionService.deposit(accountNumber, amount);
            
            System.out.println("\n✓ Deposit Successful!");
            displayTransactionReceipt(transaction);
            
            logger.info("Deposit processed: " + transaction.getReferenceNumber());
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ Account not found: " + e.getMessage());
        } catch (InvalidTransactionException e) {
            System.out.println("✗ Invalid transaction: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("✗ Transaction failed: " + e.getMessage());
            logger.error("Deposit failed: " + e.getMessage());
        }
    }
    
    /**
     * Process a withdrawal transaction
     */
    private void processWithdrawal() {
        try {
            System.out.println("\n=== Withdraw Funds ===");
            
            String accountNumber = getStringInput("Enter account number: ");
            BigDecimal amount = getBigDecimalInput("Enter amount to withdraw: $");
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("✗ Amount must be greater than zero.");
                return;
            }
            
            // Confirm transaction
            System.out.println("\n── Transaction Details ──");
            System.out.println("Account: " + accountNumber);
            System.out.println("Amount: $" + amount);
            String confirm = getStringInput("Confirm withdrawal? (yes/no): ");
            
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Withdrawal cancelled.");
                return;
            }
            
            // Process withdrawal
            Transaction transaction = transactionService.withdraw(accountNumber, amount);
            
            System.out.println("\n✓ Withdrawal Successful!");
            displayTransactionReceipt(transaction);
            
            logger.info("Withdrawal processed: " + transaction.getReferenceNumber());
            
        } catch (InsufficientFundsException e) {
            System.out.println("✗ Insufficient Funds!");
            System.out.println("   Available: $" + e.getAvailableBalance());
            System.out.println("   Requested: $" + e.getRequestedAmount());
            System.out.println("   Shortfall: $" + e.getShortfall());
        } catch (AccountNotFoundException e) {
            System.out.println("✗ Account not found: " + e.getMessage());
        } catch (InvalidTransactionException e) {
            System.out.println("✗ Invalid transaction: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("✗ Transaction failed: " + e.getMessage());
            logger.error("Withdrawal failed: " + e.getMessage());
        }
    }
    
    /**
     * Process a transfer transaction
     */
    private void processTransfer() {
        try {
            System.out.println("\n=== Transfer Funds ===");
            
            String fromAccount = getStringInput("Enter source account number: ");
            String toAccount = getStringInput("Enter destination account number: ");
            BigDecimal amount = getBigDecimalInput("Enter amount to transfer: $");
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("✗ Amount must be greater than zero.");
                return;
            }
            
            // Confirm transaction
            System.out.println("\n── Transaction Details ──");
            System.out.println("From Account: " + fromAccount);
            System.out.println("To Account: " + toAccount);
            System.out.println("Amount: $" + amount);
            String confirm = getStringInput("Confirm transfer? (yes/no): ");
            
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Transfer cancelled.");
                return;
            }
            
            // Process transfer
            Transaction transaction = transactionService.transfer(fromAccount, toAccount, amount);
            
            System.out.println("\n✓ Transfer Successful!");
            displayTransactionReceipt(transaction);
            
            logger.info("Transfer processed: " + transaction.getReferenceNumber());
            
        } catch (InsufficientFundsException e) {
            System.out.println("✗ Insufficient Funds!");
            System.out.println("   Available: $" + e.getAvailableBalance());
            System.out.println("   Requested: $" + e.getRequestedAmount());
            System.out.println("   Shortfall: $" + e.getShortfall());
        } catch (AccountNotFoundException e) {
            System.out.println("✗ Account not found: " + e.getMessage());
        } catch (InvalidTransactionException e) {
            System.out.println("✗ Invalid transaction: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("✗ Transaction failed: " + e.getMessage());
            logger.error("Transfer failed: " + e.getMessage());
        }
    }
    
    /**
     * View transaction history for an account
     */
    private void viewTransactionHistory() {
        try {
            System.out.println("\n=== Transaction History ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            List<Transaction> transactions = transactionService.getTransactionHistory(accountId);
            
            if (transactions.isEmpty()) {
                System.out.println("No transactions found for this account.");
                return;
            }
            
            System.out.println("\nTotal Transactions: " + transactions.size());
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            System.out.printf("%-15s | %-12s | %-15s | %-12s | %-20s%n",
                "Reference", "Type", "Amount", "Balance", "Date");
            System.out.println("───────────────────────────────────────────────────────────────────────────────");
            
            for (Transaction transaction : transactions) {
                displayTransactionRow(transaction);
            }
            
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("✗ Failed to retrieve transactions: " + e.getMessage());
        }
    }
    
    /**
     * View transactions by date range
     */
    private void viewTransactionsByDateRange() {
        try {
            System.out.println("\n=== View Transactions by Date Range ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            
            System.out.println("Enter start date (yyyy-MM-dd HH:mm:ss): ");
            String startDateStr = getStringInput("");
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, DATE_FORMATTER);
            
            System.out.println("Enter end date (yyyy-MM-dd HH:mm:ss): ");
            String endDateStr = getStringInput("");
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, DATE_FORMATTER);
            
            List<Transaction> transactions = transactionService.getTransactionsByDateRange(
                accountId, startDate, endDate);
            
            if (transactions.isEmpty()) {
                System.out.println("No transactions found in the specified date range.");
                return;
            }
            
            System.out.println("\nTotal Transactions: " + transactions.size());
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            System.out.printf("%-15s | %-12s | %-15s | %-12s | %-20s%n",
                "Reference", "Type", "Amount", "Balance", "Date");
            System.out.println("───────────────────────────────────────────────────────────────────────────────");
            
            for (Transaction transaction : transactions) {
                displayTransactionRow(transaction);
            }
            
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.out.println("✗ Failed to retrieve transactions: " + e.getMessage());
        }
    }
    
    /**
     * View account summary with transaction statistics
     */
    private void viewAccountSummary() {
        try {
            System.out.println("\n=== Account Transaction Summary ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            
            BigDecimal totalDeposits = transactionService.getTotalDeposits(accountId);
            BigDecimal totalWithdrawals = transactionService.getTotalWithdrawals(accountId);
            List<Transaction> allTransactions = transactionService.getTransactionHistory(accountId);
            
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║    ACCOUNT TRANSACTION SUMMARY     ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.println("Total Transactions: " + allTransactions.size());
            System.out.println("Total Deposits: $" + totalDeposits);
            System.out.println("Total Withdrawals: $" + totalWithdrawals);
            System.out.println("Net Change: $" + totalDeposits.subtract(totalWithdrawals));
            
            // Count by type
            long deposits = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT).count();
            long withdrawals = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL).count();
            long transfers = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER).count();
            
            System.out.println("\n── Transaction Breakdown ──");
            System.out.println("Deposits: " + deposits);
            System.out.println("Withdrawals: " + withdrawals);
            System.out.println("Transfers: " + transfers);
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("✗ Failed to retrieve summary: " + e.getMessage());
        }
    }
    
    // Display helper methods
    
    /**
     * Display transaction receipt
     */
    private void displayTransactionReceipt(Transaction transaction) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║      TRANSACTION RECEIPT           ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("Reference Number: " + transaction.getReferenceNumber());
        System.out.println("Transaction Type: " + transaction.getType());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("Balance After: $" + transaction.getBalanceAfter());
        System.out.println("Date: " + transaction.getTransactionDate().format(DATE_FORMATTER));
        System.out.println("Status: " + transaction.getStatus());
        if (transaction.getDescription() != null) {
            System.out.println("Description: " + transaction.getDescription());
        }
    }
    
    /**
     * Display detailed transaction information
     */
    private void displayTransactionDetails(Transaction transaction) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║      TRANSACTION DETAILS           ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("Transaction ID: " + transaction.getTransactionId());
        System.out.println("Reference Number: " + transaction.getReferenceNumber());
        System.out.println("Account ID: " + transaction.getAccountId());
        if (transaction.getToAccountId() != null) {
            System.out.println("To Account ID: " + transaction.getToAccountId());
        }
        System.out.println("Transaction Type: " + transaction.getType());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("Balance Before: $" + transaction.getBalanceBefore());
        System.out.println("Balance After: $" + transaction.getBalanceAfter());
        System.out.println("Currency: " + transaction.getCurrency());
        System.out.println("Date: " + transaction.getTransactionDate().format(DATE_FORMATTER));
        System.out.println("Status: " + transaction.getStatus());
        if (transaction.getDescription() != null) {
            System.out.println("Description: " + transaction.getDescription());
        }
        if (transaction.getInitiatedBy() != null) {
            System.out.println("Initiated By: " + transaction.getInitiatedBy());
        }
    }
    
    /**
     * Display transaction as a single row
     */
    private void displayTransactionRow(Transaction transaction) {
        System.out.printf("%-15s | %-12s | $%-14s | $%-11s | %-20s%n",
            transaction.getReferenceNumber(),
            transaction.getType().toString().substring(0, Math.min(12, transaction.getType().toString().length())),
            transaction.getAmount(),
            transaction.getBalanceAfter(),
            transaction.getTransactionDate().format(DATE_FORMATTER));
    }
    
    // Input helper methods
    
    private int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. " + prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }
    
    private Long getLongInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextLong()) {
            System.out.print("Invalid input. " + prompt);
            scanner.next();
        }
        Long value = scanner.nextLong();
        scanner.nextLine(); // consume newline
        return value;
    }
    
    private BigDecimal getBigDecimalInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextBigDecimal()) {
            System.out.print("Invalid input. " + prompt);
            scanner.next();
        }
        BigDecimal value = scanner.nextBigDecimal();
        scanner.nextLine(); // consume newline
        return value;
    }
    
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * View a specific transaction by ID
     */
    private void viewTransactionById() {
        try {
            System.out.println("\n=== View Transaction ===");
            
            Long transactionId = getLongInput("Enter Transaction ID: ");
            Transaction transaction = transactionService.getTransactionById(transactionId);
            
            if (transaction == null) {
                System.out.println("✗ Transaction not found.");
                return;
            }
            
            displayTransactionDetails(transaction);
            
        } catch (DatabaseException e) {
            System.out.println("✗ Failed to retrieve transaction: " + e.getMessage());
        }
    }
    
    /**
     * View transactions by type
     */
    private void viewTransactionsByType() {
        try {
            System.out.println("\n=== View Transactions by Type ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            
            System.out.println("\nTransaction Types:");
            System.out.println("1. DEPOSIT");
            System.out.println("2. WITHDRAWAL");
            System.out.println("3. TRANSFER");
            System.out.println("4. TRANSFER_RECEIVED");
            
            int choice = getIntInput("Select type: ");
            
            TransactionType type;
            switch (choice) {
                case 1: type = TransactionType.DEPOSIT; break;
                case 2: type = TransactionType.WITHDRAWAL; break;
                case 3: type = TransactionType.TRANSFER; break;
                case 4: type = TransactionType.TRANSFER_RECEIVED; break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            
            List<Transaction> transactions = transactionService.getTransactionsByType(accountId, type);
            
            if (transactions.isEmpty()) {
                System.out.println("No " + type + " transactions found.");
                return;
            }
            
            System.out.println("\nTotal " + type + " Transactions: " + transactions.size());
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            System.out.printf("%-15s | %-12s | %-15s | %-12s | %-20s%n",
                "Reference", "Type", "Amount", "Balance", "Date");
            System.out.println("───────────────────────────────────────────────────────────────────────────────");
            
            for (Transaction transaction : transactions) {
                displayTransactionRow(transaction);
            }
            
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("✗ Failed to retrieve transactions: " + e.getMessage());
        }
    }
}
