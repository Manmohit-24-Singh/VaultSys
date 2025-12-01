package com.vaultsys;

import java.sql.*;
import java.util.Random;

/**
 * Simulation Service - performance testing tool
 * Demonstrates batch processing for high-volume operations
 * Simulates 10,000 transactions to test database performance
 */
public class SimulationService {

    /**
     * Runs simulation of 10,000 transactions
     * KEY CONCEPTS:
     * - Batch processing (addBatch/executeBatch) for performance
     * - Transaction management (commit/rollback)
     * - Progress indicator (prints dots every 1000 transactions)
     */
    public void runSimulation() {
        System.out.println("Starting 10,000 Transaction Simulation...");
        long startTime = System.currentTimeMillis();

        int userId = ensureTestUser();
        if (userId == -1) {
            System.out.println("Failed to create test user for simulation.");
            return;
        }

        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect()) {
            conn.setAutoCommit(false); // Batch mode for better performance

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Random rand = new Random();

                // Generate 10,000 transactions
                for (int i = 0; i < 10000; i++) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, i % 2 == 0 ? "DEPOSIT" : "WITHDRAWAL");
                    pstmt.setDouble(3, rand.nextDouble() * 100);
                    pstmt.setString(4, "Simulated Transaction #" + i);
                    pstmt.addBatch(); // Add to batch instead of executing immediately

                    // Execute batch every 1000 transactions
                    if (i % 1000 == 0) {
                        pstmt.executeBatch(); // Execute all batched statements
                        System.out.print("."); // Progress indicator
                    }
                }
                pstmt.executeBatch(); // Execute remaining transactions
                conn.commit();
                System.out.println("\nSimulation Complete!");

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
    }

    /**
     * Gets first available account for testing
     * In production, create a test account specifically for simulations
     */
    private int ensureTestUser() {
        String sql = "SELECT account_id FROM accounts LIMIT 1";
        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
