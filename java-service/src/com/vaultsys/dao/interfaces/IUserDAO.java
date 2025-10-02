package com.vaultsys.dao.interfaces;

import com.vaultsys.models.User;
import com.vaultsys.exceptions.DatabaseException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity operations.
 * Defines the contract for all user-related database operations.
 */
public interface IUserDAO {
    
    /**
     * Create a new user in the database
     * @param user The user object to create
     * @return The created user with generated ID
     * @throws DatabaseException if database operation fails
     */
    User create(User user) throws DatabaseException;
    
    /**
     * Find a user by their unique ID
     * @param userId The user ID to search for
     * @return Optional containing the user if found, empty otherwise
     * @throws DatabaseException if database operation fails
     */
    Optional<User> findById(Long userId) throws DatabaseException;
    
    /**
     * Find a user by their username
     * @param username The username to search for
     * @return Optional containing the user if found, empty otherwise
     * @throws DatabaseException if database operation fails
     */
    Optional<User> findByUsername(String username) throws DatabaseException;
    
    /**
     * Find a user by their email address
     * @param email The email to search for
     * @return Optional containing the user if found, empty otherwise
     * @throws DatabaseException if database operation fails
     */
    Optional<User> findByEmail(String email) throws DatabaseException;
    
    /**
     * Update an existing user's information
     * @param user The user object with updated information
     * @return The updated user
     * @throws DatabaseException if database operation fails
     */
    User update(User user) throws DatabaseException;
    
    /**
     * Delete a user by their ID
     * @param userId The ID of the user to delete
     * @return true if deletion was successful, false otherwise
     * @throws DatabaseException if database operation fails
     */
    boolean delete(Long userId) throws DatabaseException;
    
    /**
     * Retrieve all users from the database
     * @return List of all users
     * @throws DatabaseException if database operation fails
     */
    List<User> findAll() throws DatabaseException;
    
    /**
     * Find all users with a specific role
     * @param role The role to filter by (e.g., "CUSTOMER", "ADMIN")
     * @return List of users with the specified role
     * @throws DatabaseException if database operation fails
     */
    List<User> findByRole(String role) throws DatabaseException;
    
    /**
     * Find all active users
     * @return List of active users
     * @throws DatabaseException if database operation fails
     */
    List<User> findActiveUsers() throws DatabaseException;
    
    /**
     * Update the last login timestamp for a user
     * @param userId The ID of the user
     * @return true if update was successful
     * @throws DatabaseException if database operation fails
     */
    boolean updateLastLogin(Long userId) throws DatabaseException;
    
    /**
     * Check if a username already exists
     * @param username The username to check
     * @return true if username exists, false otherwise
     * @throws DatabaseException if database operation fails
     */
    boolean usernameExists(String username) throws DatabaseException;
    
    /**
     * Check if an email already exists
     * @param email The email to check
     * @return true if email exists, false otherwise
     * @throws DatabaseException if database operation fails
     */
    boolean emailExists(String email) throws DatabaseException;
    
    /**
     * Activate a user account
     * @param userId The ID of the user to activate
     * @return true if activation was successful
     * @throws DatabaseException if database operation fails
     */
    boolean activateUser(Long userId) throws DatabaseException;
    
    /**
     * Deactivate a user account
     * @param userId The ID of the user to deactivate
     * @return true if deactivation was successful
     * @throws DatabaseException if database operation fails
     */
    boolean deactivateUser(Long userId) throws DatabaseException;
    
    /**
     * Update user's password hash
     * @param userId The ID of the user
     * @param passwordHash The new password hash
     * @return true if update was successful
     * @throws DatabaseException if database operation fails
     */
    boolean updatePassword(Long userId, String passwordHash) throws DatabaseException;
    
    /**
     * Search users by name (first name or last name)
     * @param searchTerm The search term
     * @return List of users matching the search
     * @throws DatabaseException if database operation fails
     */
    List<User> searchByName(String searchTerm) throws DatabaseException;
    
    /**
     * Get total count of users
     * @return The total number of users
     * @throws DatabaseException if database operation fails
     */
    long getTotalUserCount() throws DatabaseException;
    
    /**
     * Get count of active users
     * @return The number of active users
     * @throws DatabaseException if database operation fails
     */
    long getActiveUserCount() throws DatabaseException;
}