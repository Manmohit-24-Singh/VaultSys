-- -----------------------------------------------------------
-- Seed: seed_accounts.sql
-- Inserts initial accounts and opening transactions.
-- This script assumes the IDs from seed_users.sql are available (e.g., via temporary variables).
-- -----------------------------------------------------------

-- **IMPORTANT: YOU NEED TO DEFINE THESE VARS based on the output of seed_users.sql**
-- Assuming:
-- adminuser (ID 1)
-- customer1 (ID 3)
-- customer2 (ID 4)
\set admin_id 1
\set user1_id 3
\set user2_id 4

-- 1. Insert Initial Accounts (Based on Java Subclasses)

-- Customer 1: CHECKING Account (CHK200000001)
INSERT INTO accounts (user_id, account_number, balance, account_type, overdraft_limit) VALUES
(
    :user1_id, 
    'CHK200000001', 
    500.50, 
    'CHECKING',
    1000.00
);
SELECT account_id FROM accounts WHERE account_number = 'CHK200000001' INTO chk1_id;

-- Customer 1: SAVINGS Account (SAV200000002)
INSERT INTO accounts (user_id, account_number, balance, account_type, minimum_balance) VALUES
(
    :user1_id, 
    'SAV200000002', 
    200.00, 
    'SAVINGS',
    100.00
);
SELECT account_id FROM accounts WHERE account_number = 'SAV200000002' INTO sav1_id;

-- Customer 2: SAVINGS Account (SAV300000003) - Dormant/Low Balance test case
INSERT INTO accounts (user_id, account_number, balance, account_type, minimum_balance, status) VALUES
(
    :user2_id, 
    'SAV300000003', 
    50.00, 
    'SAVINGS',
    100.00,
    'RESTRICTED'
);
SELECT account_id FROM accounts WHERE account_number = 'SAV300000003' INTO sav2_id;

-- 2. Insert Initial Transactions (Opening Deposits)

-- Transaction for CHK200000001 (Customer 1)
INSERT INTO transactions (account_id, transaction_type, amount, balance_before, balance_after, description, initiated_by) VALUES
(
    :chk1_id, 
    'DEPOSIT', 
    500.50, 
    0.00, 
    500.50, 
    'Initial Account Opening Deposit', 
    'System'
);

-- Transaction for SAV200000002 (Customer 1)
INSERT INTO transactions (account_id, transaction_type, amount, balance_before, balance_after, description, initiated_by) VALUES
(
    :sav1_id, 
    'DEPOSIT', 
    200.00, 
    0.00, 
    200.00, 
    'Initial Account Opening Deposit', 
    'System'
);