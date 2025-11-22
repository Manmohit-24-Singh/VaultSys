package com.vaultsys;

import java.sql.*;
import java.util.Random;

public class SimulationService {

    public void runSimulation() {
        System.out.println("Starting 10,000 Transaction Simulation...");
        long startTime = System.currentTimeMillis();

        // Ensure we have a test user
        int userId = ensureTestUser();
        if (userId == -1) {
            System.out.println("Failed to create test user for simulation.");
            return;
        }

        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect()) {
            conn.setAutoCommit(false); // Batch processing for speed

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Random rand = new Random();

                for (int i = 0; i < 10000; i++) {
                    pstmt.setInt(1, userId); // Using account_id (assuming 1-to-1 for simplicity here, usually need
                                             // lookup)
                    pstmt.setString(2, i % 2 == 0 ? "DEPOSIT" : "WITHDRAWAL");
                    pstmt.setDouble(3, rand.nextDouble() * 100);
                    pstmt.setString(4, "Simulated Transaction #" + i);
                    pstmt.addBatch();

                    if (i % 1000 == 0) {
                        pstmt.executeBatch();
                        System.out.print(".");
                    }
                }
                pstmt.executeBatch();
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

    private int ensureTestUser() {
        // Get the first account ID available
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
