package com.vaultsys.dao.impl;

import com.vaultsys.dao.interfaces.IAccountDAO;
import com.vaultsys.models.*;
import com.vaultsys.exceptions.DatabaseException;
import com.vaultsys.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of IAccountDAO for PostgreSQL database operations.
 */
public class AccountDAOImpl implements IAccountDAO {
    
    private static final String INSERT_ACCOUNT = 
        "INSERT INTO accounts (user_id, account_number, balance, currency, status, " +
        "account_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING account_id";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM accounts WHERE account_id = ?";
    
    private static final String SELECT_BY_ACCOUNT_NUMBER = 
        "SELECT * FROM accounts WHERE account_number = ?";
    
    private static final String SELECT_BY_USER_ID = 
        "SELECT * FROM accounts WHERE user_id = ? ORDER BY created_at DESC";
    
    private static final String UPDATE_ACCOUNT = 
        "UPDATE accounts SET balance = ?, status = ?, updated_at = ? WHERE account_id = ?";
    
    private static final String DELETE_ACCOUNT = 
        "DELETE FROM accounts WHERE account_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM accounts ORDER BY created_at DESC";
    
    private static final String SELECT_BY_STATUS = 
        "SELECT * FROM accounts WHERE status = ? ORDER BY created_at DESC";
    
    private static final String SELECT_BY_TYPE = 
        "SELECT * FROM accounts WHERE account_type = ? ORDER BY created_at DESC";
    
    private static final String UPDATE_BALANCE = 
        "UPDATE accounts SET balance = ?, updated_at = ? WHERE account_id = ?";
    
    private static final String UPDATE_STATUS = 
        "UPDATE accounts SET status = ?, updated_at = ? WHERE account_id = ?";
    
    private static final String CHECK_ACCOUNT_NUMBER_EXISTS = 
        "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
    
    private static final String SELECT_TOTAL_BALANCE_BY_USER = 
        "SELECT SUM(balance) FROM accounts WHERE user_id = ? AND status = 'ACTIVE'";
    
    private static final String SELECT_BALANCE_GREATER_THAN = 
        "SELECT * FROM accounts WHERE balance > ? ORDER BY balance DESC";
    
    private static final String SELECT_BALANCE_LESS_THAN = 
        "SELECT * FROM accounts WHERE balance < ? ORDER BY balance ASC";
    
    private static final String SELECT_ACTIVE_BY_USER = 
        "SELECT * FROM accounts WHERE user_id = ? AND status = 'ACTIVE' ORDER BY created_at DESC";
    
    private static final String COUNT_BY_TYPE = 
        "SELECT COUNT(*) FROM accounts WHERE account_type = ?";
    
    private static final String COUNT_TOTAL = 
        "SELECT COUNT(*) FROM accounts";
    
    private static final String SELECT_TOTAL_SYSTEM_BALANCE = 
        "SELECT SUM(balance) FROM accounts WHERE status = 'ACTIVE'";
    
    private static final String SELECT_DORMANT_ACCOUNTS = 
        "SELECT a.* FROM accounts a " +
        "LEFT JOIN transactions t ON a.account_id = t.account_id " +
        "WHERE a.status = 'ACTIVE' " +
        "GROUP BY a.account_id " +
        "HAVING MAX(t.transaction_date) < NOW() - INTERVAL '? days' OR MAX(t.transaction_date) IS NULL";

