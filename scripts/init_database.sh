#!/bin/bash
# init_database.sh: PostgreSQL setup script for VaultSys Banking System

# --- Configuration (MUST MATCH application.properties) ---
DB_NAME="vaultsys_db"
DB_USER="vaultsys_user"
# NOTE: Replace 'your_secure_db_password' with the actual password you used in application.properties
DB_PASSWORD="your_secure_db_password"

# --- 1. Create User and Database (Requires superuser access, e.g., 'postgres') ---

# Drop existing database and user if they exist (for clean setup)
echo "Dropping existing database and user (if they exist)..."
psql -U postgres -c "DROP DATABASE IF EXISTS $DB_NAME;"
psql -U postgres -c "DROP USER IF EXISTS $DB_USER;"

# Create user with a secure password
echo "Creating user '$DB_USER'..."
psql -U postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';"

# Create database and grant ownership to the new user
echo "Creating database '$DB_NAME' and granting privileges..."
psql -U postgres -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"

# --- 2. Create Tables (Using the new user and database) ---

echo "Connecting to $DB_NAME and creating tables..."

psql -U $DB_USER -d $DB_NAME <<-EOSQL

-- -----------------------------------------------------------
-- 1. USERS Table
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) DEFAULT 'CUSTOMER' NOT NULL, -- CUSTOMER, ADMIN, MANAGER
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITHOUT TIME ZONE
);

-- Index for faster login lookups
CREATE INDEX idx_users_username ON users (username);


-- -----------------------------------------------------------
-- 2. ACCOUNTS Table
-- -----------------------------------------------------------
CREATE TYPE account_status_enum AS ENUM (
    'ACTIVE', 'SUSPENDED', 'CLOSED', 'PENDING', 'FROZEN', 'DORMANT', 'RESTRICTED', 'UNDER_REVIEW'
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_number VARCHAR(12) UNIQUE NOT NULL,
    balance NUMERIC(19, 2) DEFAULT 0.00 NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    status account_status_enum DEFAULT 'ACTIVE' NOT NULL,
    account_type VARCHAR(20) NOT NULL, -- SAVINGS, CHECKING
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for user-specific account lookup
CREATE INDEX idx_accounts_user_id ON accounts (user_id);


-- -----------------------------------------------------------
-- 3. TRANSACTIONS Table
-- -----------------------------------------------------------
CREATE TYPE transaction_type_enum AS ENUM (
    'DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'TRANSFER_RECEIVED', 'INTEREST', 'FEE', 
    'REFUND', 'PAYMENT', 'ATM_WITHDRAWAL', 'CHECK_DEPOSIT', 'WIRE_TRANSFER', 
    'WIRE_RECEIVED', 'DIRECT_DEPOSIT', 'OVERDRAFT_FEE', 'MAINTENANCE_FEE', 
    'REVERSAL', 'ADJUSTMENT'
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE RESTRICT,
    to_account_id BIGINT REFERENCES accounts(account_id) ON DELETE RESTRICT, -- Null for non-transfers
    transaction_type transaction_type_enum NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance_before NUMERIC(19, 2),
    balance_after NUMERIC(19, 2),
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    transaction_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255),
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED' NOT NULL, -- PENDING, COMPLETED, FAILED
    initiated_by VARCHAR(50), -- Username or system
    failure_reason TEXT,
    processed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT -- For JSON or extra data
);

-- Index for quick transaction history lookup
CREATE INDEX idx_transactions_account_id ON transactions (account_id);

-- -----------------------------------------------------------
-- 4. Sample Data Insertion (Optional but recommended for testing)
-- -----------------------------------------------------------

-- Create a temporary default admin password hash (e.g., 'admin123' hashed)
-- In a real app, use the Java PasswordHasher utility to get the real hash!
DO \$\$
DECLARE
    admin_pass_hash VARCHAR(255) := 'c2FsdF9mb3JfYWRtaW46aGFzaF9mb3JfYWRtaW4='; -- Placeholder hash
BEGIN
    -- Create an initial Admin user
    INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active, last_login_at)
    VALUES ('admin', admin_pass_hash, 'admin@vaultsys.com', 'System', 'Admin', 'ADMIN', TRUE, CURRENT_TIMESTAMP)
    ON CONFLICT (username) DO NOTHING;

    -- Create an initial Customer user
    INSERT INTO users (username, password_hash, email, first_name, last_name, role)
    VALUES ('customer', admin_pass_hash, 'customer@vaultsys.com', 'John', 'Doe', 'CUSTOMER')
    ON CONFLICT (username) DO NOTHING;
END \$\$;

EOSQL

echo "Database setup complete for $DB_NAME."