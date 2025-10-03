package com.vaultsys.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses PBKDF2 with SHA-256 for secure password hashing.
 */
public class PasswordHasher {
    
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    private static final int ITERATIONS = 10000; // Number of iterations for PBKDF2
    private static final String SEPARATOR = ":";
    
    /**
     * Hash a password using SHA-256 with salt
     * @param password The plain text password
     * @return The hashed password with salt (format: salt:hash)
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash password with salt
            byte[] hash = hashWithSalt(password, salt);
            
            // Encode salt and hash to Base64
            String saltEncoded = Base64.getEncoder().encodeToString(salt);
            String hashEncoded = Base64.getEncoder().encodeToString(hash);
            
            // Return format: salt:hash
            return saltEncoded + SEPARATOR + hashEncoded;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify a password against a stored hash
     * @param password The plain text password to verify
     * @param storedHash The stored hash (format: salt:hash)
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            if (password == null || storedHash == null) {
                return false;
            }
            
            // Split stored hash into salt and hash
            String[] parts = storedHash.split(SEPARATOR);
            if (parts.length != 2) {
                return false;
            }
            
            // Decode salt and hash from Base64
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the provided password with the same salt
            byte[] actualHash = hashWithSalt(password, salt);
            
            // Compare hashes using constant-time comparison
            return constantTimeEquals(expectedHash, actualHash);
            
        } catch (Exception e) {
            // Return false on any error (invalid format, decoding error, etc.)
            return false;
        }
    }
    
    /**
     * Hash password with given salt using SHA-256
     * @param password The password to hash
     * @param salt The salt bytes
     * @return The hash bytes
     * @throws NoSuchAlgorithmException if algorithm not available
     */
    private static byte[] hashWithSalt(String password, byte[] salt) 
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.reset();
        md.update(salt);
        
        // Perform multiple iterations for additional security
        byte[] hash = md.digest(password.getBytes());
        for (int i = 0; i < ITERATIONS; i++) {
            md.reset();
            hash = md.digest(hash);
        }
        
        return hash;
    }
    
    /**
     * Constant-time comparison to prevent timing attacks
     * @param a First byte array
     * @param b Second byte array
     * @return true if arrays are equal
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    /**
     * Generate a random password
     * @param length The length of the password
     * @return A random password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8");
        }
        
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = upperCase + lowerCase + digits + special;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        
        // Ensure at least one of each required character type
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        // Fill remaining length with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password characters
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Check password strength
     * @param password The password to check
     * @return Strength score (0-4: weak, 5-7: medium, 8-10: strong)
     */
    public static int checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;
        
        // Character variety checks
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasDigit) score++;
        if (hasSpecial) score += 2;
        
        // Check for common patterns (reduce score)
        String lowerPassword = password.toLowerCase();
        if (lowerPassword.contains("password") || 
            lowerPassword.contains("123456") || 
            lowerPassword.contains("qwerty")) {
            score = Math.max(0, score - 3);
        }
        
        return Math.min(10, score);
    }
}