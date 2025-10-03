-- -----------------------------------------------------------
-- Migration: V1.0_initial_schema.sql
-- Creates the core USER and ACCOUNT tables.
-- -----------------------------------------------------------

-- Enable UUID extension for generating universally unique IDs (used for reference numbers)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users Table (Maps to User.java and IUserDAO)
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Stores value from PasswordHasher.java (salt:hash)
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- CUSTOMER, ADMIN, MANAGER
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX idx_users_username ON users (lower(username));
CREATE INDEX idx_users_email ON users (email);


-- 2. Accounts Table (Maps to Account.java, SavingsAccount.java, CheckingAccount.java)
CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    account_number VARCHAR(15) UNIQUE NOT NULL, -- e.g., 'SAV123456789' (from AccountService.java)
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CLOSED, SUSPENDED (from AccountStatus.java)
    account_type VARCHAR(20) NOT NULL, -- SAVINGS, CHECKING
    
    -- SavingsAccount fields (can be NULL for Checking)
    interest_rate NUMERIC(5, 4) DEFAULT 0.0250, 
    minimum_balance NUMERIC(19, 2) DEFAULT 100.00,
    max_withdrawals_per_month INTEGER DEFAULT 6,
    current_month_withdrawals INTEGER DEFAULT 0,
    last_interest_calc TIMESTAMP WITHOUT TIME ZONE,
    
    -- CheckingAccount fields (can be NULL for Savings)
    overdraft_limit NUMERIC(19, 2) DEFAULT 500.00,
    transaction_fee NUMERIC(5, 2) DEFAULT 0.25,
    free_transactions_per_month INTEGER DEFAULT 20,
    current_month_transactions INTEGER DEFAULT 0,
    
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_accounts_number ON accounts (account_number);
CREATE INDEX idx_accounts_user_id ON accounts (user_id);