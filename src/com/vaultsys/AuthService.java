package com.vaultsys;

import java.security.MessageDigest;
import java.sql.*;

public class AuthService {

    public User login(String username, String password) {
        String sql = "SELECT user_id, username, role, password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (hashPassword(password).equals(storedHash)) {
                    return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("role"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password, String fullName) {
        String sql = "INSERT INTO users (username, password_hash, full_name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.setString(3, fullName);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Auto-create a checking account for new users
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        createAccount(generatedKeys.getInt(1), "CHECKING");
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error: Username likely already exists.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createAccount(int userId, String type) {
        String sql = "INSERT INTO accounts (user_id, account_type) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
