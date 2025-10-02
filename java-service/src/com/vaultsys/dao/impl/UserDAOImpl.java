package com.vaultsys.dao.impl;

import com.vaultsys.dao.interfaces.IUserDAO;
import com.vaultsys.models.User;
import com.vaultsys.exceptions.DatabaseException;
import com.vaultsys.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of IUserDAO for PostgreSQL database operations.
 */
public class UserDAOImpl implements IUserDAO {
    
    private static final String INSERT_USER = 
        "INSERT INTO users (username, password_hash, email, first_name, last_name, " +
        "phone_number, role, is_active, created_at, last_login_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING user_id";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM users WHERE user_id = ?";
    
    private static final String SELECT_BY_USERNAME = 
        "SELECT * FROM users WHERE username = ?";
    
    private static final String SELECT_BY_EMAIL = 
        "SELECT * FROM users WHERE email = ?";
    
    private static final String UPDATE_USER = 
        "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, " +
        "phone_number = ?, role = ?, is_active = ? WHERE user_id = ?";
    
    private static final String DELETE_USER = 
        "DELETE FROM users WHERE user_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM users ORDER BY created_at DESC";
    
    private static final String SELECT_BY_ROLE = 
        "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC";
    
    private static final String SELECT_ACTIVE = 
        "SELECT * FROM users WHERE is_active = true ORDER BY created_at DESC";
    
    private static final String UPDATE_LAST_LOGIN = 
        "UPDATE users SET last_login_at = ? WHERE user_id = ?";
    
    private static final String CHECK_USERNAME_EXISTS = 
        "SELECT COUNT(*) FROM users WHERE username = ?";
    
    private static final String CHECK_EMAIL_EXISTS = 
        "SELECT COUNT(*) FROM users WHERE email = ?";
    
    private static final String UPDATE_ACTIVE_STATUS = 
        "UPDATE users SET is_active = ? WHERE user_id = ?";
    
    private static final String UPDATE_PASSWORD = 
        "UPDATE users SET password_hash = ? WHERE user_id = ?";
    
    private static final String SEARCH_BY_NAME = 
        "SELECT * FROM users WHERE LOWER(first_name) LIKE LOWER(?) " +
        "OR LOWER(last_name) LIKE LOWER(?) ORDER BY last_name, first_name";
    
    private static final String COUNT_TOTAL = 
        "SELECT COUNT(*) FROM users";
    
    private static final String COUNT_ACTIVE = 
        "SELECT COUNT(*) FROM users WHERE is_active = true";

    @Override
    public User create(User user) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_USER)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFirstName());
            pstmt.setString(5, user.getLastName());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setString(7, user.getRole());
            pstmt.setBoolean(8, user.isActive());
            pstmt.setTimestamp(9, Timestamp.valueOf(user.getCreatedAt()));
            pstmt.setTimestamp(10, user.getLastLoginAt() != null ? 
                Timestamp.valueOf(user.getLastLoginAt()) : null);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                user.setUserId(rs.getLong("user_id"));
            }
            
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long userId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_USERNAME)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_EMAIL)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email: " + e.getMessage(), e);
        }
    }

    @Override
    public User update(User user) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_USER)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getPhoneNumber());
            pstmt.setString(6, user.getRole());
            pstmt.setBoolean(7, user.isActive());
            pstmt.setLong(8, user.getUserId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("User not found for update: " + user.getUserId());
            }
            
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Long userId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_USER)) {
            
            pstmt.setLong(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all users: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findByRole(String role) throws DatabaseException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ROLE)) {
            
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding users by role: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findActiveUsers() throws DatabaseException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ACTIVE);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding active users: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateLastLogin(Long userId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_LAST_LOGIN)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating last login: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean usernameExists(String username) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_USERNAME_EXISTS)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error checking username existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean emailExists(String email) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_EMAIL_EXISTS)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error checking email existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean activateUser(Long userId) throws DatabaseException {
        return updateActiveStatus(userId, true);
    }

    @Override
    public boolean deactivateUser(Long userId) throws DatabaseException {
        return updateActiveStatus(userId, false);
    }

    private boolean updateActiveStatus(Long userId, boolean isActive) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_ACTIVE_STATUS)) {
            
            pstmt.setBoolean(1, isActive);
            pstmt.setLong(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updatePassword(Long userId, String passwordHash) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PASSWORD)) {
            
            pstmt.setString(1, passwordHash);
            pstmt.setLong(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating password: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> searchByName(String searchTerm) throws DatabaseException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_NAME)) {
            
            String wildcardTerm = "%" + searchTerm + "%";
            pstmt.setString(1, wildcardTerm);
            pstmt.setString(2, wildcardTerm);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Error searching users by name: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalUserCount() throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_TOTAL);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total user count: " + e.getMessage(), e);
        }
    }

    @Override
    public long getActiveUserCount() throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_ACTIVE);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting active user count: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
        if (lastLoginAt != null) {
            user.setLastLoginAt(lastLoginAt.toLocalDateTime());
        }
        
        return user;
    }
}