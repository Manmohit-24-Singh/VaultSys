package com.vaultsys.controllers;

import com.vaultsys.models.Account;
import com.vaultsys.models.AccountStatus;
import com.vaultsys.models.SavingsAccount;
import com.vaultsys.models.CheckingAccount;
import com.vaultsys.services.AccountService;
import com.vaultsys.exceptions.AccountException;
import com.vaultsys.exceptions.AccountNotFoundException;
import com.vaultsys.utils.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Controller class for handling account-related user interactions.
 * Acts as the presentation layer, coordinating between user input and service layer.
 * 
 * Key OOP Concepts:
 * - Separation of Concerns: UI logic separate from business logic
 * - Composition: Uses AccountService
 * - Single Responsibility: Only handles user interaction
 */
public class AccountController {
    
    private final AccountService accountService;
    private static final Logger logger = Logger.getInstance();
    private final Scanner scanner;
    
    /**
     * Constructor with dependency injection
     * @param accountService Account service instance
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Display account menu and handle user selection
     */
    public void showAccountMenu() {
        boolean running = true;
        
        while (running) {
            displayAccountMenuOptions();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        createSavingsAccount();
                        break;
                    case 2:
                        createCheckingAccount();
                        break;
                    case 3:
                        viewAccountDetails();
                        break;
                    case 4:
                        viewAccountByNumber();
                        break;
                    case 5:
                        viewAllUserAccounts();
                        break;
                    case 6:
                        checkBalance();
                        break;
                    case 7:
                        closeAccount();
                        break;
                    case 8:
                        updateAccountStatus();
                        break;
                    case 9:
                        viewAccountsByType();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Returning to main menu...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                logger.error("Error in account menu: " + e.getMessage());
                System.out.println("An error occurred. Please try again.");
            }
        }
    }
    
    /**
     * Display account menu options
     */
    private void displayAccountMenuOptions() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║      ACCOUNT MANAGEMENT MENU       ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. Create Savings Account");
        System.out.println("2. Create Checking Account");
        System.out.println("3. View Account Details (by ID)");
        System.out.println("4. View Account (by Account Number)");
        System.out.println("5. View All User Accounts");
        System.out.println("6. Check Account Balance");
        System.out.println("7. Close Account");
        System.out.println("8. Update Account Status");
        System.out.println("9. View Accounts by Type");
        System.out.println("0. Back to Main Menu");
        System.out.println("────────────────────────────────────");
    }
    
    /**
     * Create a new savings account
     */
    private void createSavingsAccount() {
        try {
            System.out.println("\n=== Create Savings Account ===");
            
            Long userId = getLongInput("Enter User ID: ");
            BigDecimal initialDeposit = getBigDecimalInput("Enter initial deposit amount: $");
            
            SavingsAccount account = accountService.createSavingsAccount(userId, initialDeposit);
            
            System.out.println("\n✓ Savings Account Created Successfully!");
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Initial Balance: $" + account.getBalance());
            System.out.println("Interest Rate: " + account.getInterestRate() + "%");
            
            logger.info("Savings account created: " + account.getAccountNumber());
            
        } catch (AccountException e) {
            System.out.println("✗ Failed to create account: " + e.getMessage());
            logger.error("Failed to create savings account: " + e.getMessage());
        }
    }
    
    /**
     * Create a new checking account
     */
    private void createCheckingAccount() {
        try {
            System.out.println("\n=== Create Checking Account ===");
            
            Long userId = getLongInput("Enter User ID: ");
            BigDecimal initialDeposit = getBigDecimalInput("Enter initial deposit amount: $");
            
            CheckingAccount account = accountService.createCheckingAccount(userId, initialDeposit);
            
            System.out.println("\n✓ Checking Account Created Successfully!");
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Initial Balance: $" + account.getBalance());
            System.out.println("Overdraft Limit: $" + account.getOverdraftLimit());
            
            logger.info("Checking account created: " + account.getAccountNumber());
            
        } catch (AccountException e) {
            System.out.println("✗ Failed to create account: " + e.getMessage());
            logger.error("Failed to create checking account: " + e.getMessage());
        }
    }
    
    /**
     * View account details by ID
     */
    private void viewAccountDetails() {
        try {
            System.out.println("\n=== View Account Details ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            Account account = accountService.getAccountById(accountId);
            
            displayAccountDetails(account);
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }
    
    /**
     * View account by account number
     */
    private void viewAccountByNumber() {
        try {
            System.out.println("\n=== View Account by Number ===");
            
            String accountNumber = getStringInput("Enter Account Number: ");
            Account account = accountService.getAccountByNumber(accountNumber);
            
            displayAccountDetails(account);
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }
    
    /**
     * View all accounts for a user
     */
    private void viewAllUserAccounts() {
        try {
            System.out.println("\n=== View User Accounts ===");
            
            Long userId = getLongInput("Enter User ID: ");
            List<Account> accounts = accountService.getUserAccounts(userId);
            
            if (accounts.isEmpty()) {
                System.out.println("No accounts found for this user.");
                return;
            }
            
            System.out.println("\nTotal Accounts: " + accounts.size());
            System.out.println("────────────────────────────────────────────────────────");
            
            for (Account account : accounts) {
                displayAccountSummary(account);
                System.out.println("────────────────────────────────────────────────────────");
            }
            
            // Display total balance
            BigDecimal totalBalance = accountService.getTotalUserBalance(userId);
            System.out.println("\nTotal Balance Across All Accounts: $" + totalBalance);
            
        } catch (AccountException e) {
            System.out.println("✗ Failed to retrieve accounts: " + e.getMessage());
        }
    }
    
    /**
     * Check account balance
     */
    private void checkBalance() {
        try {
            System.out.println("\n=== Check Account Balance ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            BigDecimal balance = accountService.getBalance(accountId);
            BigDecimal availableBalance = accountService.getAvailableBalance(accountId);
            
            System.out.println("\n── Balance Information ──");
            System.out.println("Current Balance: $" + balance);
            System.out.println("Available Balance: $" + availableBalance);
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }
    
    /**
     * Close an account
     */
    private void closeAccount() {
        try {
            System.out.println("\n=== Close Account ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            
            // Display account details first
            Account account = accountService.getAccountById(accountId);
            displayAccountSummary(account);
            
            // Confirm closure
            String confirm = getStringInput("\nAre you sure you want to close this account? (yes/no): ");
            
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Account closure cancelled.");
                return;
            }
            
            boolean success = accountService.closeAccount(accountId);
            
            if (success) {
                System.out.println("✓ Account closed successfully.");
                logger.info("Account closed: " + accountId);
            } else {
                System.out.println("✗ Failed to close account.");
            }
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        } catch (AccountException e) {
            System.out.println("✗ Cannot close account: " + e.getMessage());
        }
    }
    
    /**
     * Update account status
     */
    private void updateAccountStatus() {
        try {
            System.out.println("\n=== Update Account Status ===");
            
            Long accountId = getLongInput("Enter Account ID: ");
            
            // Display current account info
            Account account = accountService.getAccountById(accountId);
            System.out.println("Current Status: " + account.getStatus());
            
            // Display status options
            System.out.println("\nAvailable Statuses:");
            System.out.println("1. ACTIVE");
            System.out.println("2. SUSPENDED");
            System.out.println("3. FROZEN");
            System.out.println("4. DORMANT");
            
            int choice = getIntInput("Select new status: ");
            
            AccountStatus newStatus;
            switch (choice) {
                case 1:
                    newStatus = AccountStatus.ACTIVE;
                    break;
                case 2:
                    newStatus = AccountStatus.SUSPENDED;
                    break;
                case 3:
                    newStatus = AccountStatus.FROZEN;
                    break;
                case 4:
                    newStatus = AccountStatus.DORMANT;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            
            boolean success = accountService.updateAccountStatus(accountId, newStatus);
            
            if (success) {
                System.out.println("✓ Account status updated to: " + newStatus);
                logger.info("Account status updated: " + accountId + " -> " + newStatus);
            } else {
                System.out.println("✗ Failed to update account status.");
            }
            
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        } catch (AccountException e) {
            System.out.println("✗ Failed to update status: " + e.getMessage());
        }
    }
    
    /**
     * View accounts by type
     */
    private void viewAccountsByType() {
        try {
            System.out.println("\n=== View Accounts by Type ===");
            System.out.println("1. Savings Accounts");
            System.out.println("2. Checking Accounts");
            
            int choice = getIntInput("Select account type: ");
            String accountType = (choice == 1) ? "SAVINGS" : "CHECKING";
            
            List<Account> accounts = accountService.getAccountsByType(accountType);
            
            if (accounts.isEmpty()) {
                System.out.println("No " + accountType.toLowerCase() + " accounts found.");
                return;
            }
            
            System.out.println("\nTotal " + accountType + " Accounts: " + accounts.size());
            System.out.println("────────────────────────────────────────────────────────");
            
            for (Account account : accounts) {
                displayAccountSummary(account);
                System.out.println("────────────────────────────────────────────────────────");
            }
            
        } catch (AccountException e) {
            System.out.println("✗ Failed to retrieve accounts: " + e.getMessage());
        }
    }
    
    // Display helper methods
    
    /**
     * Display detailed account information
     */
    private void displayAccountDetails(Account account) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║      ACCOUNT DETAILS               ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("Account ID: " + account.getAccountId());
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Account Type: " + account.getAccountType());
        System.out.println("User ID: " + account.getUserId());
        System.out.println("Balance: $" + account.getBalance());
        System.out.println("Available Balance: $" + account.getAvailableBalance());
        System.out.println("Status: " + account.getStatus());
        System.out.println("Currency: " + account.getCurrency());
        System.out.println("Created At: " + account.getCreatedAt());
        System.out.println("Updated At: " + account.getUpdatedAt());
        
        if (account instanceof SavingsAccount) {
            SavingsAccount savings = (SavingsAccount) account;
            System.out.println("Interest Rate: " + savings.getInterestRate() + "%");
            System.out.println("Minimum Balance: $" + savings.getMinimumBalance());
        } else if (account instanceof CheckingAccount) {
            CheckingAccount checking = (CheckingAccount) account;
            System.out.println("Overdraft Limit: $" + checking.getOverdraftLimit());
            System.out.println("Transaction Fee: $" + checking.getTransactionFee());
            System.out.println("Free Transactions/Month: " + checking.getFreeTransactionsPerMonth());
            System.out.println("Current Month Transactions: " + checking.getCurrentMonthTransactions());
        }
    }
    
    /**
     * Display account summary (one-line format)
     */
    private void displayAccountSummary(Account account) {
        System.out.printf("%-15s | %-12s | %-10s | $%-12s | %-10s%n",
            account.getAccountNumber(),
            account.getAccountType(),
            account.getStatus(),
            account.getBalance(),
            account.getCurrency());
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
}