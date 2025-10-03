-- VaultSys Stored Functions Script
-- PostgreSQL 12+
-- Stored functions for common business logic operations

-- ============================================
-- ACCOUNT FUNCTIONS
-- ============================================

-- Get total balance for a user across all accounts
CREATE OR REPLACE FUNCTION get_user_total_balance(p_user_id BIGINT)
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_total DECIMAL(15, 2);
BEGIN
    SELECT COALESCE(SUM(balance), 0)
    INTO v_total
    FROM accounts
    WHERE user_id = p_user_id
      AND status = 'ACTIVE';
    
    RETURN v_total;
END;
$$ LANGUAGE plpgsql;

-- Get available balance including overdraft
CREATE OR REPLACE FUNCTION get_available_balance(p_account_id BIGINT)
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_balance DECIMAL(15, 2);
    v_overdraft DECIMAL(15, 2);
BEGIN
    SELECT balance, overdraft_limit
    INTO v_balance, v_overdraft
    FROM accounts
    WHERE account_id = p_account_id;
    
    RETURN v_balance + v_overdraft;
END;
$$ LANGUAGE plpgsql;

-- Check if account can withdraw amount
CREATE OR REPLACE FUNCTION can_withdraw(
    p_account_id BIGINT,
    p_amount DECIMAL(15, 2)
)
RETURNS BOOLEAN AS $$
DECLARE
    v_available DECIMAL(15, 2);
BEGIN
    v_available := get_available_balance(p_account_id);
    RETURN v_available >= p_amount;
END;
$$ LANGUAGE plpgsql;

-- Get account count by type
CREATE OR REPLACE FUNCTION get_account_count_by_type(p_account_type account_type)
RETURNS BIGINT AS $$
DECLARE
    v_count BIGINT;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM accounts
    WHERE account_type = p_account_type
      AND status = 'ACTIVE';
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- TRANSACTION FUNCTIONS
-- ============================================

-- Get total deposits for an account
CREATE OR REPLACE FUNCTION get_total_deposits(p_account_id BIGINT)
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_total DECIMAL(15, 2);
BEGIN
    SELECT COALESCE(SUM(amount), 0)
    INTO v_total
    FROM transactions
    WHERE account_id = p_account_id
      AND transaction_type = 'DEPOSIT'
      AND status = 'COMPLETED';
    
    RETURN v_total;
END;
$$ LANGUAGE plpgsql;

-- Get total withdrawals for an account
CREATE OR REPLACE FUNCTION get_total_withdrawals(p_account_id BIGINT)
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_total DECIMAL(15, 2);
BEGIN
    SELECT COALESCE(SUM(amount), 0)
    INTO v_total
    FROM transactions
    WHERE account_id = p_account_id
      AND transaction_type IN ('WITHDRAWAL', 'TRANSFER', 'ATM_WITHDRAWAL')
      AND status = 'COMPLETED';
    
    RETURN v_total;
END;
$$ LANGUAGE plpgsql;

-- Get transaction count for an account
CREATE OR REPLACE FUNCTION get_transaction_count(p_account_id BIGINT)
RETURNS BIGINT AS $$
DECLARE
    v_count BIGINT;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM transactions
    WHERE account_id = p_account_id
      AND status = 'COMPLETED';
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- Get monthly transaction volume
CREATE OR REPLACE FUNCTION get_monthly_transaction_volume(
    p_account_id BIGINT,
    p_year INT,
    p_month INT
)
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_volume DECIMAL(15, 2);
BEGIN
    SELECT COALESCE(SUM(amount), 0)
    INTO v_volume
    FROM transactions
    WHERE account_id = p_account_id
      AND EXTRACT(YEAR FROM transaction_date) = p_year
      AND EXTRACT(MONTH FROM transaction_date) = p_month
      AND status = 'COMPLETED';
    
    RETURN v_volume;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- INTEREST CALCULATION FUNCTIONS
-- ============================================

-- Calculate simple interest for savings account
CREATE OR REPLACE FUNCTION calculate_simple_interest(
    p_account_id BIGINT,
    p_days INT DEFAULT 30
)
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_balance DECIMAL(15, 2);
    v_rate DECIMAL(5, 4);
    v_interest DECIMAL(15, 2);
BEGIN
    SELECT balance, interest_rate
    INTO v_balance, v_rate
    FROM accounts
    WHERE account_id = p_account_id;
    
    -- Interest = Principal × Rate × (Days / 365)
    v_interest := v_balance * v_rate * (p_days::DECIMAL / 365);
    
    RETURN ROUND(v_interest, 2);
END;
$$ LANGUAGE plpgsql;

-- Apply interest to savings accounts
CREATE OR REPLACE FUNCTION apply_monthly_interest()
RETURNS TABLE(account_id BIGINT, interest_amount DECIMAL(15, 2)) AS $$
BEGIN
    RETURN QUERY
    UPDATE accounts a
    SET balance = balance + calculate_simple_interest(a.account_id, 30),
        last_interest_calculation = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE account_type = 'SAVINGS'
      AND status = 'ACTIVE'
      AND interest_rate > 0
      AND (last_interest_calculation IS NULL 
           OR last_interest_calculation < CURRENT_DATE - INTERVAL '1 month')
    RETURNING a.account_id, calculate_simple_interest(a.account_id, 30);
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- USER FUNCTIONS
-- ============================================

