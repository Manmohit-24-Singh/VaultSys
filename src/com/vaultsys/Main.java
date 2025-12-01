package com.vaultsys; // Package declaration

import java.util.Scanner; // For reading user input from console

/**
 * Main - CLI Interface for VaultSys Banking System
 * This implements a state machine with 3 states:
 * 1. Not Logged In (Welcome Menu)
 * 2. Admin Dashboard
 * 3. User Dashboard
 */
public class Main {
    public static void main(String[] args) {
        Database.init(); // Initialize database schema (creates tables if needed)
        BankingSystem bank = new BankingSystem(); // Create instance of business logic class
        Scanner scanner = new Scanner(System.in); // Create Scanner for reading console input
        String currentUser = null; // Stores logged-in username (null = not logged in)
        boolean isAdmin = false; // Admin authentication flag

        System.out.println("=== VaultSys Banking (15+ Functions) ==="); // Welcome header

        // Main application loop - runs until user exits
        while (true) {
            // ========== STATE 1: NOT LOGGED IN (Welcome Menu) ==========
            if (currentUser == null && !isAdmin) { // Check if neither user nor admin is logged in
                System.out.println("\n--- Welcome ---"); // Menu header
                System.out.println("1. User Login"); // Option: User authentication
                System.out.println("2. Admin Login"); // Option: Admin authentication
                System.out.println("3. Register New Account"); // Option: Create new user
                System.out.println("4. Exit"); // Option: Quit application
                System.out.print("Select: "); // Prompt for choice

                String choice = scanner.next(); // Read user's menu selection

                if (choice.equals("1")) { // User Login selected
                    System.out.print("Username: "); // Prompt for username
                    String u = scanner.next(); // Read username
                    System.out.print("Password: "); // Prompt for password
                    String p = scanner.next(); // Read password
                    if (bank.login(u, p)) { // Verify credentials with BankingSystem
                        currentUser = u; // Set current user (enter user state)
                        System.out.println("✅ Login Successful.");
                    } else
                        System.out.println("❌ Invalid credentials."); // Login failed
                } else if (choice.equals("2")) { // Admin Login selected
                    System.out.print("Enter Admin Password: "); // Prompt for admin password
                    if (scanner.next().equals("admin123")) { // Hardcoded admin password (simple authentication)
                        isAdmin = true; // Set admin flag (enter admin state)
                        System.out.println("✅ Admin Access Granted.");
                    } else
                        System.out.println("❌ Access Denied."); // Wrong password
                } else if (choice.equals("3")) { // Register selected
                    System.out.print("Choose Username: "); // Prompt for desired username
                    String u = scanner.next(); // Read username
                    System.out.print("Choose Password: "); // Prompt for password
                    String p = scanner.next(); // Read password
                    if (bank.register(u, p)) // Attempt to create account
                        System.out.println("✅ Account Created! Please Login.");
                    else
                        System.out.println("❌ Username taken."); // Username already exists
                } else if (choice.equals("4")) // Exit selected
                    break; // Exit main loop (end program)
            }
            // ========== STATE 2: ADMIN DASHBOARD ==========
            else if (isAdmin) { // Check if admin is logged in
                System.out.println("\n--- 🔒 ADMIN DASHBOARD ---"); // Admin menu header
                System.out.println("1. View All Transactions"); // Option: See all system transactions
                System.out.println("2. View Total Reserves"); // Option: See total bank balance
                System.out.println("3. View Insights (User List)"); // Option: See user registration data
                System.out.println("4. View System Cashflow"); // Option: Money in vs out (system-wide)
                System.out.println("5. Freeze User Account"); // Option: Lock user account
                System.out.println("6. Run 10k Simulation"); // Option: Stress test with batch inserts
                System.out.println("7. Logout"); // Option: Return to welcome menu
                System.out.print("Select: "); // Prompt for choice

                String choice = scanner.next(); // Read admin's selection

                if (choice.equals("1")) // View All Transactions
                    bank.viewAllTransactions(); // Call Function 12
                else if (choice.equals("2")) // View Total Reserves
                    bank.viewTotalReserves(); // Call Function 13
                else if (choice.equals("3")) // View Insights
                    bank.viewInsights(); // Call Function 14
                else if (choice.equals("4")) // View System Cashflow
                    bank.viewReserveCashflow(); // Call Function 15
                else if (choice.equals("5")) { // Freeze User Account
                    System.out.print("Username to Freeze: "); // Prompt for username
                    bank.freezeAccount(scanner.next(), true); // Call Function 11 (freeze = true)
                } else if (choice.equals("6")) // Run 10k Simulation
                    bank.runSimulation(); // Call Function 16 (batch inserts)
                else if (choice.equals("7")) { // Logout
                    isAdmin = false; // Clear admin flag (return to welcome state)
                    System.out.println("🔒 Admin Logged Out.");
                }
            }
            // ========== STATE 3: USER DASHBOARD ==========
            else { // User is logged in (currentUser != null)
                System.out.println("\n--- 👤 User Menu: " + currentUser + " ---"); // User menu header with username
                System.out.println("1. Check Balance"); // Option: View current balance
                System.out.println("2. Deposit Funds"); // Option: Add money
                System.out.println("3. Withdraw Funds"); // Option: Remove money
                System.out.println("4. Transfer Money"); // Option: Send money to another user
                System.out.println("5. View My Transactions"); // Option: See transaction history
                System.out.println("6. Calculate Interest"); // Option: Interest calculator
                System.out.println("7. View My Cashflow"); // Option: Personal income/expense report
                System.out.println("8. Change Password"); // Option: Update password
                System.out.println("9. Close Account"); // Option: Delete account permanently
                System.out.println("0. Logout"); // Option: Return to welcome menu
                System.out.print("Select: "); // Prompt for choice

                String choice = scanner.next(); // Read user's selection

                if (choice.equals("1")) // Check Balance
                    System.out.println("Balance: $" + bank.getBalance(currentUser)); // Display balance
                else if (choice.equals("2")) { // Deposit Funds
                    System.out.print("Amount: "); // Prompt for deposit amount
                    bank.deposit(currentUser, scanner.nextDouble()); // Call Function 3
                } else if (choice.equals("3")) { // Withdraw Funds
                    System.out.print("Amount: "); // Prompt for withdrawal amount
                    if (!bank.withdraw(currentUser, scanner.nextDouble())) // Call Function 4
                        System.out.println("❌ Failed."); // Insufficient funds or frozen account
                } else if (choice.equals("4")) { // Transfer Money
                    System.out.print("To User: "); // Prompt for recipient username
                    String to = scanner.next(); // Read recipient
                    System.out.print("Amount: "); // Prompt for transfer amount
                    if (!bank.transfer(currentUser, to, scanner.nextDouble())) // Call Function 5 (atomic)
                        System.out.println("❌ Transfer Failed."); // Insufficient funds or frozen
                } else if (choice.equals("5")) // View My Transactions
                    bank.viewTransactions(currentUser); // Call Function 8 (last 5 transactions)
                else if (choice.equals("6")) { // Calculate Interest
                    System.out.print("Principal: "); // Prompt for principal amount
                    double p = scanner.nextDouble(); // Read principal
                    System.out.print("Rate %: "); // Prompt for interest rate
                    double r = scanner.nextDouble(); // Read rate
                    System.out.print("Years: "); // Prompt for time period
                    int t = scanner.nextInt(); // Read years
                    System.out.println("Projected Interest: $" + bank.calculateInterest(p, r, t)); // Call Function 9
                } else if (choice.equals("7")) // View My Cashflow
                    bank.viewUserCashflow(currentUser); // Call Function 10 (money in vs out)
                else if (choice.equals("8")) { // Change Password
                    System.out.print("New Password: "); // Prompt for new password
                    bank.changePassword(currentUser, scanner.next()); // Call Function 6
                    System.out.println("✅ Password Changed.");
                } else if (choice.equals("9")) { // Close Account
                    System.out.print("Type 'CONFIRM' to delete account: "); // Confirmation prompt
                    if (scanner.next().equals("CONFIRM")) { // Verify user wants to delete
                        bank.closeAccount(currentUser); // Call Function 7 (delete user)
                        currentUser = null; // Clear current user (return to welcome state)
                        System.out.println("Account Closed.");
                    }
                } else if (choice.equals("0")) // Logout
                    currentUser = null; // Clear current user (return to welcome state)
            }
        } // End of while loop
    } // End of main method
} // End of Main class