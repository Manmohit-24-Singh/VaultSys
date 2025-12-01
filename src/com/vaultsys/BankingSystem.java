package com.vaultsys; // Package declaration

// Import SQL classes for database operations
import java.sql.*; // Wildcard import for Connection, PreparedStatement, ResultSet, Statement
import java.util.Random; // For generating random numbers in simulation

/**
 * BankingSystem - Core Business Logic Class
 * Contains 16 banking functions: 10 user operations + 6 admin operations
 * Uses PreparedStatements to prevent SQL injection attacks
 */
public class BankingSystem {

    // ========== USER FUNCTIONS (1-10) ==========

    /**
     * Function 1: Register a new user account
     * Returns boolean to indicate success/failure (username
     * uniqueness constraint)
     * 
     * @param username Desired username (must be unique)
     * @param password User password (plain text - should be hashed in production!)
     * @return true if registration successful, false if username already exists
     */
    public boolean register(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)"; // Parameterized SQL query
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username); // Set first placeholder (?) to username
            pstmt.setString(2, password); // Set second placeholder (?) to password
            pstmt.executeUpdate(); // Execute INSERT statement
            return true; // Registration successful
        } catch (Exception e) {
            return false; // Registration failed (likely duplicate username)
        }
    }

    /**
     * Function 2: Authenticate user login
     * Uses SELECT to verify credentials without exposing data
     * 
     * @param username Username to check
     * @param password Password to verify
     * @return true if credentials match, false otherwise
     */
    public boolean login(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?"; // Check if user exists with matching
                                                                                 // credentials
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username); // Bind username parameter
            pstmt.setString(2, password); // Bind password parameter
            return pstmt.executeQuery().next(); // Returns true if at least one row found (valid credentials)
        } catch (Exception e) {
            return false; // Login failed (connection error or invalid credentials)
        }
    }

    /**
     * Function 3: Deposit money into user account
     * Validates amount before modifying balance to prevent negative
     * deposits
     * 
     * @param username User to deposit money into
     * @param amount   Amount to deposit (must be positive)
     */
    public void deposit(String username, double amount) {
        if (amount <= 0) // Reject non-positive amounts
            return;
        updateBalance(username, amount); // Add amount to user's balance (helper method)
        logTransaction(username, "DEPOSIT", amount); // Record transaction in audit trail
    }

    /**
     * Function 4: Withdraw money from user account
     * Checks freeze status AND sufficient funds before allowing
     * withdrawal
     * 
     * @param username User to withdraw from
     * @param amount   Amount to withdraw
     * @return true if withdrawal successful, false if frozen or insufficient funds
     */
    public boolean withdraw(String username, double amount) {
        if (isFrozen(username) || getBalance(username) < amount) // Check freeze status and balance
            return false; // Withdrawal denied
        updateBalance(username, -amount); // Subtract amount from balance (negative value)
        logTransaction(username, "WITHDRAW", amount); // Log the withdrawal
        return true; // Withdrawal successful
    }

    /**
     * Function 5: Transfer money between users (ACID-compliant)
     * Uses TRANSACTIONS (setAutoCommit(false) + commit) to ensure
     * atomicity
     * Either BOTH updates succeed, or NEITHER happens (no partial transfers)
     * 
     * @param fromUser Sender username
     * @param toUser   Recipient username
     * @param amount   Amount to transfer
     * @return true if transfer successful, false if frozen or insufficient funds
     */
    public boolean transfer(String fromUser, String toUser, double amount) {
        if (isFrozen(fromUser) || getBalance(fromUser) < amount) // Validate sender account
            return false; // Transfer denied
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false); // BEGIN TRANSACTION - disable auto-commit for atomic operation
            try (PreparedStatement sub = conn
                    .prepareStatement("UPDATE users SET balance = balance - ? WHERE username = ?"); // Deduct from
                                                                                                    // sender
                    PreparedStatement add = conn
                            .prepareStatement("UPDATE users SET balance = balance + ? WHERE username = ?")) { // Add to
                                                                                                              // recipient
                sub.setDouble(1, amount); // Set amount to deduct
                sub.setString(2, fromUser); // Set sender username
                sub.executeUpdate(); // Execute deduction
                add.setDouble(1, amount); // Set amount to add
                add.setString(2, toUser); // Set recipient username
                add.executeUpdate(); // Execute addition
            }
            conn.commit(); // COMMIT TRANSACTION - persist both changes atomically
            logTransaction(fromUser, "TRANSFER_OUT", amount); // Log outgoing transfer
            logTransaction(toUser, "TRANSFER_IN", amount); // Log incoming transfer
            return true; // Transfer successful
        } catch (Exception e) {
            return false; // Transfer failed (automatic rollback on exception)
        }
    }

    /**
     * Function 6: Change user password
     * Simple UPDATE statement without password verification (assumes
     * already authenticated)
     * 
     * @param username User whose password to change
     * @param newPass  New password to set
     */
    public void changePassword(String username, String newPass) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET password = ? WHERE username = ?")) {
            pstmt.setString(1, newPass); // Set new password
            pstmt.setString(2, username); // Identify user by username
            pstmt.executeUpdate(); // Execute update
        } catch (Exception e) {
            e.printStackTrace(); // Print error if update fails
        }
    }

    /**
     * Function 7: Delete user account permanently
     * CASCADE delete would also remove transactions if foreign key
     * constraint existed
     * 
     * @param username User account to delete
     */
    public void closeAccount(String username) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
            pstmt.setString(1, username); // Identify user to delete
            pstmt.executeUpdate(); // Execute deletion
        } catch (Exception e) {
            e.printStackTrace(); // Print error if deletion fails
        }
    }

    /**
     * Function 8: View user's transaction history
     * Uses subquery to join users and transactions, with ORDER BY
     * DESC and LIMIT
     * 
     * @param username User whose transactions to view
     */
    public void viewTransactions(String username) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT * FROM transactions WHERE user_id = (SELECT id FROM users WHERE username = ?) ORDER BY id DESC LIMIT 5")) {
            pstmt.setString(1, username); // Find user's ID via subquery
            ResultSet rs = pstmt.executeQuery(); // Execute and get results
            System.out.println("\n--- Last 5 Transactions ---"); // Header
            while (rs.next()) // Iterate through result rows
                System.out.println(rs.getString("type") + ": $" + rs.getDouble("amount")); // Print type and amount
        } catch (Exception e) {
            e.printStackTrace(); // Print error if query fails
        }
    }

    /**
     * Function 9: Calculate simple interest
     * Pure function (no side effects) - demonstrates financial
     * calculation logic
     * Formula: I = P * R * T (Interest = Principal × Rate × Time)
     * 
     * @param principal Initial amount
     * @param rate      Interest rate as percentage (e.g., 5 for 5%)
     * @param years     Duration in years
     * @return Calculated interest amount
     */
    public double calculateInterest(double principal, double rate, int years) {
        return principal * (rate / 100) * years; // Convert percentage to decimal and multiply
    }

    /**
     * Function 10: View user's cashflow report (money in vs out)
     * Uses helper method to reduce code duplication
     * 
     * @param username User whose cashflow to analyze
     */
    public void viewUserCashflow(String username) {
        String sql = "SELECT type, SUM(amount) FROM transactions WHERE user_id = (SELECT id FROM users WHERE username = ?) GROUP BY type"; // Aggregate
                                                                                                                                           // by
                                                                                                                                           // transaction
                                                                                                                                           // type
        printCashflowReport(sql, username); // Delegate to helper method
    }

    // ========== ADMIN FUNCTIONS (11-16) ==========

    /**
     * Function 11: Freeze or unfreeze a user account
     * Admin control feature - frozen accounts cannot
     * withdraw/transfer
     * 
     * @param username User to freeze/unfreeze
     * @param freeze   true to freeze, false to unfreeze
     */
    public void freezeAccount(String username, boolean freeze) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET is_frozen = ? WHERE username = ?")) {
            pstmt.setBoolean(1, freeze); // Set freeze status
            pstmt.setString(2, username); // Identify user
            int rows = pstmt.executeUpdate(); // Returns number of rows affected
            if (rows > 0) // Check if user was found
                System.out.println("User " + username + (freeze ? " Frozen ❄️" : " Unfrozen ✅"));
            else
                System.out.println("❌ User not found."); // No user matched
        } catch (Exception e) {
            e.printStackTrace(); // Print error
        }
    }

    /**
     * Function 12: View all transactions system-wide (admin dashboard)
     * Uses Statement (not PreparedStatement) since no user input -
     * shows trade-offs
     */
    public void viewAllTransactions() {
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY id DESC LIMIT 10"); // Get latest 10
                                                                                                      // transactions
            System.out.println("\n--- Global Transaction Log ---"); // Header
            while (rs.next()) // Iterate through results
                System.out.println("User ID: " + rs.getInt("user_id") + " | " + rs.getString("type") + " | $"
                        + rs.getDouble("amount")); // Print transaction details
        } catch (Exception e) {
            e.printStackTrace(); // Print error
        }
    }

    /**
     * Function 13: View total money in the banking system
     * Uses aggregate function SUM() to calculate total reserves
     */
    public void viewTotalReserves() {
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT SUM(balance) FROM users"); // Sum all user balances
            if (rs.next()) // Check if result exists
                System.out.println("💰 Total Reserves: $" + rs.getDouble(1)); // Get first column (sum result)
        } catch (Exception e) {
            e.printStackTrace(); // Print error
        }
    }

    /**
     * Function 14: View user registration insights
     * Simple reporting query showing user adoption over time
     */
    public void viewInsights() {
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT username, created_at FROM users"); // Get all users with join date
            System.out.println("\n--- User Insights ---"); // Header
            while (rs.next()) // Iterate through users
                System.out.println("User: " + rs.getString(1) + " | Joined: " + rs.getString(2)); // Print username and
                                                                                                  // timestamp
        } catch (Exception e) {
            e.printStackTrace(); // Print error
        }
    }

    /**
     * Function 15: View system-wide cashflow (all users combined)
     * Same logic as user cashflow but no username filter
     */
    public void viewReserveCashflow() {
        String sql = "SELECT type, SUM(amount) FROM transactions GROUP BY type"; // Aggregate all transactions by type
        printCashflowReport(sql, null); // null means no username filter (system-wide)
    }

    /**
     * Function 16: Simulate 10,000 transactions for stress testing
     * Demonstrates BATCH processing - faster than 10k individual
     * inserts
     * Uses addBatch() + executeBatch() for performance optimization
     */
    public void runSimulation() {
        System.out.println("🚀 Running 10,000 Transaction Simulation...");

        // FIX: Use User ID -1 (Dummy ID) so it doesn't mess up real user data
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO transactions (user_id, type, amount) VALUES (-1, 'SIMULATION', ?)")) {

            conn.setAutoCommit(false); // Batch Mode - disable auto-commit for batch processing
            Random rand = new Random(); // Random number generator for transaction amounts
            for (int i = 0; i < 10000; i++) { // Loop 10,000 times
                pstmt.setDouble(1, rand.nextDouble() * 100); // Random amount between $0-$100
                pstmt.addBatch(); // Add to batch queue (doesn't execute immediately)
            }
            pstmt.executeBatch(); // Execute all 10,000 INSERTs in one network round-trip
            conn.commit(); // Commit the transaction
            System.out.println("✅ Success! 10,000 transactions persisted to DB.");
        } catch (Exception e) {
            e.printStackTrace(); // Print error
        }
    }

    // ========== HELPER METHODS (Private utility functions) ==========

    /**
     * Get user's current balance
     * Public method since Main.java needs to display balance
     * 
     * @param username User to check
     * @return Current balance or 0 if user not found
     */
    public double getBalance(String username) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement("SELECT balance FROM users WHERE username = ?")) {
            pstmt.setString(1, username); // Find user by username
            ResultSet rs = pstmt.executeQuery(); // Execute query
            if (rs.next()) // Check if user exists
                return rs.getDouble(1); // Return balance (first column)
        } catch (Exception e) {
        }
        return 0; // Default to 0 if error or user not found
    }

    /**
     * Check if user account is frozen
     * Private helper to encapsulate freeze check logic
     * 
     * @param username User to check
     * @return true if frozen, false otherwise
     */
    private boolean isFrozen(String username) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement("SELECT is_frozen FROM users WHERE username = ?")) {
            pstmt.setString(1, username); // Find user
            ResultSet rs = pstmt.executeQuery(); // Execute
            if (rs.next()) // If user exists
                return rs.getBoolean(1); // Return freeze status
        } catch (Exception e) {
        }
        return false; // Default to not frozen if error
    }

    /**
     * Update user balance by adding/subtracting amount
     * Uses "balance + ?" to allow both positive and negative
     * adjustments
     * 
     * @param username User to update
     * @param amount   Amount to add (negative for subtraction)
     */
    private void updateBalance(String username, double amount) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn
                        .prepareStatement("UPDATE users SET balance = balance + ? WHERE username = ?")) {
            pstmt.setDouble(1, amount); // Amount to add (can be negative)
            pstmt.setString(2, username); // User to update
            pstmt.executeUpdate(); // Execute update
        } catch (Exception e) {
        }
    }

    /**
     * Log a transaction to the audit trail
     * Every financial operation calls this for complete transaction
     * history
     * 
     * @param username User performing transaction
     * @param type     Transaction type (DEPOSIT, WITHDRAW, TRANSFER_IN, etc.)
     * @param amount   Transaction amount
     */
    private void logTransaction(String username, String type, double amount) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO transactions (user_id, type, amount) VALUES ((SELECT id FROM users WHERE username = ?), ?, ?)")) {
            pstmt.setString(1, username); // Find user ID via subquery
            pstmt.setString(2, type); // Set transaction type
            pstmt.setDouble(3, amount); // Set amount
            pstmt.executeUpdate(); // Insert transaction record
        } catch (Exception e) {
        }
    }

    /**
     * Print cashflow report (money in vs money out)
     * Reusable method for both user and system-wide cashflow
     * 
     * @param sql      SQL query with GROUP BY type
     * @param username Username for user-specific report, or null for system-wide
     */
    private void printCashflowReport(String sql, String username) {
        double in = 0, out = 0; // Accumulators for money in/out
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (username != null) // If user-specific report
                pstmt.setString(1, username); // Bind username parameter
            ResultSet rs = pstmt.executeQuery(); // Execute query
            while (rs.next()) { // Iterate through transaction types
                String type = rs.getString(1); // Get transaction type
                double amt = rs.getDouble(2); // Get sum for that type
                if (type.contains("DEPOSIT") || type.contains("IN")) // Check if money coming in
                    in += amt; // Add to money in
                else
                    out += amt; // Otherwise it's money out
            }
            System.out.println("\n--- Cashflow Report ---"); // Print header
            System.out.println("Money IN:  $" + in); // Total incoming
            System.out.println("Money OUT: $" + out); // Total outgoing
            System.out.println("Net Flow:  $" + (in - out)); // Net cashflow (profit/loss)
        } catch (Exception e) {
            e.printStackTrace(); // Print error
        }
    }
}