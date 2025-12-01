package com.vaultsys;

import java.util.Scanner;

/**
 * Main entry point for VaultSys Banking CLI
 * menu-driven architecture and state management with currentUser
 */
public class Main {
    // Scanner for all user input - shared across methods to avoid resource leaks
    private static final Scanner scanner = new Scanner(System.in);

    // Session management: null when logged out, User object when logged in
    private static User currentUser = null;

    // Service layer instances - demonstrates separation of concerns
    private static final AuthService authService = new AuthService();
    private static final BankingService bankingService = new BankingService();
    private static final AdminService adminService = new AdminService();
    private static final SimulationService simulationService = new SimulationService();

    public static void main(String[] args) {
        System.out.println("Welcome to VaultSys Banking (Student Edition)");

        // Main application loop - runs until user exits
        while (true) {
            if (currentUser == null) {
                showAuthMenu(); // Not logged in: show login/register
            } else {
                showMainMenu(); // Logged in: show banking operations
            }
        }
    }

    /**
     * Authentication menu for login and registration
     * Mention input validation prevents empty credentials
     */
    private static void showAuthMenu() {
        System.out.println("\n--- Auth Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegistration();
                break;
            case "3":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    /**
     * Handles user login with input validation
     * Validation happens at both UI and service layer (defense in depth)
     */
    private static void handleLogin() {
        System.out.print("Username: ");
        String loginUser = scanner.nextLine();
        System.out.print("Password: ");
        String loginPass = scanner.nextLine();

        // UI-level validation before calling service
        if (loginUser == null || loginUser.trim().isEmpty() ||
                loginPass == null || loginPass.trim().isEmpty()) {
            System.out.println("Login failed: Username and password cannot be empty.");
            return;
        }

        // Delegate authentication to AuthService
        currentUser = authService.login(loginUser, loginPass);
        if (currentUser != null) {
            System.out.println("Login successful! Welcome " + currentUser.getUsername());
        } else {
            System.out.println("Login failed.");
        }
    }

    /**
     * Handles new user registration
     * AuthService validates password strength and creates account automatically
     */
    private static void handleRegistration() {
        System.out.print("Username: ");
        String regUser = scanner.nextLine();
        System.out.print("Password: ");
        String regPass = scanner.nextLine();
        System.out.print("Full Name: ");
        String regName = scanner.nextLine();

        // Service layer handles validation and provides clear error messages
        if (authService.register(regUser, regPass, regName)) {
            System.out.println("Registration successful! Please login.");
        } else {
            System.out.println("Registration failed.");
        }
    }

    /**
     * Main banking menu for logged-in users
     * Role-based menu shows admin options only to admins
     */
    private static void showMainMenu() {
        System.out.println("\n--- Main Menu (" + currentUser.getUsername() + ") ---");
        System.out.println("1. View Balance");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Transaction History");
        System.out.println("6. Logout");

        // Admin-only options (role-based access control)
        if ("ADMIN".equals(currentUser.getRole())) {
            System.out.println("7. [ADMIN] View All Users");
            System.out.println("8. [ADMIN] Run Simulation (10k txns)");
        }

        System.out.print("Choose: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                bankingService.viewBalance(currentUser);
                break;
            case "2":
                handleDeposit();
                break;
            case "3":
                handleWithdrawal();
                break;
            case "4":
                handleTransfer();
                break;
            case "5":
                bankingService.viewHistory(currentUser);
                break;
            case "6":
                currentUser = null; // Clear session
                System.out.println("Logged out.");
                break;
            case "7":
                handleAdminViewUsers();
                break;
            case "8":
                handleAdminSimulation();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    /**
     * Handles deposit with amount validation
     * Triple validation - null check, positive check, numeric check
     */
    private static void handleDeposit() {
        System.out.print("Amount to deposit: ");
        String depInput = scanner.nextLine();

        // Validation 1: Check for empty input
        if (depInput == null || depInput.trim().isEmpty()) {
            System.out.println("Invalid amount: cannot be empty.");
            return;
        }

        try {
            double depAmount = Double.parseDouble(depInput.trim());

            // Validation 2: Check for positive amount
            if (depAmount <= 0) {
                System.out.println("Invalid amount: must be positive.");
                return;
            }

            if (bankingService.deposit(currentUser, depAmount)) {
                System.out.println("Deposit successful.");
            } else {
                System.out.println("Deposit failed.");
            }
        } catch (NumberFormatException e) {
            // Validation 3: Check for numeric value
            System.out.println("Invalid amount: must be a number.");
        }
    }

    /**
     * Handles withdrawal with balance checking
     * BankingService checks balance before allowing withdrawal
     */
    private static void handleWithdrawal() {
        System.out.print("Amount to withdraw: ");
        String withInput = scanner.nextLine();

        if (withInput == null || withInput.trim().isEmpty()) {
            System.out.println("Invalid amount: cannot be empty.");
            return;
        }

        try {
            double withAmount = Double.parseDouble(withInput.trim());

            if (withAmount <= 0) {
                System.out.println("Invalid amount: must be positive.");
                return;
            }

            if (bankingService.withdraw(currentUser, withAmount)) {
                System.out.println("Withdrawal successful.");
            } else {
                System.out.println("Withdrawal failed (Insufficient funds?).");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount: must be a number.");
        }
    }

    /**
     * Handles transfer between users
     * Transfers are atomic - both accounts update or neither does
     */
    private static void handleTransfer() {
        System.out.print("Target Username: ");
        String targetUser = scanner.nextLine();

        if (targetUser == null || targetUser.trim().isEmpty()) {
            System.out.println("Transfer failed: username cannot be empty.");
            return;
        }

        System.out.print("Amount to transfer: ");
        String transInput = scanner.nextLine();

        if (transInput == null || transInput.trim().isEmpty()) {
            System.out.println("Invalid amount: cannot be empty.");
            return;
        }

        try {
            double transAmount = Double.parseDouble(transInput.trim());

            if (transAmount <= 0) {
                System.out.println("Invalid amount: must be positive.");
                return;
            }

            // BankingService ensures atomic transfer with rollback on failure
            if (bankingService.transfer(currentUser, targetUser.trim(), transAmount)) {
                System.out.println("Transfer successful.");
            } else {
                System.out.println("Transfer failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount: must be a number.");
        }
    }

    /**
     * Admin function to view all users
     * Role checking prevents unauthorized access
     */
    private static void handleAdminViewUsers() {
        if ("ADMIN".equals(currentUser.getRole())) {
            adminService.viewAllUsers();
        } else {
            System.out.println("Access Denied.");
        }
    }

    /**
     * Admin function to run performance simulation
     * Demonstrates batch processing for high-volume transactions
     */
    private static void handleAdminSimulation() {
        if ("ADMIN".equals(currentUser.getRole())) {
            simulationService.runSimulation();
        } else {
            System.out.println("Access Denied.");
        }
    }
}
