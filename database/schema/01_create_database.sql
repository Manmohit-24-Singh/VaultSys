-- VaultSys Database Creation Script
-- PostgreSQL 12+

-- Drop database if exists (use with caution in production)
DROP DATABASE IF EXISTS vaultsys;

-- Create database
CREATE DATABASE vaultsys
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Connect to the database
\c vaultsys;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone
SET timezone = 'UTC';

-- Create custom types/enums
CREATE TYPE account_status AS ENUM (
    'ACTIVE',
    'SUSPENDED',
    'CLOSED',
    'PENDING',
    'FROZEN',
    'DORMANT',
    'RESTRICTED',
    'UNDER_REVIEW'
);

CREATE TYPE account_type AS ENUM (
    'SAVINGS',
    'CHECKING'
);

CREATE TYPE transaction_type AS ENUM (
    'DEPOSIT',
    'WITHDRAWAL',
    'TRANSFER',
    'TRANSFER_RECEIVED',
    'INTEREST',
    'FEE',
    'REFUND',
    'PAYMENT',
    'ATM_WITHDRAWAL',
    'CHECK_DEPOSIT',
    'WIRE_TRANSFER',
    'WIRE_RECEIVED',
    'DIRECT_DEPOSIT',
    'OVERDRAFT_FEE',
    'MAINTENANCE_FEE',
    'REVERSAL',
    'ADJUSTMENT'
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED',
    'REVERSED'
);

CREATE TYPE user_role AS ENUM (
    'CUSTOMER',
    'ADMIN',
    'MANAGER'
);

-- Create schema for audit logs
CREATE SCHEMA IF NOT EXISTS audit;

COMMENT ON DATABASE vaultsys IS 'VaultSys Banking System Database';
COMMENT ON SCHEMA audit IS 'Schema for audit logging and tracking';