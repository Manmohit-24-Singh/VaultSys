-- VaultSys Index Creation Script
-- PostgreSQL 12+
-- Indexes improve query performance for frequently accessed data

-- ============================================
-- USERS TABLE INDEXES
-- ============================================

-- Username lookup (for login)
CREATE INDEX idx_users_username ON users(username);

-- Email lookup (for password reset, uniqueness checks)
CREATE INDEX idx_users_email ON users(email);

-- Active users filter
CREATE INDEX idx_users_is_active ON users(is_active);

-- Role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Composite index for active users by role
CREATE INDEX idx_users_active_role ON users(is_active, role);

-- Last login tracking
CREATE INDEX idx_users_last_login ON users(last_login_at DESC);


-- ============================================
-- ACCOUNTS TABLE INDEXES
-- ============================================

-- User's accounts lookup (most common query)
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- Account number lookup (for transactions)
CREATE INDEX idx_accounts_account_number ON accounts(account_number);

-- Account status filtering
CREATE INDEX idx_accounts_status ON accounts(status);

-- Account type filtering
CREATE INDEX idx_accounts_type ON accounts(account_type);

-- Composite index for user's active accounts
CREATE INDEX idx_accounts_user_active ON accounts(user_id, status) 
    WHERE status = 'ACTIVE';

-- Balance queries (for reports, overdraft detection)
CREATE INDEX idx_accounts_balance ON accounts(balance);

-- Dormant account detection
CREATE INDEX idx_accounts_updated_at ON accounts(updated_at);

-- Composite index for account search by user and type
CREATE INDEX idx_accounts_user_type ON accounts(user_id, account_type);


-- ============================================
-- TRANSACTIONS TABLE INDEXES
-- ============================================

-- Account transaction history (most frequent query)
CREATE INDEX idx_transactions_account_id ON transactions(account_id);

-- Transaction date for range queries
CREATE INDEX idx_transactions_date ON transactions(transaction_date DESC);

-- Composite index for account transactions by date
CREATE INDEX idx_transactions_account_date ON transactions(account_id, transaction_date DESC);

-- Reference number lookup
CREATE INDEX idx_transactions_reference ON transactions(reference_number);

-- Transaction status filtering
CREATE INDEX idx_transactions_status ON transactions(status);

-- Transaction type filtering
CREATE INDEX idx_transactions_type ON transactions(transaction_type);

-- Transfer destination lookup
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id) 
    WHERE to_account_id IS NOT NULL;

-- Composite index for transfers between accounts
CREATE INDEX idx_transactions_transfer ON transactions(account_id, to_account_id, transaction_type)
    WHERE transaction_type IN ('TRANSFER', 'TRANSFER_RECEIVED');

-- Initiated by (user action tracking)
CREATE INDEX idx_transactions_initiated_by ON transactions(initiated_by);

-- Pending transactions (for batch processing)
CREATE INDEX idx_transactions_pending ON transactions(status, transaction_date)
    WHERE status = 'PENDING';

-- Large transaction detection
CREATE INDEX idx_transactions_amount ON transactions(amount DESC);

-- Composite index for account transactions by type and date
CREATE INDEX idx_transactions_account_type_date ON transactions(
    account_id, 
    transaction_type, 
    transaction_date DESC
);

-- Failed transactions for analysis
CREATE INDEX idx_transactions_failed ON transactions(status, transaction_date)
    WHERE status = 'FAILED';


-- ============================================
-- USER_SESSIONS TABLE INDEXES
-- ============================================

-- Session token lookup (for authentication)
CREATE INDEX idx_sessions_token ON user_sessions(session_token);

-- User's active sessions
CREATE INDEX idx_sessions_user_active ON user_sessions(user_id, is_active)
    WHERE is_active = TRUE;

-- Session expiry cleanup
CREATE INDEX idx_sessions_expires ON user_sessions(expires_at);

-- IP address tracking (security)
CREATE INDEX idx_sessions_ip ON user_sessions(ip_address);


-- ============================================
-- ACCOUNT_HOLDERS TABLE INDEXES
-- ============================================

-- Find accounts for a user
CREATE INDEX idx_holders_user_id ON account_holders(user_id);

-- Find holders for an account
CREATE INDEX idx_holders_account_id ON account_holders(account_id);


-- ============================================
-- AUDIT LOG INDEXES
-- ============================================

-- Audit by table and record
CREATE INDEX idx_audit_table_record ON audit.audit_log(table_name, record_id);

-- Audit by user
CREATE INDEX idx_audit_changed_by ON audit.audit_log(changed_by);

-- Audit by timestamp
CREATE INDEX idx_audit_changed_at ON audit.audit_log(changed_at DESC);

-- Audit by operation type
CREATE INDEX idx_audit_operation ON audit.audit_log(operation);

-- Composite index for table audit trail
CREATE INDEX idx_audit_table_date ON audit.audit_log(table_name, changed_at DESC);


-- ============================================
-- FAILED LOGIN ATTEMPTS INDEXES
-- ============================================

-- Username tracking
CREATE INDEX idx_failed_login_username ON audit.failed_login_attempts(username);

-- IP address tracking (for blocking)
CREATE INDEX idx_failed_login_ip ON audit.failed_login_attempts(ip_address);

-- Time-based analysis
CREATE INDEX idx_failed_login_time ON audit.failed_login_attempts(attempted_at DESC);

-- Composite index for username/IP analysis
CREATE INDEX idx_failed_login_user_ip ON audit.failed_login_attempts(
    username, 
    ip_address, 
    attempted_at DESC
);


-- ============================================
-- SCHEDULED TRANSACTIONS INDEXES
-- ============================================

-- Account's scheduled transactions
CREATE INDEX idx_scheduled_account ON scheduled_transactions(account_id);

-- Active scheduled transactions
CREATE INDEX idx_scheduled_active ON scheduled_transactions(is_active)
    WHERE is_active = TRUE;

-- Next execution (for batch processing)
CREATE INDEX idx_scheduled_next_exec ON scheduled_transactions(next_execution_at)
    WHERE is_active = TRUE;


-- ============================================
-- TRANSACTION LIMITS INDEXES
-- ============================================

-- Account limits lookup
CREATE INDEX idx_limits_account ON transaction_limits(account_id);

-- Limit reset date
CREATE INDEX idx_limits_reset_date ON transaction_limits(last_reset_date);


-- ============================================
-- BENEFICIARIES TABLE INDEXES
-- ============================================

-- User's beneficiaries
CREATE INDEX idx_beneficiaries_user ON beneficiaries(user_id);

-- Active beneficiaries
CREATE INDEX idx_beneficiaries_active ON beneficiaries(user_id, is_active)
    WHERE is_active = TRUE;

-- Beneficiary account lookup
CREATE INDEX idx_beneficiaries_account ON beneficiaries(beneficiary_account_number);


-- ============================================
-- SYSTEM_CONFIG TABLE INDEXES
-- ============================================

-- Config key is already the primary key, no additional index needed


-- ============================================
-- PERFORMANCE NOTES
-- ============================================
-- 1. Indexes speed up SELECT queries but slow down INSERT/UPDATE/DELETE
-- 2. Partial indexes (with WHERE clause) save space and improve performance
-- 3. DESC indexes optimize ORDER BY DESC queries
-- 4. Composite indexes should match common query patterns
-- 5. Monitor index usage with pg_stat_user_indexes view
-- 6. Rebuild indexes periodically: REINDEX INDEX idx_name;