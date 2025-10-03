-- VaultSys Table Creation Script
-- PostgreSQL 12+

-- Users table
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role user_role DEFAULT 'CUSTOMER' NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Accounts table
CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type account_type NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    status account_status DEFAULT 'ACTIVE' NOT NULL,
    interest_rate DECIMAL(5, 4) DEFAULT 0.0000,
    minimum_balance DECIMAL(15, 2) DEFAULT 0.00,
    overdraft_limit DECIMAL(15, 2) DEFAULT 0.00,
    transaction_fee DECIMAL(5, 2) DEFAULT 0.00,
    max_withdrawals_per_month INT DEFAULT 6,
    current_month_withdrawals INT DEFAULT 0,
    free_transactions_per_month INT DEFAULT 20,
    current_month_transactions INT DEFAULT 0,
    last_interest_calculation TIMESTAMP,
    has_debit_card BOOLEAN DEFAULT FALSE,
    debit_card_number VARCHAR(16),
    debit_card_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT balance_non_negative CHECK (balance >= -overdraft_limit),
    CONSTRAINT interest_rate_valid CHECK (interest_rate >= 0 AND interest_rate <= 1),
    CONSTRAINT minimum_balance_non_negative CHECK (minimum_balance >= 0),
    CONSTRAINT overdraft_limit_non_negative CHECK (overdraft_limit >= 0)
);

-- Transactions table
CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    to_account_id BIGINT REFERENCES accounts(account_id) ON DELETE SET NULL,
    transaction_type transaction_type NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    balance_before DECIMAL(15, 2),
    balance_after DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    description TEXT,
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    status transaction_status DEFAULT 'PENDING' NOT NULL,
    initiated_by VARCHAR(50),
    failure_reason TEXT,
    processed_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT amount_positive CHECK (amount > 0)
);

-- User sessions table (for authentication tracking)
CREATE TABLE user_sessions (
    session_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL
);

-- Account holders (for joint accounts - future enhancement)
CREATE TABLE account_holders (
    account_holder_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    relationship VARCHAR(50) DEFAULT 'PRIMARY' NOT NULL,
    permissions VARCHAR(20)[] DEFAULT ARRAY['VIEW', 'TRANSACT'],
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_account_user UNIQUE (account_id, user_id)
);

-- Audit log table
CREATE TABLE audit.audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id BIGINT NOT NULL,
    operation VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(50),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ip_address INET,
    user_agent TEXT
);

-- Failed login attempts (security tracking)
CREATE TABLE audit.failed_login_attempts (
    attempt_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    ip_address INET NOT NULL,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    failure_reason VARCHAR(100)
);

-- Scheduled transactions (for future recurring payments)
CREATE TABLE scheduled_transactions (
    scheduled_transaction_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    to_account_id BIGINT REFERENCES accounts(account_id) ON DELETE SET NULL,
    transaction_type transaction_type NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description TEXT,
    frequency VARCHAR(20) NOT NULL, -- DAILY, WEEKLY, MONTHLY, YEARLY
    start_date DATE NOT NULL,
    end_date DATE,
    last_executed_at TIMESTAMP,
    next_execution_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Transaction limits (daily/monthly limits per account)
CREATE TABLE transaction_limits (
    limit_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    transaction_type transaction_type NOT NULL,
    daily_limit DECIMAL(15, 2),
    monthly_limit DECIMAL(15, 2),
    daily_count_limit INT,
    monthly_count_limit INT,
    current_daily_amount DECIMAL(15, 2) DEFAULT 0.00,
    current_monthly_amount DECIMAL(15, 2) DEFAULT 0.00,
    current_daily_count INT DEFAULT 0,
    current_monthly_count INT DEFAULT 0,
    last_reset_date DATE DEFAULT CURRENT_DATE,
    CONSTRAINT unique_account_transaction_type UNIQUE (account_id, transaction_type)
);

-- Beneficiaries (for quick transfers)
CREATE TABLE beneficiaries (
    beneficiary_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    beneficiary_account_number VARCHAR(20) NOT NULL,
    beneficiary_name VARCHAR(200) NOT NULL,
    nickname VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- System configuration table
CREATE TABLE system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(50)
);

-- Comments
COMMENT ON TABLE users IS 'Stores user account information and credentials';
COMMENT ON TABLE accounts IS 'Stores bank account information';
COMMENT ON TABLE transactions IS 'Stores all financial transactions';
COMMENT ON TABLE user_sessions IS 'Tracks active user sessions for authentication';
COMMENT ON TABLE audit.audit_log IS 'Comprehensive audit trail for all data changes';
COMMENT ON TABLE audit.failed_login_attempts IS 'Security tracking for failed login attempts';
COMMENT ON TABLE scheduled_transactions IS 'Stores recurring/scheduled transaction definitions';
COMMENT ON TABLE transaction_limits IS 'Stores transaction limits per account';
COMMENT ON TABLE beneficiaries IS 'Stores frequently used transfer recipients';
COMMENT ON TABLE system_config IS 'System-wide configuration parameters';