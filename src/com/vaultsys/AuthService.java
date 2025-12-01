package com.vaultsys;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

/**
 * Authentication Service - handles user login and registration
 * This demonstrates security best practices:
 * - Password hashing with SHA-256
 * - Random salt per user (prevents rainbow table attacks)
 * - Password strength validation
 * - Input sanitization with trim()
 */
public class AuthService {

    /**
     * Authenticates user with username and password
     * Retrieves salt from DB and hashes input password with same salt
     * Uses PreparedStatement to prevent SQL injection
     */
    public User login(String username, String password) {
        // Input validation at service layer (defense in depth)
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
            return null;
        }

        // Query retrieves salt along with hash - needed to verify password
        String sql = "SELECT user_id, username, role, password_hash, password_salt FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("password_salt");

                // Hash input password with stored salt, compare with stored hash
                if (hashPassword(password, storedSalt).equals(storedHash)) {
                    return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("role"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers new user with validation
     * Automatically creates a checking account for new users
     * Generates unique salt per user for password security
     */
    public boolean register(String username, String password, String fullName) {
        // Multi-level validation
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
            return false;
        }
        if (username.length() < 3) {
            System.out.println("Error: Username must be at least 3 characters.");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
            return false;
        }
        if (!isPasswordStrong(password)) {
            System.out.println("Error: Password must be at least 8 characters with uppercase, lowercase, and digit.");
            return false;
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            System.out.println("Error: Full name cannot be empty.");
            return false;
        }

        // Generate random salt - unique per user
        String salt = generateSalt();

        String sql = "INSERT INTO users (username, password_hash, password_salt, full_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, hashPassword(password, salt)); // Store hashed password
            pstmt.setString(3, salt); // Store salt for later verification
            pstmt.setString(4, fullName.trim());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Auto-create checking account for convenience
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

    /**
     * Creates a new account for a user
     * Called automatically during registration
     */
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

    /**
     * Generates cryptographically secure random salt
     * Uses SecureRandom (not regular Random) for cryptographic strength
     * 16 bytes provides good security, Base64 encoding for storage
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes password with salt using SHA-256
     * Combining password + salt before hashing prevents rainbow table attacks
     * Even if two users have same password, different salts = different hashes
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt; // Concatenate before hashing
            byte[] encodedhash = digest.digest(saltedPassword.getBytes());

            // Convert bytes to hex string for storage
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

    /**
     * Validates password strength
     * Forces strong passwords to improve security
     * Requirements: 8+ chars, uppercase, lowercase, digit
     */
    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            if (Character.isLowerCase(c))
                hasLower = true;
            if (Character.isDigit(c))
                hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
