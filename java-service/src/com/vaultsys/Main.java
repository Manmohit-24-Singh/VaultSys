package com.vaultsys;

import com.vaultsys.controllers.AccountController;
import com.vaultsys.controllers.TransactionController;
import com.vaultsys.controllers.UserController;
import com.vaultsys.services.AccountService;
import com.vaultsys.services.AuthService;
import com.vaultsys.services.TransactionService;
import com.vaultsys.services.ValidationService;
import com.vaultsys.dao.interfaces.IAccountDAO;
import com.vaultsys.dao.interfaces.ITransactionDAO;
import com.vaultsys.dao.interfaces.IUserDAO;
import com.vaultsys.dao.impl.AccountDAOImpl;
import com.vaultsys.dao.impl.TransactionDAOImpl;
import com.vaultsys.dao.impl.UserDAOImpl;
import com.vaultsys.utils.ConfigLoader;
import com.vaultsys.utils.DatabaseConnection;
import com.vaultsys.utils.Logger;

import java.util.Scanner;

/**
 * Main application class for VaultSys Banking System.
 * Demonstrates complete OOP architecture with MVC pattern.
 * 
 * Architecture Layers:
 * - Presentation Layer: Controllers (AccountController, TransactionController, UserController)
 * - Business Logic Layer: Services (AccountService, TransactionService, AuthService)
 * - Data Access Layer: DAOs (AccountDAOImpl, TransactionDAOImpl, UserDAOImpl)
 * - Database Layer: PostgreSQL
 * 
 * OOP Concepts Demonstrated:
 * - Dependency Injection
 * - Layered Architecture
 * - Separation of Concerns
 * - Composition over Inheritance
 */
public class Main {
    
    private static final Logger logger = Logger.getInstance();
    private static Scanner scanner = new Scanner(System.in);
    
    // Controllers
    private static UserController userController;
    private static AccountController accountController;
    private static TransactionController transactionController;
    
    public static void main(String[] args) {
        try {
            // Initialize application
            logger.info("Starting VaultSys Banking System...");
            displayWelcomeBanner();
            
            // Initialize dependencies
            if (!initializeApplication()) {
                logger.error("Failed to initialize application");
                System.out.println("Failed to start application. Check database connection.");
                return;
            }
            
            // Authentication flow
            if (!authenticateUser()) {
                logger.info("User authentication cancelled. Exiting...");
                System.out.println("Goodbye!");
                return;
            }
            
            // Main application menu
            runMainMenu();
            
            // Cleanup
            cleanup();
            
        } catch (Exception e) {
            logger.error("Fatal error in main application: " + e.getMessage());
            System.out.println("A critical error occurred. Please contact support.");
            e.printStackTrace();
        }
    }
    
