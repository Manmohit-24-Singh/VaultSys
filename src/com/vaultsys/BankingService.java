package com.vaultsys;

import java.sql.*;

public class BankingService {

    public void viewBalance(User user) {
        String sql = "SELECT account_type, balance FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--- Your Accounts ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%s: $%.2f%n", rs.getString("account_type"), rs.getDouble("balance"));
            }
            if (!found)
                System.out.println("No accounts found.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deposit(User user, double amount) {
        if (amount <= 0)
            return false;
        int accountId = getPrimaryAccountId(user.getId());
        if (accountId == -1)
            return false;

        return performTransaction(accountId, "DEPOSIT", amount, "ATM Deposit");
    }

    public boolean withdraw(User user, double amount) {
        if (amount <= 0)
            return false;
        int accountId = getPrimaryAccountId(user.getId());
        if (accountId == -1)
            return false;

        double currentBalance = getBalance(accountId);
        if (currentBalance < amount) {
            System.out.println("Insufficient funds.");
            return false;
        }

        return performTransaction(accountId, "WITHDRAWAL", -amount, "ATM Withdrawal");
    }

    public boolean transfer(User user, String targetUsername, double amount) {
        if (amount <= 0)
            return false;
        int sourceAccountId = getPrimaryAccountId(user.getId());
        int targetUserId = getUserIdByUsername(targetUsername);

        if (sourceAccountId == -1 || targetUserId == -1) {
            System.out.println("Invalid account or user.");
            return false;
        }

        int targetAccountId = getPrimaryAccountId(targetUserId);
        if (targetAccountId == -1) {
            System.out.println("Target user has no account.");
            return false;
        }

        double currentBalance = getBalance(sourceAccountId);
        if (currentBalance < amount) {
            System.out.println("Insufficient funds.");
            return false;
        }

        // Transactional transfer
        try (Connection conn = DatabaseHelper.connect()) {
            conn.setAutoCommit(false);
            try {
                // Deduct from source
                updateBalance(conn, sourceAccountId, -amount);
                logTransaction(conn, sourceAccountId, "TRANSFER_OUT", -amount, "Transfer to " + targetUsername);

                // Add to target
                updateBalance(conn, targetAccountId, amount);
                logTransaction(conn, targetAccountId, "TRANSFER_IN", amount, "Transfer from " + user.getUsername());

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void viewHistory(User user) {
        int accountId = getPrimaryAccountId(user.getId());
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC LIMIT 10";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--- Recent Transactions ---");
            while (rs.next()) {
                System.out.printf("%s | %s | $%.2f | %s%n",
                        rs.getTimestamp("timestamp"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helpers
    private int getPrimaryAccountId(int userId) {
        String sql = "SELECT account_id FROM accounts WHERE user_id = ? LIMIT 1";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt("account_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt("user_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private double getBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getDouble("balance");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private boolean performTransaction(int accountId, String type, double amount, String desc) {
        try (Connection conn = DatabaseHelper.connect()) {
            conn.setAutoCommit(false);
            try {
                updateBalance(conn, accountId, amount);
                logTransaction(conn, accountId, type, amount, desc);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateBalance(Connection conn, int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        }
    }

    private void logTransaction(Connection conn, int accountId, String type, double amount, String desc)
            throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, desc);
            pstmt.executeUpdate();
        }
    }
}
