-- -----------------------------------------------------------
-- Seed: seed_users.sql
-- Inserts initial ADMIN and CUSTOMER users.
-- -----------------------------------------------------------

-- WARNING: REPLACE THE HASH BELOW!
-- Run your PasswordHasher.hashPassword("TestPassword123!") to generate a secure hash.
-- The value below is an illustrative placeholder.
\set secure_hash '''AQIDBAUGBwgJCgsMDQ4PEA==:2e8jXg/u/Y+f0h0c+g9Yy+m1xL4kHj5zO5r7pA8L4fI='''

-- 1. Insert Core Users
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) VALUES
('adminuser', :secure_hash, 'admin@vaultsys.com', 'System', 'Admin', 'ADMIN', TRUE),
('manager1', :secure_hash, 'manager@vaultsys.com', 'Alice', 'Manager', 'MANAGER', TRUE),
('customer1', :secure_hash, 'user1@vaultsys.com', 'Bob', 'Customer', 'CUSTOMER', TRUE),
('customer2', :secure_hash, 'user2@vaultsys.com', 'Charlie', 'Client', 'CUSTOMER', TRUE);

-- Get User IDs for Foreign Keys in the next script
-- NOTE: In a migration tool, these would be variables or a separate script.
-- For a simple script, we use a temp table/CTE approach.

-- Temporary table (or CTE in PostgreSQL) to hold IDs for the next step (seed_accounts)
-- This is often handled by the migration tool itself, but is shown here for completeness.
-- Example of setting variables for the next script to reference:
-- SELECT user_id FROM users WHERE username = 'customer1' INTO user1_id;
-- SELECT user_id FROM users WHERE username = 'customer2' INTO user2_id;