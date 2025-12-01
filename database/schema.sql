-- VaultSys Database Schema

DROP DATABASE IF EXISTS vaultsys_student;
CREATE DATABASE vaultsys_student;

\c vaultsys_student;

-- Users Table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Storing hashed passwords
    password_salt VARCHAR(255) NOT NULL, -- Random salt for each password
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'CUSTOMER' -- 'CUSTOMER' or 'ADMIN'
);

-- Accounts Table
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    account_type VARCHAR(20) NOT NULL, -- 'SAVINGS', 'CHECKING'
    balance DECIMAL(15, 2) DEFAULT 0.00
);

-- Transactions Table
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INT REFERENCES accounts(account_id),
    transaction_type VARCHAR(20) NOT NULL, -- 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER'
    amount DECIMAL(15, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255)
);

-- Insert a default admin user (password: admin123)
-- Hash generated using SHA-256 with salt for "admin123"
INSERT INTO users (username, password_hash, password_salt, full_name, role)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'adminsalt', 'System Admin', 'ADMIN');

-- Create an account for the admin
INSERT INTO accounts (user_id, account_type, balance)
VALUES ((SELECT user_id FROM users WHERE username = 'admin'), 'CHECKING', 0.00);