    @Override
    public Account create(Account account) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_ACCOUNT)) {
            
            pstmt.setLong(1, account.getUserId());
            pstmt.setString(2, account.getAccountNumber());
            pstmt.setBigDecimal(3, account.getBalance());
            pstmt.setString(4, account.getCurrency());
            pstmt.setString(5, account.getStatus().name());
            pstmt.setString(6, account.getAccountType());
            pstmt.setTimestamp(7, Timestamp.valueOf(account.getCreatedAt()));
            pstmt.setTimestamp(8, Timestamp.valueOf(account.getUpdatedAt()));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                account.setAccountId(rs.getLong("account_id"));
            }
            
            return account;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating account: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findById(Long accountId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToAccount(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding account by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ACCOUNT_NUMBER)) {
            
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToAccount(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding account by number: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findByUserId(Long userId) throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_USER_ID)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding accounts by user ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Account update(Account account) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_ACCOUNT)) {
            
            pstmt.setBigDecimal(1, account.getBalance());
            pstmt.setString(2, account.getStatus().name());
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(4, account.getAccountId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Account not found for update: " + account.getAccountId());
            }
            
            return account;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating account: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Long accountId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_ACCOUNT)) {
            
            pstmt.setLong(1, accountId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting account: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findAll() throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findByStatus(AccountStatus status) throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_STATUS)) {
            
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding accounts by status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findByType(String accountType) throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_TYPE)) {
            
            pstmt.setString(1, accountType);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding accounts by type: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateBalance(Long accountId, BigDecimal newBalance) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_BALANCE)) {
            
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(3, accountId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating balance: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(Long accountId, AccountStatus status) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS)) {
            
            pstmt.setString(1, status.name());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(3, accountId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean accountNumberExists(String accountNumber) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_ACCOUNT_NUMBER_EXISTS)) {
            
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error checking account number existence: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getTotalBalanceByUserId(Long userId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TOTAL_BALANCE_BY_USER)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total balance: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findAccountsWithBalanceGreaterThan(BigDecimal amount) throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BALANCE_GREATER_THAN)) {
            
            pstmt.setBigDecimal(1, amount);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding accounts with balance > amount: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findAccountsWithBalanceLessThan(BigDecimal amount) throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BALANCE_LESS_THAN)) {
            
            pstmt.setBigDecimal(1, amount);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding accounts with balance < amount: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findActiveAccountsByUserId(Long userId) throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ACTIVE_BY_USER)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding active accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public long getAccountCountByType(String accountType) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_BY_TYPE)) {
            
            pstmt.setString(1, accountType);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting account count by type: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalAccountCount() throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_TOTAL);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total account count: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getTotalSystemBalance() throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TOTAL_SYSTEM_BALANCE);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total system balance: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean closeAccount(Long accountId) throws DatabaseException {
        return updateStatus(accountId, AccountStatus.CLOSED);
    }

    @Override
    public boolean suspendAccount(Long accountId) throws DatabaseException {
        return updateStatus(accountId, AccountStatus.SUSPENDED);
    }

    @Override
    public boolean activateAccount(Long accountId) throws DatabaseException {
        return updateStatus(accountId, AccountStatus.ACTIVE);
    }

    @Override
    public List<Account> findDormantAccounts(int days) throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        String query = "SELECT a.* FROM accounts a " +
                      "LEFT JOIN transactions t ON a.account_id = t.account_id " +
                      "WHERE a.status = 'ACTIVE' " +
                      "GROUP BY a.account_id " +
                      "HAVING MAX(t.transaction_date) < NOW() - INTERVAL '" + days + " days' " +
                      "OR MAX(t.transaction_date) IS NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding dormant accounts: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to map ResultSet to Account object
     * Creates appropriate subclass based on account_type
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String accountType = rs.getString("account_type");
        Account account;
        
        // Create appropriate subclass
        if ("SAVINGS".equals(accountType)) {
            account = new SavingsAccount();
        } else if ("CHECKING".equals(accountType)) {
            account = new CheckingAccount();
        } else {
            // Default to base Account for unknown types (would need concrete implementation)
            account = new SavingsAccount(); // Fallback
        }
        
        // Set common fields
        account.setAccountId(rs.getLong("account_id"));
        account.setUserId(rs.getLong("user_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setCurrency(rs.getString("currency"));
        account.setStatus(AccountStatus.valueOf(rs.getString("status")));
        account.setAccountType(accountType);
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            account.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            account.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return account;
    }
}