-- Check if username exists
CREATE OR REPLACE FUNCTION username_exists(p_username VARCHAR(50))
RETURNS BOOLEAN AS $$
DECLARE
    v_exists BOOLEAN;
BEGIN
    SELECT EXISTS(
        SELECT 1 FROM users WHERE username = p_username
    ) INTO v_exists;
    
    RETURN v_exists;
END;
$$ LANGUAGE plpgsql;

-- Check if email exists
CREATE OR REPLACE FUNCTION email_exists(p_email VARCHAR(100))
RETURNS BOOLEAN AS $$
DECLARE
    v_exists BOOLEAN;
BEGIN
    SELECT EXISTS(
        SELECT 1 FROM users WHERE email = p_email
    ) INTO v_exists;
    
    RETURN v_exists;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- REPORTING FUNCTIONS
-- ============================================

-- Get system statistics
CREATE OR REPLACE FUNCTION get_system_stats()
RETURNS TABLE(
    total_users BIGINT,
    active_users BIGINT,
    total_accounts BIGINT,
    active_accounts BIGINT,
    total_balance DECIMAL(15, 2),
    total_transactions BIGINT,
    total_transaction_volume DECIMAL(15, 2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM users)::BIGINT,
        (SELECT COUNT(*) FROM users WHERE is_active = TRUE)::BIGINT,
        (SELECT COUNT(*) FROM accounts)::BIGINT,
        (SELECT COUNT(*) FROM accounts WHERE status = 'ACTIVE')::BIGINT,
        (SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE status = 'ACTIVE'),
        (SELECT COUNT(*) FROM transactions WHERE status = 'COMPLETED')::BIGINT,
        (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'COMPLETED');
END;
$$ LANGUAGE plpgsql;

-- Get account summary
CREATE OR REPLACE FUNCTION get_account_summary(p_account_id BIGINT)
RETURNS TABLE(
    account_number VARCHAR(20),
    account_type account_type,
    balance DECIMAL(15, 2),
    total_deposits DECIMAL(15, 2),
    total_withdrawals DECIMAL(15, 2),
    transaction_count BIGINT,
    last_transaction_date TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        a.account_number,
        a.account_type,
        a.balance,
        get_total_deposits(p_account_id),
        get_total_withdrawals(p_account_id),
        get_transaction_count(p_account_id),
        (SELECT MAX(transaction_date) FROM transactions WHERE account_id = p_account_id)
    FROM accounts a
    WHERE a.account_id = p_account_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- CLEANUP FUNCTIONS
-- ============================================

-- Clean up expired sessions
CREATE OR REPLACE FUNCTION cleanup_expired_sessions()
RETURNS BIGINT AS $$
DECLARE
    v_deleted BIGINT;
BEGIN
    DELETE FROM user_sessions
    WHERE expires_at < CURRENT_TIMESTAMP
       OR (is_active = FALSE AND created_at < CURRENT_TIMESTAMP - INTERVAL '30 days');
    
    GET DIAGNOSTICS v_deleted = ROW_COUNT;
    RETURN v_deleted;
END;
$$ LANGUAGE plpgsql;

-- Archive old transactions (move to archive table if needed)
CREATE OR REPLACE FUNCTION archive_old_transactions(p_months INT DEFAULT 12)
RETURNS BIGINT AS $$
DECLARE
    v_archived BIGINT;
BEGIN
    -- This is a placeholder - implement based on your archival strategy
    -- For now, just count transactions older than specified months
    SELECT COUNT(*)
    INTO v_archived
    FROM transactions
    WHERE transaction_date < CURRENT_TIMESTAMP - (p_months || ' months')::INTERVAL;
    
    RETURN v_archived;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- UTILITY FUNCTIONS
-- ============================================

-- Check if account number exists
CREATE OR REPLACE FUNCTION account_number_exists(p_account_number VARCHAR(20))
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS(
        SELECT 1 FROM accounts WHERE account_number = p_account_number
    );
END;
$$ LANGUAGE plpgsql;

-- Get account balance
CREATE OR REPLACE FUNCTION get_account_balance(p_account_number VARCHAR(20))
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_balance DECIMAL(15, 2);
BEGIN
    SELECT balance INTO v_balance
    FROM accounts
    WHERE account_number = p_account_number;
    
    RETURN COALESCE(v_balance, 0);
END;
$$ LANGUAGE plpgsql;

-- Comments
COMMENT ON FUNCTION get_user_total_balance(BIGINT) IS 'Calculate total balance across all active accounts for a user';
COMMENT ON FUNCTION get_available_balance(BIGINT) IS 'Get available balance including overdraft limit';
COMMENT ON FUNCTION can_withdraw(BIGINT, DECIMAL) IS 'Check if account has sufficient funds for withdrawal';
COMMENT ON FUNCTION calculate_simple_interest(BIGINT, INT) IS 'Calculate simple interest for savings account';
COMMENT ON FUNCTION apply_monthly_interest() IS 'Apply monthly interest to all eligible savings accounts';
COMMENT ON FUNCTION get_system_stats() IS 'Get system-wide statistics and metrics';
COMMENT ON FUNCTION cleanup_expired_sessions() IS 'Remove expired user sessions from database';