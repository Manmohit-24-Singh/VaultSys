-- -----------------------------------------------------------
-- Migration: V1.1_add_audit_tables.sql
-- Creates the TRANSACTIONS table and a generic SYSTEM_AUDIT_LOG table.
-- -----------------------------------------------------------

-- 1. Transactions Table (Maps to Transaction.java and ITransactionDAO)
CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts (account_id) ON DELETE RESTRICT,
    to_account_id BIGINT REFERENCES accounts (account_id) ON DELETE RESTRICT, -- Nullable for deposits/withdrawals
    transaction_type VARCHAR(30) NOT NULL, -- DEPOSIT, WITHDRAWAL, TRANSFER (from TransactionType.java)
    amount NUMERIC(19, 2) NOT NULL,
    balance_before NUMERIC(19, 2),
    balance_after NUMERIC(19, 2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    transaction_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255),
    reference_number UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(), -- Use UUID data type for reference
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', -- PENDING, COMPLETED, FAILED, REVERSED
    initiated_by VARCHAR(50) NOT NULL DEFAULT 'System', -- Username or 'System'
    failure_reason VARCHAR(255),
    processed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB -- For storing JSON metadata (e.g., source IP, device info)

    -- Constraint to prevent self-transfers
    CONSTRAINT chk_no_self_transfer CHECK (account_id <> to_account_id)
);

CREATE INDEX idx_transactions_account_id ON transactions (account_id);
CREATE INDEX idx_transactions_date ON transactions (transaction_date);

-- 2. System Audit Log Table (For Logger.java audit/security events)
CREATE TABLE system_audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    log_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    log_level VARCHAR(10) NOT NULL, -- DEBUG, INFO, WARN, ERROR (from Logger.java)
    source VARCHAR(50), -- e.g., AuthService, AccountService, Main
    user_id BIGINT REFERENCES users (user_id) ON DELETE SET NULL, -- Nullable if system event
    message TEXT NOT NULL,
    ip_address INET,
    metadata JSONB -- Store additional context like request headers, stack traces
);

CREATE INDEX idx_audit_log_level ON system_audit_log (log_level);
CREATE INDEX idx_audit_log_time ON system_audit_log (log_time DESC);