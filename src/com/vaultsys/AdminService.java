package com.vaultsys;

import java.sql.*;

/**
 * Admin Service - administrative functions
 * Role-based access - only admins can view this data
 */
public class AdminService {

    /**
     * Views all users with their balances
     * Uses LEFT JOIN to include users even if they have no accounts
     * Useful for system administration and debugging
     */
    public void viewAllUsers() {
        String sql = "SELECT u.user_id, u.username, u.role, a.balance FROM users u LEFT JOIN accounts a ON u.user_id = a.user_id";
        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- All Users ---");
            System.out.printf("%-5s %-15s %-10s %-10s%n", "ID", "Username", "Role", "Balance");
            while (rs.next()) {
                System.out.printf("%-5d %-15s %-10s $%-10.2f%n",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
