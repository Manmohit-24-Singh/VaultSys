# VaultSys - Student Banking System

A Java-based CLI banking application with PostgreSQL demonstrating **16 core banking functions** across user and admin operations, featuring ACID-compliant transactions and batch processing capabilities.

## Features

### User Operations (10 Functions)
1. **Register** - Create new account
2. **Login** - Authenticate user
3. **Deposit** - Add funds
4. **Withdraw** - Remove funds (with freeze check)
5. **Transfer** - Atomic money transfer between users
6. **Change Password** - Update credentials
7. **Close Account** - Delete user and data
8. **View Transactions** - Last 5 transactions
9. **Calculate Interest** - Simple interest calculator
10. **View Cashflow** - Personal income/expense report

### Admin Operations (6 Functions)
11. **Freeze Account** - Lock/unlock user accounts
12. **View All Transactions** - Global transaction log
13. **View Reserves** - Total bank balance
14. **View Insights** - User registration data
15. **View System Cashflow** - Bank-wide money flow
16. **Run Simulation** - Insert 10,000 test transactions (batch mode)

## Tech Stack
- **Language:** Java 11+
- **Database:** PostgreSQL 14+
- **JDBC Driver:** PostgreSQL 42.7.1
- **Architecture:** DAO pattern with atomic transactions

## Setup

### Prerequisites
```bash
# Install PostgreSQL
brew install postgresql@14

# Start PostgreSQL
brew services start postgresql@14

# Create database and user
psql postgres -c "CREATE USER student WITH PASSWORD 'student';"
psql postgres -c "CREATE DATABASE vaultsys_student OWNER student;"
```

### Compile
```bash
javac -d bin -cp "lib/*" src/com/vaultsys/*.java
```

### Run
```bash
./run.sh
# OR
java -cp "bin:lib/*" com.vaultsys.Main
```

## Database Schema

Auto-created on first run via `Database.init()`:

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE,
    password TEXT,
    balance DECIMAL(15,2) DEFAULT 0.00,
    is_frozen BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INT,
    type TEXT,
    amount DECIMAL(15,2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Testing Workflow

```bash
# 1. Run application
./run.sh

# 2. Create account (Option 3)
Username: alice
Password: pass123

# 3. Login (Option 1)
Username: alice
Password: pass123

# 4. Test operations
Deposit: $1000
Withdraw: $200
Transfer: $50 to bob

# 5. Admin login (Option 2)
Password: admin123
Run Simulation → Adds 10k records
View Reserves → Check total balance

# 6. Verify database
psql -U student -d vaultsys_student -c "SELECT * FROM users;"
psql -U student -d vaultsys_student -c "SELECT COUNT(*) FROM transactions;"
```

## Project Structure
```
VaultSys/
├── src/com/vaultsys/
│   ├── Database.java       # Connection + Schema
│   ├── BankingSystem.java  # 16 Business Functions
│   └── Main.java           # CLI Interface
├── bin/                    # Compiled .class files
├── lib/
│   └── postgresql.jar      # JDBC Driver
└── run.sh                  # Execution script
```

## Key Implementation Highlights

- **ACID Transactions:** Transfer uses `setAutoCommit(false)` + `commit()`
- **Batch Processing:** 10k simulation uses `addBatch()` + `executeBatch()`
- **Security:** Account freeze prevents withdrawals/transfers
- **Prepared Statements:** All queries use parameterized SQL (SQL injection safe)
- **Resource Management:** Try-with-resources for auto-close
- **Atomic Operations:** Transfer is all-or-nothing

## Reset Database
```bash
psql -U student -d vaultsys_student -c "DROP TABLE IF EXISTS transactions, users CASCADE;"
./run.sh  # Recreates schema
```

## Admin Credentials
- **Username:** N/A (direct password)
- **Password:** `admin123`
