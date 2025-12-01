# VaultSys Banking System

Student-level banking simulation with Java & PostgreSQL. Demonstrates secure authentication, transaction management, and ACID compliance.

---

## Features

- **Secure Authentication**: SHA-256 + salt, password strength validation
- **Banking Operations**: Deposit, withdraw, transfer (atomic)
- **Transaction History**: Full audit trail with timestamps
- **Admin Functions**: View all users, run 10k transaction simulations
- **Input Validation**: Comprehensive checks at UI and service layers

---

## Quick Start

### Prerequisites
- Java 11+
- PostgreSQL 12+
- JDBC Driver (included in `lib/`)

### Setup

```bash
# 1. Clone repository
git clone https://github.com/Manmohit-24-Singh/VaultSys.git
cd VaultSys

# 2. Create database
sudo -u postgres psql
CREATE DATABASE vaultsys_student;
CREATE USER student WITH PASSWORD 'student';
GRANT ALL PRIVILEGES ON DATABASE vaultsys_student TO student;
\q

# 3. Run schema
psql -U student -d vaultsys_student -f database/schema.sql

# 4. Configure database (edit if needed)
cp db.properties.example db.properties

# 5. Compile
javac -d bin -cp "lib/*" src/com/vaultsys/*.java

# 6. Run
java -cp "bin:lib/*" com.vaultsys.Main
```

---

## Usage

### Register New User
```
Username: yourname
Password: SecurePass123  (min 8 chars, uppercase+lowercase+digit)
Full Name: Your Name
```

### Login & Banking
```
1. View Balance    → Check account balances
2. Deposit         → Add funds
3. Withdraw        → Remove funds (with balance check)
4. Transfer        → Send money to another user
5. History         → View last 10 transactions
6. Logout          → End session
```

### Admin Functions (username: admin, password: admin123)
```
7. View All Users           → See all users and balances
8. Run Simulation (10k)     → Performance test with batch processing
```

---

## Project Structure

```
VaultSys/
├── src/com/vaultsys/
│   ├── Main.java              # CLI entry point
│   ├── User.java              # User model
│   ├── AuthService.java       # Authentication + security
│   ├── BankingService.java    # Core banking + ACID transactions
│   ├── AdminService.java      # Admin features
│   ├── SimulationService.java # Performance testing
│   └── DatabaseHelper.java    # DB connection + config
├── database/
│   └── schema.sql             # Database schema
├── lib/
│   └── postgresql-42.7.3.jar  # JDBC driver
├── bin/                       # Compiled classes
└── db.properties              # Database config
```

---

## Database Schema

### users
```sql
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,  -- Random salt per user
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'CUSTOMER'
);
```

### accounts
```sql
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00
);
```

### transactions
```sql
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INT REFERENCES accounts(account_id),
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255)
);
```

---

## Security Features

### Password Security
- **SHA-256 hashing** with random salt per user
- **Salt generation**: 16 bytes via SecureRandom
- **Strength requirements**: 8+ chars, uppercase, lowercase, digit
- **Protection**: Prevents rainbow table attacks

### Database Security
- **PreparedStatements** prevent SQL injection
- **ACID transactions** ensure data consistency
- **Atomic transfers** (both accounts update or neither)
- **Input validation** at multiple layers

---

## Interview Talking Points

### Technical Highlights
1. **ACID Transactions**: Atomic transfers with rollback on failure
2. **Password Security**: Salt + SHA-256 (explain why salt matters)
3. **Batch Processing**: 10k transactions in ~2-5 seconds
4. **Input Validation**: Defense in depth (UI + service layers)
5. **Configuration**: Externalized to `db.properties` (no hardcoded credentials)

### Code Walkthrough
- **BankingService.transfer()**: Demonstrates transaction management
- **AuthService.hashPassword()**: Shows salt implementation
- **SimulationService**: Batch processing with addBatch/executeBatch
- **Main.java**: Menu-driven architecture with state management

### What You'd Improve
- Add connection pooling (HikariCP)
- Implement unit tests (JUnit)
- Use PBKDF2 instead of SHA-256 for password hashing
- Add logging framework (SLF4J)
- Implement pagination for transaction history

---

## Common Commands

```bash
# Recompile after changes
javac -d bin -cp "lib/*" src/com/vaultsys/*.java

# Run application
java -cp "bin:lib/*" com.vaultsys.Main

# Reset database
psql -U student -d vaultsys_student -f database/schema.sql

# View database
psql -U student -d vaultsys_student
SELECT * FROM users;
SELECT * FROM accounts;
SELECT * FROM transactions;
```

---

## Performance

- **Single Transaction**: ~50-100ms
- **10k Transactions (batched)**: ~2-5 seconds
- **Batch Size**: 1,000 per executeBatch()

---

## Author

**Manmohit Singh**  
GitHub: [@Manmohit-24-Singh](https://github.com/Manmohit-24-Singh)

---

## License

MIT License - Open Source
