package com.vaultsys.dao.impl;

import com.vaultsys.dao.interfaces.ITransactionDAO;
import com.vaultsys.models.Transaction;
import com.vaultsys.models.TransactionType;
import com.vaultsys.exceptions.DatabaseException;
import com.vaultsys.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ITransactionDAO for PostgreSQL database operations.
 */
public class TransactionDAOImpl implements ITransactionDAO {
    
    private static final String INSERT_TRANSACTION = 
        "INSERT INTO transactions (account_id, to_account_id, transaction_type, amount, " +
        "balance_before, balance_after, currency, transaction_date, description, " +
        "reference_number, status, initiated_by, processed_at, metadata) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING transaction_id";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM transactions WHERE transaction_id = ?";
    
    private static final String SELECT_BY_REFERENCE = 
        "SELECT * FROM transactions WHERE reference_number = ?";
    
    private static final String SELECT_BY_ACCOUNT_ID = 
        "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
    
    private static final String SELECT_BY_ACCOUNT_AND_DATE_RANGE = 
        "SELECT * FROM transactions WHERE account_id = ? AND transaction_date BETWEEN ? AND ? " +
        "ORDER BY transaction_date DESC";
    
    private static final String UPDATE_TRANSACTION = 
        "UPDATE transactions SET status = ?, balance_before = ?, balance_after = ?, " +
        "processed_at = ?, failure_reason = ? WHERE transaction_id = ?";
    
    private static final String DELETE_TRANSACTION = 
        "DELETE FROM transactions WHERE transaction_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM transactions ORDER BY transaction_date DESC";
    
    private static final String SELECT_BY_TYPE = 
        "SELECT * FROM transactions WHERE transaction_type = ? ORDER BY transaction_date DESC";
    
    private static final String SELECT_BY_STATUS = 
        "SELECT * FROM transactions WHERE status = ? ORDER BY transaction_date DESC";
    
    private static final String SELECT_PENDING = 
        "SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY transaction_date DESC";
    
    private static final String SELECT_FAILED = 
        "SELECT * FROM transactions WHERE status = 'FAILED' ORDER BY transaction_date DESC";
    
    private static final String SELECT_BY_INITIATOR = 
        "SELECT * FROM transactions WHERE initiated_by = ? ORDER BY transaction_date DESC";
    
    private static final String SELECT_RECENT = 
        "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC LIMIT ?";
    
    private static final String SELECT_BY_DATE_RANGE = 
        "SELECT * FROM transactions WHERE transaction_date BETWEEN ? AND ? " +
        "ORDER BY transaction_date DESC";
    
    private static final String SELECT_AMOUNT_GREATER_THAN = 
        "SELECT * FROM transactions WHERE amount > ? ORDER BY amount DESC";
    
    private static final String SELECT_AMOUNT_LESS_THAN = 
        "SELECT * FROM transactions WHERE amount < ? ORDER BY amount ASC";
    
    private static final String SELECT_TRANSFERS_BETWEEN = 
        "SELECT * FROM transactions WHERE account_id = ? AND to_account_id = ? " +
        "AND transaction_type = 'TRANSFER' ORDER BY transaction_date DESC";
    
    private static final String SUM_BY_ACCOUNT_AND_TYPE = 
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
        "WHERE account_id = ? AND transaction_type = ? AND status = 'COMPLETED'";
    
    private static final String SUM_DEPOSITS = 
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
        "WHERE account_id = ? AND transaction_type = 'DEPOSIT' AND status = 'COMPLETED'";
    
    private static final String SUM_WITHDRAWALS = 
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
        "WHERE account_id = ? AND transaction_type = 'WITHDRAWAL' AND status = 'COMPLETED'";
    
    private static final String COUNT_BY_ACCOUNT = 
        "SELECT COUNT(*) FROM transactions WHERE account_id = ?";
    
    private static final String COUNT_BY_ACCOUNT_AND_TYPE = 
        "SELECT COUNT(*) FROM transactions WHERE account_id = ? AND transaction_type = ?";
    
