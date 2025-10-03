-- VaultSys Trigger Creation Script
-- PostgreSQL 12+
-- Triggers automate business logic and maintain data integrity

-- ============================================
-- TRIGGER FUNCTIONS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to audit table changes
CREATE OR REPLACE FUNCTION audit_table_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            old_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            OLD.user_id,
            TG_OP,
            row_to_json(OLD),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            old_values,
            new_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.user_id,
            TG_OP,
            row_to_json(OLD),
            row_to_json(NEW),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            new_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.user_id,
            TG_OP,
            row_to_json(NEW),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to audit account changes
CREATE OR REPLACE FUNCTION audit_account_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            old_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            OLD.account_id,
            TG_OP,
            row_to_json(OLD),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            old_values,
            new_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.account_id,
            TG_OP,
            row_to_json(OLD),
            row_to_json(NEW),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            new_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.account_id,
            TG_OP,
            row_to_json(NEW),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to audit transaction changes
CREATE OR REPLACE FUNCTION audit_transaction_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            old_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            OLD.transaction_id,
            TG_OP,
            row_to_json(OLD),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            old_values,
            new_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.transaction_id,
            TG_OP,
            row_to_json(OLD),
            row_to_json(NEW),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit.audit_log (
            table_name,
            record_id,
            operation,
            new_values,
            changed_by,
            changed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.transaction_id,
            TG_OP,
            row_to_json(NEW),
            current_user,
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to validate transaction
CREATE OR REPLACE FUNCTION validate_transaction()
RETURNS TRIGGER AS $$
BEGIN
    -- Ensure amount is positive
    IF NEW.amount <= 0 THEN
        RAISE EXCEPTION 'Transaction amount must be positive';
    END IF;
    
    -- Ensure account exists
    IF NOT EXISTS (SELECT 1 FROM accounts WHERE account_id = NEW.account_id) THEN
        RAISE EXCEPTION 'Account does not exist: %', NEW.account_id;
    END IF;
    
    -- For transfers, ensure destination account exists
    IF NEW.transaction_type IN ('TRANSFER', 'WIRE_TRANSFER') THEN
        IF NEW.to_account_id IS NULL THEN
            RAISE EXCEPTION 'Transfer requires destination account';
        END IF;
        IF NOT EXISTS (SELECT 1 FROM accounts WHERE account_id = NEW.to_account_id) THEN
            RAISE EXCEPTION 'Destination account does not exist: %', NEW.to_account_id;
        END IF;
        IF NEW.account_id = NEW.to_account_id THEN
            RAISE EXCEPTION 'Cannot transfer to the same account';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to reset monthly counters
CREATE OR REPLACE FUNCTION reset_monthly_counters()
RETURNS TRIGGER AS $$
BEGIN
    -- Reset counters if month has changed
    IF DATE_TRUNC('month', OLD.updated_at) < DATE_TRUNC('month', NEW.updated_at) THEN
        NEW.current_month_withdrawals = 0;
        NEW.current_month_transactions = 0;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to prevent negative balance (unless overdraft allowed)
CREATE OR REPLACE FUNCTION check_account_balance()
RETURNS TRIGGER AS $$
BEGIN
    -- Allow overdraft up to the overdraft limit
    IF NEW.balance < -(NEW.overdraft_limit) THEN
        RAISE EXCEPTION 'Insufficient funds. Balance cannot go below overdraft limit';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to update transaction counts
CREATE OR REPLACE FUNCTION update_transaction_counts()
RETURNS TRIGGER AS $$
DECLARE
    v_account RECORD;
BEGIN
    -- Get account information
    SELECT * INTO v_account FROM accounts WHERE account_id = NEW.account_id;
    
    -- Update transaction count for checking accounts
    IF v_account.account_type = 'CHECKING' THEN
        UPDATE accounts 
        SET current_month_transactions = current_month_transactions + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE account_id = NEW.account_id;
    END IF;
    
    -- Update withdrawal count for savings accounts
    IF v_account.account_type = 'SAVINGS' AND 
       NEW.transaction_type IN ('WITHDRAWAL', 'TRANSFER', 'ATM_WITHDRAWAL') THEN
        UPDATE accounts 
        SET current_month_withdrawals = current_month_withdrawals + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE account_id = NEW.account_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to generate unique reference number
CREATE OR REPLACE FUNCTION generate_reference_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.reference_number IS NULL OR NEW.reference_number = '' THEN
        NEW.reference_number = 'TXN' || 
                              TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS') || 
                              LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- TRIGGERS ON USERS TABLE
-- ============================================

-- Update updated_at timestamp
CREATE TRIGGER users_update_timestamp
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Audit user changes
CREATE TRIGGER users_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW
    EXECUTE FUNCTION audit_table_changes();

-- ============================================
-- TRIGGERS ON ACCOUNTS TABLE
-- ============================================

-- Update updated_at timestamp
CREATE TRIGGER accounts_update_timestamp
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Audit account changes
CREATE TRIGGER accounts_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION audit_account_changes();

-- Reset monthly counters
CREATE TRIGGER accounts_reset_counters
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION reset_monthly_counters();

-- Check balance constraints
CREATE TRIGGER accounts_check_balance
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    WHEN (OLD.balance IS DISTINCT FROM NEW.balance)
    EXECUTE FUNCTION check_account_balance();

-- ============================================
-- TRIGGERS ON TRANSACTIONS TABLE
-- ============================================

-- Generate reference number if not provided
CREATE TRIGGER transactions_generate_reference
    BEFORE INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION generate_reference_number();

-- Validate transaction
CREATE TRIGGER transactions_validate
    BEFORE INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION validate_transaction();

-- Audit transaction changes
CREATE TRIGGER transactions_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION