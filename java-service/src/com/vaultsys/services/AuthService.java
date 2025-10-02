package com.vaultsys.services;

import com.vaultsys.dao.interfaces.IUserDAO;
import com.vaultsys.models.User;
import com.vaultsys.exceptions.AuthenticationException;
import com.vaultsys.exceptions.DatabaseException;
import com.vaultsys.utils.PasswordHasher;
import com.vaultsys.utils.Logger;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service class for authentication and authorization operations.
 * Handles user login, registration, password management, and session tracking.
 */
public class AuthService {
    
    private final IUserDAO userDAO;
    private static final Logger logger = Logger.getInstance();
    
    // Password validation patterns
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    
    public AuthService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The plain text password
     * @return The authenticated user
     * @throws AuthenticationException if authentication fails
     */
    public User login(String username, String password) throws AuthenticationException {
        try {
            logger.info("Login attempt for user: " + username);
            
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                throw new AuthenticationException("Username is required");
            }
            if (password == null || password.isEmpty()) {
                throw new AuthenticationException("Password is required");
            }
            
            // Find user by username
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (!userOpt.isPresent()) {
                logger.warn("Login failed - user not found: " + username);
                throw new AuthenticationException("Invalid username or password");
            }
            
            User user = userOpt.get();
            
            // Check if user is active
            if (!user.isActive()) {
                logger.warn("Login failed - inactive account: " + username);
                throw new AuthenticationException("Account is inactive. Please contact support.");
            }
            
            // Verify password
            if (!PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
                logger.warn("Login failed - incorrect password for: " + username);
                throw new AuthenticationException("Invalid username or password");
            }
            
            // Update last login timestamp
            userDAO.updateLastLogin(user.getUserId());
            user.updateLastLogin();
            
            logger.info("Login successful for user: " + username);
            return user;
            
        } catch (DatabaseException e) {
            logger.error("Database error during login: " + e.getMessage());
            throw new AuthenticationException("Authentication service unavailable. Please try again later.");
        }
    }
    
    /**
     * Register a new user
     * @param user The user object with registration details
     * @param password The plain text password
     * @return The created user
     * @throws AuthenticationException if registration fails
     */
    public User register(User user, String password) throws AuthenticationException {
        try {
            logger.info("Registration attempt for username: " + user.getUsername());
            
            // Validate user data
            validateUserData(user, password);
            
            // Check if username already exists
            if (userDAO.usernameExists(user.getUsername())) {
                logger.warn("Registration failed - username exists: " + user.getUsername());
                throw new AuthenticationException("Username already exists");
            }
            
            // Check if email already exists
            if (userDAO.emailExists(user.getEmail())) {
                logger.warn("Registration failed - email exists: " + user.getEmail());
                throw new AuthenticationException("Email already registered");
            }
            
            // Hash the password
            String passwordHash = PasswordHasher.hashPassword(password);
            user.setPasswordHash(passwordHash);
            
            // Set default role if not specified
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("CUSTOMER");
            }
            
            // Create user
            User createdUser = userDAO.create(user);
            logger.info("User registered successfully: " + user.getUsername());
            
            return createdUser;
            
        } catch (DatabaseException e) {
            logger.error("Database error during registration: " + e.getMessage());
            throw new AuthenticationException("Registration service unavailable. Please try again later.");
        }
    }
    
    /**
     * Change user password
     * @param userId The user ID
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if password changed successfully
     * @throws AuthenticationException if password change fails
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) 
            throws AuthenticationException {
        try {
            logger.info("Password change attempt for user ID: " + userId);
            
            // Validate new password
            validatePassword(newPassword);
            
            // Find user
            Optional<User> userOpt = userDAO.findById(userId);
            if (!userOpt.isPresent()) {
                throw new AuthenticationException("User not found");
            }
            
            User user = userOpt.get();
            
            // Verify old password
            if (!PasswordHasher.verifyPassword(oldPassword, user.getPasswordHash())) {
                logger.warn("Password change failed - incorrect old password for user ID: " + userId);
                throw new AuthenticationException("Current password is incorrect");
            }
            
            // Check if new password is same as old
            if (PasswordHasher.verifyPassword(newPassword, user.getPasswordHash())) {
                throw new AuthenticationException("New password must be different from current password");
            }
            
            // Hash new password
            String newPasswordHash = PasswordHasher.hashPassword(newPassword);
            
            // Update password
            boolean success = userDAO.updatePassword(userId, newPasswordHash);
            
            if (success) {
                logger.info("Password changed successfully for user ID: " + userId);
            }
            
            return success;
            
        } catch (DatabaseException e) {
            logger.error("Database error during password change: " + e.getMessage());
            throw new AuthenticationException("Password change service unavailable. Please try again later.");
        }
    }
    
    /**
     * Reset user password (admin function)
     * @param userId The user ID
     * @param newPassword The new password
     * @return true if password reset successfully
     * @throws AuthenticationException if password reset fails
     */
    public boolean resetPassword(Long userId, String newPassword) throws AuthenticationException {
        try {
            logger.info("Password reset for user ID: " + userId);
            
            // Validate new password
            validatePassword(newPassword);
            
            // Hash new password
            String newPasswordHash = PasswordHasher.hashPassword(newPassword);
            
            // Update password
            boolean success = userDAO.updatePassword(userId, newPasswordHash);
            
            if (success) {
                logger.info("Password reset successfully for user ID: " + userId);
            }
            
            return success;
            
        } catch (DatabaseException e) {
            logger.error("Database error during password reset: " + e.getMessage());
            throw new AuthenticationException("Password reset service unavailable. Please try again later.");
        }
    }
    
    /**
     * Validate user has required role
     * @param user The user
     * @param requiredRole The required role
     * @return true if user has the role
     * @throws AuthenticationException if user doesn't have required role
     */
    public boolean authorize(User user, String requiredRole) throws AuthenticationException {
        if (user == null) {
            throw new AuthenticationException("User not authenticated");
        }
        
        if (!user.isActive()) {
            throw new AuthenticationException("User account is inactive");
        }
        
        if (!user.hasRole(requiredRole)) {
            logger.warn("Authorization failed - user " + user.getUsername() + 
                       " does not have role: " + requiredRole);
            throw new AuthenticationException("Insufficient permissions");
        }
        
        return true;
    }
    
    /**
     * Check if user is administrator
     * @param user The user
     * @return true if user is admin
     */
    public boolean isAdmin(User user) {
        return user != null && user.hasRole("ADMIN");
    }
    
    /**
     * Check if user is manager
     * @param user The user
     * @return true if user is manager
     */
    public boolean isManager(User user) {
        return user != null && user.hasRole("MANAGER");
    }
    
    /**
     * Activate a user account
     * @param userId The user ID
     * @return true if activated successfully
     * @throws AuthenticationException if activation fails
     */
    public boolean activateAccount(Long userId) throws AuthenticationException {
        try {
            logger.info("Activating user account: " + userId);
            return userDAO.activateUser(userId);
        } catch (DatabaseException e) {
            logger.error("Error activating account: " + e.getMessage());
            throw new AuthenticationException("Account activation failed");
        }
    }
    
    /**
     * Deactivate a user account
     * @param userId The user ID
     * @return true if deactivated successfully
     * @throws AuthenticationException if deactivation fails
     */
    public boolean deactivateAccount(Long userId) throws AuthenticationException {
        try {
            logger.info("Deactivating user account: " + userId);
            return userDAO.deactivateUser(userId);
        } catch (DatabaseException e) {
            logger.error("Error deactivating account: " + e.getMessage());
            throw new AuthenticationException("Account deactivation failed");
        }
    }
    
    // Validation methods
    
    private void validateUserData(User user, String password) throws AuthenticationException {
        // Validate username
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new AuthenticationException("Username is required");
        }
        
        String username = user.getUsername().trim();
        if (username.length() < MIN_USERNAME_LENGTH) {
            throw new AuthenticationException("Username must be at least " + 
                                            MIN_USERNAME_LENGTH + " characters");
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new AuthenticationException("Username must not exceed " + 
                                            MAX_USERNAME_LENGTH + " characters");
        }
        
        // Validate email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new AuthenticationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new AuthenticationException("Invalid email format");
        }
        
        // Validate first name
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new AuthenticationException("First name is required");
        }
        
        // Validate last name
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new AuthenticationException("Last name is required");
        }
        
        // Validate password
        validatePassword(password);
    }
    
    private void validatePassword(String password) throws AuthenticationException {
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password is required");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new AuthenticationException("Password must be at least " + 
                                            MIN_PASSWORD_LENGTH + " characters");
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new AuthenticationException("Password must not exceed " + 
                                            MAX_PASSWORD_LENGTH + " characters");
        }
        
        // Check for complexity requirements
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
        
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new AuthenticationException(
                "Password must contain at least one uppercase letter, " +
                "one lowercase letter, and one digit");
        }
    }
    
    /**
     * Validate session token (placeholder for JWT/token validation)
     * @param token The session token
     * @return The user associated with the token
     * @throws AuthenticationException if token is invalid
     */
    public User validateToken(String token) throws AuthenticationException {
        // This is a placeholder for token validation
        // In a real system, you would validate JWT or session tokens here
        throw new AuthenticationException("Token validation not implemented");
    }
}