    private static final String UPDATE_STATUS_COMPLETED = 
        "UPDATE transactions SET status = 'COMPLETED', balance_before = ?, balance_after = ?, " +
        "processed_at = ? WHERE transaction_id = ?";
    
    private static final String UPDATE_STATUS_FAILED = 
        "UPDATE transactions SET status = 'FAILED', failure_reason = ?, processed_at = ? " +
        "WHERE transaction_id = ?";
    
    private static final String UPDATE_STATUS_REVERSED = 
        "UPDATE transactions SET status = 'REVERSED', processed_at = ? WHERE transaction_id = ?";
    
    private static final String DAILY_VOLUME = 
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
        "WHERE account_id = ? AND DATE(transaction_date) = DATE(?) AND status = 'COMPLETED'";
    
    private static final String MONTHLY_VOLUME = 
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
        "WHERE account_id = ? AND EXTRACT(YEAR FROM transaction_date) = ? " +
        "AND EXTRACT(MONTH FROM transaction_date) = ? AND status = 'COMPLETED'";
    
    private static final String SELECT_SUSPICIOUS = 
        "SELECT * FROM transactions WHERE amount > ? AND status = 'COMPLETED' " +
        "ORDER BY amount DESC, transaction_date DESC";
    
    private static final String COUNT_TOTAL = 
        "SELECT COUNT(*) FROM transactions";
    
    private static final String SUM_TOTAL_VOLUME = 
        "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'COMPLETED'";

