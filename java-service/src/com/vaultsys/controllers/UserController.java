package com.vaultsys.controllers;

import com.vaultsys.models.User;
import com.vaultsys.services.AuthService;
import com.vaultsys.exceptions.AuthenticationException;
import com.vaultsys.utils.Logger;

import java.util.Scanner;

/**
 * Controller class for handling user authentication and management.
 * Manages user login, registration, and profile operations.
 * 
 * Key OOP Concepts:
 * - Separation of Concerns: UI logic separate from business logic
 * - Composition: Uses AuthService
 * - Encapsulation: Hides password handling details
 */
public class UserController {
    
    private final AuthService authService;
    private static final Logger logger = Logger.getInstance();
    private final Scanner scanner;
    private User currentUser; // Stores logged-in user
    
    /**
     * Constructor with dependency injection
     * @param authService Authentication service instance
     */
    public UserController(AuthService authService) {
        this.authService = authService;
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
    }
    
    /**
     * Display user authentication menu
     * @return true if user successfully authenticated
     */
    public boolean showAuthenticationMenu() {
        boolean running = true;
        
        while (running) {
            displayAuthMenuOptions();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        if (login()) {
                            return true; // Successfully logged in
                        }
                        break;
                    case 2:
                        register();
                        break;
                    case 3:
                        running = false;
                        return false; // Exit application
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                logger.error("Error in authentication menu: " + e.getMessage());
                System.out.println("An error occurred. Please try again.");
            }
        }
        
        return false;
    }
    
    /**
     * Display user management menu (for logged-in users)
     */
    public void showUserMenu() {
        if (currentUser == null) {
            System.out.println("✗ No user logged in.");
            return;
        }
        
        boolean running = true;
        
        while (running) {
            displayUserMenuOptions();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        viewProfile();
                        break;
                    case 2:
                        changePassword();
                        break;
                    case 3:
                        logout();
                        running = false;
                        break;
                    case 0:
                        running = false;
                        System.out.println("Returning to main menu...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                logger.error("Error in user menu: " + e.getMessage());
                System.out.println("An error occurred. Please try again.");
            }
        }
    }
    
    /**
     * Display authentication menu options
     */
    private void displayAuthMenuOptions() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║         VAULTSYS BANKING           ║");
        System.out.println("║      Authentication System         ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. Login");
        System.out.println("2. Register New Account");
        System.out.println("3. Exit");
        System.out.println("────────────────────────────────────");
    }
    
    /**
     * Display user management menu options
     */
    private void displayUserMenuOptions() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║       USER MANAGEMENT MENU         ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. View Profile");
        System.out.println("2. Change Password");
        System.out.println("3. Logout");
        System.out.println("0. Back to Main Menu");
        System.out.println("────────────────────────────────────");
    }
    
    /**
     * Handle user login
     * @return true if login successful
     */
    private boolean login() {
        try {
            System.out.println("\n=== User Login ===");
            
            String username = getStringInput("Username: ");
            String password = getPasswordInput("Password: ");
            
            User user = authService.login(username, password);
            this.currentUser = user;
            
            System.out.println("\n✓ Login Successful!");
            System.out.println("Welcome, " + user.getFirstName() + " " + user.getLastName() + "!");
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Role: " + user.getRole());
            
            logger.info("User logged in: " + username);
            return true;
            
        } catch (AuthenticationException e) {
            System.out.println("✗ Login Failed: " + e.getMessage());
            logger.warn("Login attempt failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle user registration
     */
    private void register() {
        try {
            System.out.println("\n=== User Registration ===");
            
            // Collect user information
            String username = getStringInput("Username (3-50 characters): ");
            String email = getStringInput("Email address: ");
            String firstName = getStringInput("First Name: ");
            String lastName = getStringInput("Last Name: ");
            String password = getPasswordInput("Password (min 8 characters): ");
            String confirmPassword = getPasswordInput("Confirm Password: ");
            
            // Validate password match
            if (!password.equals(confirmPassword)) {
                System.out.println("✗ Passwords do not match!");
                return;
            }
            
            // Create user object
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setRole("CUSTOMER"); // Default role
            
            // Register user
            User createdUser = authService.register(newUser, password);
            
            System.out.println("\n✓ Registration Successful!");
            System.out.println("User ID: " + createdUser.getUserId());
            System.out.println("Username: " + createdUser.getUsername());
            System.out.println("You can now login with your credentials.");
            
            logger.info("New user registered: " + username);
            
        } catch (AuthenticationException e) {
            System.out.println("✗ Registration Failed: " + e.getMessage());
            logger.error("Registration failed: " + e.getMessage());
        }
    }
    
    /**
     * View current user profile
     */
    private void viewProfile() {
        if (currentUser == null) {
            System.out.println("✗ No user logged in.");
            return;
        }
        
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║         USER PROFILE               ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("User ID: " + currentUser.getUserId());
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("Name: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        System.out.println("Role: " + currentUser.getRole());
        System.out.println("Status: " + (currentUser.isActive() ? "Active" : "Inactive"));
        System.out.println("Created: " + currentUser.getCreatedAt());
        if (currentUser.getLastLoginAt() != null) {
            System.out.println("Last Login: " + currentUser.getLastLoginAt());
        }
    }
    
    /**
     * Change user password
     */
    private void changePassword() {
        if (currentUser == null) {
            System.out.println("✗ No user logged in.");
            return;
        }
        
        try {
            System.out.println("\n=== Change Password ===");
            
            String oldPassword = getPasswordInput("Current Password: ");
            String newPassword = getPasswordInput("New Password: ");
            String confirmPassword = getPasswordInput("Confirm New Password: ");
            
            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("✗ New passwords do not match!");
                return;
            }
            
            // Change password
            boolean success = authService.changePassword(
                currentUser.getUserId(), oldPassword, newPassword);
            
            if (success) {
                System.out.println("✓ Password changed successfully!");
                logger.info("Password changed for user: " + currentUser.getUsername());
            } else {
                System.out.println("✗ Failed to change password.");
            }
            
        } catch (AuthenticationException e) {
            System.out.println("✗ Password change failed: " + e.getMessage());
        }
    }
    
    /**
     * Logout current user
     */
    private void logout() {
        if (currentUser != null) {
            String username = currentUser.getUsername();
            this.currentUser = null;
            System.out.println("✓ Logged out successfully.");
            logger.info("User logged out: " + username);
        } else {
            System.out.println("No user logged in.");
        }
    }
    
    // Getters
    
    /**
     * Get currently logged-in user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if a user is logged in
     * @return true if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current user's ID
     * @return User ID or null if not logged in
     */
    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }
    
    /**
     * Check if current user has admin privileges
     * @return true if user is admin
     */
    public boolean isCurrentUserAdmin() {
        return currentUser != null && authService.isAdmin(currentUser);
    }
    
    /**
     * Check if current user has manager privileges
     * @return true if user is manager
     */
    public boolean isCurrentUserManager() {
        return currentUser != null && authService.isManager(currentUser);
    }
    
    // Input helper methods
    
    private int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. " + prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }
    
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * Get password input (without showing characters)
     * Note: For production, use Console.readPassword() for better security
     */
    private String getPasswordInput(String prompt) {
        System.out.print(prompt);
        // In a real application, use System.console().readPassword()
        // For this demo, we use regular input
        return scanner.nextLine();
    }
}