    /**
     * Display welcome banner
     */
    private static void displayWelcomeBanner() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║                                                      ║");
        System.out.println("║              VAULTSYS BANKING SYSTEM                 ║");
        System.out.println("║                                                      ║");
        System.out.println("║          Secure Multi-Tier Banking Solution          ║");
        System.out.println("║                                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Initialize all application dependencies using Dependency Injection
     * This demonstrates the Dependency Injection pattern
     */
    private static boolean initializeApplication() {
        try {
            logger.info("Initializing application components...");
            
            // Initialize database connection
            // Load configuration
            ConfigLoader config = ConfigLoader.getInstance();
            DatabaseConnection.initialize(config);
            
            if (!DatabaseConnection.testConnection()) {
                logger.error("Database connection failed");
                return false;
            }
            logger.info("Database connection established");
            
            // Initialize DAOs (Data Access Layer)
            IUserDAO userDAO = new UserDAOImpl();
            IAccountDAO accountDAO = new AccountDAOImpl();
            ITransactionDAO transactionDAO = new TransactionDAOImpl();
            logger.info("DAO layer initialized");
            
            // Initialize Services (Business Logic Layer)
            AuthService authService = new AuthService(userDAO);
            AccountService accountService = new AccountService(accountDAO);
            ValidationService validationService = new ValidationService();
            TransactionService transactionService = new TransactionService(
                accountDAO, transactionDAO, validationService);
            logger.info("Service layer initialized");
            
            // Initialize Controllers (Presentation Layer)
            userController = new UserController(authService);
            accountController = new AccountController(accountService);
            transactionController = new TransactionController(transactionService);
            logger.info("Controller layer initialized");
            
            System.out.println("✓ Application initialized successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Application initialization failed: " + e.getMessage());
            System.out.println("✗ Failed to initialize application: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle user authentication
     */
    private static boolean authenticateUser() {
        logger.info("Starting authentication flow");
        return userController.showAuthenticationMenu();
    }
    
    /**
     * Run main application menu
     */
    private static void runMainMenu() {
        boolean running = true;
        
        logger.info("User entered main application menu");
        
        while (running) {
            displayMainMenu();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        accountController.showAccountMenu();
                        break;
                    case 2:
                        transactionController.showTransactionMenu();
                        break;
                    case 3:
                        userController.showUserMenu();
                        break;
                    case 4:
                        displaySystemInfo();
                        break;
                    case 5:
                        displayHelp();
                        break;
                    case 0:
                        running = confirmExit();
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                logger.error("Error in main menu: " + e.getMessage());
                System.out.println("An error occurred. Please try again.");
            }
        }
    }
    
    /**
     * Display main menu options
     */
    private static void displayMainMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║         MAIN MENU                  ║");
        System.out.println("╚════════════════════════════════════╝");
        
        if (userController.isLoggedIn()) {
            System.out.println("User: " + userController.getCurrentUser().getFullName());
            System.out.println("Role: " + userController.getCurrentUser().getRole());
            System.out.println("────────────────────────────────────");
        }
        
        System.out.println("1. Account Management");
        System.out.println("2. Transactions");
        System.out.println("3. User Profile");
        System.out.println("4. System Information");
        System.out.println("5. Help");
        System.out.println("0. Exit");
        System.out.println("────────────────────────────────────");
    }
    
    /**
     * Display system information
     */
    private static void displaySystemInfo() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║      SYSTEM INFORMATION            ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("Application: VaultSys Banking System");
        System.out.println("Version: 1.0.0");
        System.out.println("Architecture: Multi-tier (Java + C++ + PostgreSQL)");
        System.out.println();
        System.out.println("Components:");
        System.out.println("- Java Service: Business Logic & API");
        System.out.println("- C++ Service: High-Performance Transaction Processing");
        System.out.println("- PostgreSQL: Reliable Data Persistence");
        System.out.println();
        System.out.println("Features:");
        System.out.println("- Savings & Checking Accounts");
        System.out.println("- Deposits, Withdrawals, Transfers");
        System.out.println("- Transaction History & Analytics");
        System.out.println("- User Authentication & Authorization");
        System.out.println("- Interest Calculations");
        System.out.println("- Overdraft Protection");
        System.out.println();
        System.out.println("Database Status: " + 
            (DatabaseConnection.testConnection() ? "Connected" : "Disconnected"));
        System.out.println("Connection Pool: " + DatabaseConnection.getPoolStats());
        System.out.println("Current User: " + 
            (userController.isLoggedIn() ? userController.getCurrentUser().getUsername() : "Not logged in"));
    }
    
    /**
     * Display help information
     */
    private static void displayHelp() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║           HELP & GUIDE             ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println();
        System.out.println("GETTING STARTED:");
        System.out.println("1. Create a user account (Register)");
        System.out.println("2. Login with your credentials");
        System.out.println("3. Create a bank account (Savings or Checking)");
        System.out.println("4. Start making transactions");
        System.out.println();
        System.out.println("ACCOUNT TYPES:");
        System.out.println("- Savings Account: Earns interest, minimum balance requirements");
        System.out.println("- Checking Account: Daily transactions, overdraft protection");
        System.out.println();
        System.out.println("TRANSACTIONS:");
        System.out.println("- Deposit: Add funds to your account");
        System.out.println("- Withdrawal: Remove funds from your account");
        System.out.println("- Transfer: Move funds between accounts");
        System.out.println();
        System.out.println("SECURITY:");
        System.out.println("- All transactions are logged");
        System.out.println("- Passwords are hashed and secured");
        System.out.println("- Account balances are protected");
        System.out.println();
        System.out.println("SUPPORT:");
        System.out.println("For assistance, contact your system administrator");
    }
    
    /**
     * Confirm exit
     */
    private static boolean confirmExit() {
        System.out.print("\nAre you sure you want to exit? (yes/no): ");
        String response = scanner.nextLine().trim();
        
        if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
            System.out.println("\nThank you for using VaultSys Banking System!");
            System.out.println("Goodbye!");
            logger.info("User exited application");
            return false; // Stop running
        }
        
        return true; // Continue running
    }
    
    /**
     * Cleanup resources before exit
     */
    private static void cleanup() {
        try {
            logger.info("Cleaning up application resources...");
            
            // Close database connections
            DatabaseConnection.closeAllConnections();
            
            // Close scanner
            if (scanner != null) {
                scanner.close();
            }
            
            logger.info("Application shutdown complete");
            
        } catch (Exception e) {
            logger.error("Error during cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Get integer input with validation
     */
    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. " + prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }
}