    @Override
    public Transaction create(Transaction transaction) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TRANSACTION)) {
            
            pstmt.setLong(1, transaction.getAccountId());
            if (transaction.getToAccountId() != null) {
                pstmt.setLong(2, transaction.getToAccountId());
            } else {
                pstmt.setNull(2, Types.BIGINT);
            }
            pstmt.setString(3, transaction.getType().name());
            pstmt.setBigDecimal(4, transaction.getAmount());
            
            if (transaction.getBalanceBefore() != null) {
                pstmt.setBigDecimal(5, transaction.getBalanceBefore());
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }
            
            if (transaction.getBalanceAfter() != null) {
                pstmt.setBigDecimal(6, transaction.getBalanceAfter());
            } else {
                pstmt.setNull(6, Types.DECIMAL);
            }
            
            pstmt.setString(7, transaction.getCurrency());
            pstmt.setTimestamp(8, Timestamp.valueOf(transaction.getTransactionDate()));
            pstmt.setString(9, transaction.getDescription());
            pstmt.setString(10, transaction.getReferenceNumber());
            pstmt.setString(11, transaction.getStatus());
            pstmt.setString(12, transaction.getInitiatedBy());
            
            if (transaction.getProcessedAt() != null) {
                pstmt.setTimestamp(13, Timestamp.valueOf(transaction.getProcessedAt()));
            } else {
                pstmt.setNull(13, Types.TIMESTAMP);
            }
            
            pstmt.setString(14, transaction.getMetadata());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                transaction.setTransactionId(rs.getLong("transaction_id"));
            }
            
            return transaction;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long transactionId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setLong(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToTransaction(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding transaction by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findByReferenceNumber(String referenceNumber) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_REFERENCE)) {
            
            pstmt.setString(1, referenceNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToTransaction(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding transaction by reference: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId) throws DatabaseException {
        return executeQuery(SELECT_BY_ACCOUNT_ID, pstmt -> pstmt.setLong(1, accountId));
    }

    @Override
    public List<Transaction> findByAccountIdAndDateRange(Long accountId, LocalDateTime startDate, 
                                                          LocalDateTime endDate) throws DatabaseException {
        return executeQuery(SELECT_BY_ACCOUNT_AND_DATE_RANGE, pstmt -> {
            pstmt.setLong(1, accountId);
            pstmt.setTimestamp(2, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(3, Timestamp.valueOf(endDate));
        });
    }

    @Override
    public Transaction update(Transaction transaction) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_TRANSACTION)) {
            
            pstmt.setString(1, transaction.getStatus());
            
            if (transaction.getBalanceBefore() != null) {
                pstmt.setBigDecimal(2, transaction.getBalanceBefore());
            } else {
                pstmt.setNull(2, Types.DECIMAL);
            }
            
            if (transaction.getBalanceAfter() != null) {
                pstmt.setBigDecimal(3, transaction.getBalanceAfter());
            } else {
                pstmt.setNull(3, Types.DECIMAL);
            }
            
            if (transaction.getProcessedAt() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(transaction.getProcessedAt()));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }
            
            pstmt.setString(5, transaction.getFailureReason());
            pstmt.setLong(6, transaction.getTransactionId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Transaction not found for update: " + 
                                          transaction.getTransactionId());
            }
            
            return transaction;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Long transactionId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TRANSACTION)) {
            
            pstmt.setLong(1, transactionId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findAll() throws DatabaseException {
        return executeQuery(SELECT_ALL, pstmt -> {});
    }

    @Override
    public List<Transaction> findByType(TransactionType type) throws DatabaseException {
        return executeQuery(SELECT_BY_TYPE, pstmt -> pstmt.setString(1, type.name()));
    }

    @Override
    public List<Transaction> findByStatus(String status) throws DatabaseException {
        return executeQuery(SELECT_BY_STATUS, pstmt -> pstmt.setString(1, status));
    }

    @Override
    public List<Transaction> findPendingTransactions() throws DatabaseException {
        return executeQuery(SELECT_PENDING, pstmt -> {});
    }

    @Override
    public List<Transaction> findFailedTransactions() throws DatabaseException {
        return executeQuery(SELECT_FAILED, pstmt -> {});
    }

    @Override
    public List<Transaction> findByInitiator(String username) throws DatabaseException {
        return executeQuery(SELECT_BY_INITIATOR, pstmt -> pstmt.setString(1, username));
    }

    @Override
    public List<Transaction> getRecentTransactions(Long accountId, int limit) throws DatabaseException {
        return executeQuery(SELECT_RECENT, pstmt -> {
            pstmt.setLong(1, accountId);
            pstmt.setInt(2, limit);
        });
    }

    @Override
    public List<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) 
            throws DatabaseException {
        return executeQuery(SELECT_BY_DATE_RANGE, pstmt -> {
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
        });
    }

    @Override
    public List<Transaction> findByAmountGreaterThan(BigDecimal amount) throws DatabaseException {
        return executeQuery(SELECT_AMOUNT_GREATER_THAN, pstmt -> pstmt.setBigDecimal(1, amount));
    }

    @Override
    public List<Transaction> findByAmountLessThan(BigDecimal amount) throws DatabaseException {
        return executeQuery(SELECT_AMOUNT_LESS_THAN, pstmt -> pstmt.setBigDecimal(1, amount));
    }

    @Override
    public List<Transaction> findTransfersBetweenAccounts(Long fromAccountId, Long toAccountId) 
            throws DatabaseException {
        return executeQuery(SELECT_TRANSFERS_BETWEEN, pstmt -> {
            pstmt.setLong(1, fromAccountId);
            pstmt.setLong(2, toAccountId);
        });
    }

    @Override
    public BigDecimal getSumByAccountAndType(Long accountId, TransactionType type) 
            throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SUM_BY_ACCOUNT_AND_TYPE)) {
            
            pstmt.setLong(1, accountId);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting sum by account and type: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getTotalDeposits(Long accountId) throws DatabaseException {
        return executeSumQuery(SUM_DEPOSITS, accountId);
    }

    @Override
    public BigDecimal getTotalWithdrawals(Long accountId) throws DatabaseException {
        return executeSumQuery(SUM_WITHDRAWALS, accountId);
    }

    @Override
    public long getTransactionCount(Long accountId) throws DatabaseException {
        return executeCountQuery(COUNT_BY_ACCOUNT, pstmt -> pstmt.setLong(1, accountId));
    }

    @Override
    public long getTransactionCountByType(Long accountId, TransactionType type) 
            throws DatabaseException {
        return executeCountQuery(COUNT_BY_ACCOUNT_AND_TYPE, pstmt -> {
            pstmt.setLong(1, accountId);
            pstmt.setString(2, type.name());
        });
    }

    @Override
    public boolean markAsCompleted(Long transactionId, BigDecimal balanceBefore, 
                                   BigDecimal balanceAfter) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS_COMPLETED)) {
            
            pstmt.setBigDecimal(1, balanceBefore);
            pstmt.setBigDecimal(2, balanceAfter);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(4, transactionId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking transaction as completed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean markAsFailed(Long transactionId, String failureReason) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS_FAILED)) {
            
            pstmt.setString(1, failureReason);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(3, transactionId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking transaction as failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean reverseTransaction(Long transactionId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS_REVERSED)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, transactionId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error reversing transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getDailyTransactionVolume(Long accountId, LocalDateTime date) 
            throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DAILY_VOLUME)) {
            
            pstmt.setLong(1, accountId);
            pstmt.setTimestamp(2, Timestamp.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting daily volume: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getMonthlyTransactionVolume(Long accountId, int year, int month) 
            throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(MONTHLY_VOLUME)) {
            
            pstmt.setLong(1, accountId);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting monthly volume: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findSuspiciousTransactions(BigDecimal threshold) 
            throws DatabaseException {
        return executeQuery(SELECT_SUSPICIOUS, pstmt -> pstmt.setBigDecimal(1, threshold));
    }

    @Override
    public long getTotalTransactionCount() throws DatabaseException {
        return executeCountQuery(COUNT_TOTAL, pstmt -> {});
    }

    @Override
    public BigDecimal getTotalTransactionVolume() throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SUM_TOTAL_VOLUME);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total volume: " + e.getMessage(), e);
        }
    }

    // Helper methods
    
    @FunctionalInterface
    private interface PreparedStatementSetter {
        void set(PreparedStatement pstmt) throws SQLException;
    }

    private List<Transaction> executeQuery(String sql, PreparedStatementSetter setter) 
            throws DatabaseException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            setter.set(pstmt);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        } catch (SQLException e) {
            throw new DatabaseException("Error executing query: " + e.getMessage(), e);
        }
    }

    private BigDecimal executeSumQuery(String sql, Long accountId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DatabaseException("Error executing sum query: " + e.getMessage(), e);
        }
    }

    private long executeCountQuery(String sql, PreparedStatementSetter setter) 
            throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            setter.set(pstmt);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error executing count query: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to map ResultSet to Transaction object
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        
        transaction.setTransactionId(rs.getLong("transaction_id"));
        transaction.setAccountId(rs.getLong("account_id"));
        
        long toAccountId = rs.getLong("to_account_id");
        if (!rs.wasNull()) {
            transaction.setToAccountId(toAccountId);
        }
        
        transaction.setType(TransactionType.valueOf(rs.getString("transaction_type")));
        transaction.setAmount(rs.getBigDecimal("amount"));
        
        BigDecimal balanceBefore = rs.getBigDecimal("balance_before");
        if (!rs.wasNull()) {
            transaction.setBalanceBefore(balanceBefore);
        }
        
        BigDecimal balanceAfter = rs.getBigDecimal("balance_after");
        if (!rs.wasNull()) {
            transaction.setBalanceAfter(balanceAfter);
        }
        
        transaction.setCurrency(rs.getString("currency"));
        
        Timestamp transactionDate = rs.getTimestamp("transaction_date");
        if (transactionDate != null) {
            transaction.setTransactionDate(transactionDate.toLocalDateTime());
        }
        
        transaction.setDescription(rs.getString("description"));
        transaction.setReferenceNumber(rs.getString("reference_number"));
        transaction.setStatus(rs.getString("status"));
        transaction.setInitiatedBy(rs.getString("initiated_by"));
        transaction.setFailureReason(rs.getString("failure_reason"));
        
        Timestamp processedAt = rs.getTimestamp("processed_at");
        if (processedAt != null) {
            transaction.setProcessedAt(processedAt.toLocalDateTime());
        }
        
        transaction.setMetadata(rs.getString("metadata"));
        
        return transaction;
    }
}