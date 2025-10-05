# VaultSys

A secure, multi-tier banking simulation system featuring robust Java-based business logic and PostgreSQL for reliable data persistence. VaultSys provides comprehensive financial account management with enterprise-grade security and transaction processing.

## 📋 Overview

VaultSys is a full-featured banking system designed to demonstrate modern software architecture patterns and best practices. Built with a multi-layer architecture, it provides secure account management, transaction processing, and comprehensive financial operations suitable for banking simulations and educational purposes.

## ✨ Features

### Account Management
- **Multiple Account Types**
  - Savings Accounts with interest calculation
  - Checking Accounts with overdraft protection
- **Account Operations**
  - Create and manage accounts
  - Check balance and available funds
  - Close accounts
  - Update account status (Active, Suspended, Frozen, Dormant)
  - Search accounts by type or status

### Transaction Processing
- **Core Transactions**
  - Deposits: Add funds to accounts
  - Withdrawals: Remove funds with validation
  - Transfers: Move funds between accounts
- **Transaction Features**
  - Real-time balance updates
  - Transaction history and analytics
  - Transaction filtering by type and date range
  - Reference number generation
  - Comprehensive transaction receipts

### User Authentication & Security
- **Secure Authentication**
  - User registration and login
  - Password hashing with SHA-256 and salt
  - Password strength validation
  - Session management
- **Authorization**
  - Role-based access control (Customer, Manager, Admin)
  - User profile management
  - Password change functionality

### Interest Calculation
- **Multiple Interest Methods**
  - Simple interest calculation
  - Compound interest (daily, monthly, yearly)
  - APY (Annual Percentage Yield) calculation
  - Future value with regular deposits

### Validation & Business Rules
- Comprehensive input validation
- Transaction amount limits
- Balance validation
- Email and phone number format validation
- Account number format verification

## 🏗️ Architecture

VaultSys follows a **multi-tier MVC architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│     Presentation Layer (Controllers)    │
│  UserController | AccountController |   │
│       TransactionController             │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Business Logic Layer (Services)    │
│   AuthService | AccountService |        │
│  TransactionService | ValidationService │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│    Data Access Layer (DAO Pattern)      │
│   UserDAO | AccountDAO | TransactionDAO │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         Database Layer (PostgreSQL)     │
└─────────────────────────────────────────┘
```

### Key Components

#### Controllers (Presentation Layer)
- **UserController**: User authentication and profile management
- **AccountController**: Account creation and management
- **TransactionController**: Transaction processing and history

#### Services (Business Logic Layer)
- **AuthService**: Authentication and authorization
- **AccountService**: Account business logic
- **TransactionService**: Transaction processing and validation
- **ValidationService**: Input validation and business rules
- **InterestCalculator**: Interest calculation utilities

#### DAO (Data Access Layer)
- **UserDAO**: User data persistence
- **AccountDAO**: Account data persistence
- **TransactionDAO**: Transaction data persistence

#### Utilities
- **DatabaseConnection**: Connection pooling and management
- **Logger**: Application logging
- **PasswordHasher**: Secure password hashing
- **ConfigLoader**: Configuration management
- **DateUtils**: Date and time utilities

## 🛠️ Tech Stack

### Core Technologies
- **Java** (JDK 11 or higher) - Primary application language
- **PostgreSQL** (v12+) - Database management system
- **JDBC** - Database connectivity

### Design Patterns
- **MVC (Model-View-Controller)** - Application architecture
- **DAO (Data Access Object)** - Data persistence pattern
- **Singleton** - Logger and configuration management
- **Dependency Injection** - Loose coupling between components
- **Factory Pattern** - Object creation

### OOP Principles Demonstrated
- Encapsulation
- Inheritance (Account → SavingsAccount/CheckingAccount)
- Polymorphism
- Abstraction (DAO interfaces)
- Composition over Inheritance

## 📦 Prerequisites

Before running VaultSys, ensure you have:

- **Java Development Kit (JDK)** 11 or higher
- **PostgreSQL** 12 or higher
- **PostgreSQL JDBC Driver** (postgresql-42.x.x.jar)
- **Git** (for cloning the repository)

### Installation Commands

#### Ubuntu/Debian
```bash
# Install Java
sudo apt update
sudo apt install openjdk-11-jdk

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Verify installations
java -version
psql --version
```

#### macOS
```bash
# Install Java
brew install openjdk@11

# Install PostgreSQL
brew install postgresql
brew services start postgresql

# Verify installations
java -version
psql --version
```

#### Windows
- Download and install [Java JDK](https://www.oracle.com/java/technologies/downloads/)
- Download and install [PostgreSQL](https://www.postgresql.org/download/windows/)

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Manmohit-24-Singh/VaultSys.git
cd VaultSys
```

### 2. Set Up PostgreSQL Database

```bash
# Start PostgreSQL service
sudo service postgresql start  # Linux
brew services start postgresql  # macOS

# Access PostgreSQL
sudo -u postgres psql

# Create database and user
CREATE DATABASE vaultsys;
CREATE USER vaultsys_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE vaultsys TO vaultsys_user;
\q
```

### 3. Create Database Schema

Create the necessary tables by running the SQL schema:

```sql
-- Users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Accounts table
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    interest_rate DECIMAL(5, 4),
    minimum_balance DECIMAL(15, 2),
    overdraft_limit DECIMAL(15, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INTEGER REFERENCES accounts(account_id),
    to_account_id INTEGER REFERENCES accounts(account_id),
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    balance_before DECIMAL(15, 2),
    balance_after DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'PENDING',
    description TEXT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    initiated_by VARCHAR(50)
);

-- Indexes for performance
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
```

### 4. Configure Database Connection

Edit the configuration in `java-service/src/resources/application.properties`:

```properties
# Database Configuration
db.url=jdbc:postgresql://localhost:5432/vaultsys
db.user=vaultsys_user
db.password=your_secure_password
db.driver=org.postgresql.Driver
db.pool.size=10

# Application Configuration
app.name=VaultSys
app.version=1.0.0
app.environment=development

# Account Configuration
account.savings.interest.rate=0.025
account.savings.min.balance=100.00
account.checking.overdraft.limit=500.00

# Transaction Configuration
transaction.max.amount=50000.00
transaction.daily.limit=100000.00

# Security Configuration
security.password.min.length=8
security.password.max.length=128
```

### 5. Download PostgreSQL JDBC Driver

Download the JDBC driver from [PostgreSQL JDBC Download](https://jdbc.postgresql.org/download.html) and place it in your project's `lib` directory.

### 6. Compile the Application

```bash
# Create bin directory for compiled classes
mkdir -p bin

# Compile Java sources
javac -d bin -cp "lib/*" java-service/src/com/vaultsys/**/*.java

# Or use your IDE (Eclipse, IntelliJ IDEA, NetBeans)
```

### 7. Run the Application

```bash
# Run from command line
java -cp "bin:lib/*" com.vaultsys.Main

# Or run from your IDE
```

## 🎯 Usage

### Starting the Application

When you run VaultSys, you'll see the welcome banner:

```
╔══════════════════════════════════════════════════════╗
║                                                      ║
║              VAULTSYS BANKING SYSTEM                 ║
║                                                      ║
║          Secure Multi-Tier Banking Solution          ║
║                                                      ║
╚══════════════════════════════════════════════════════╝
```

### Authentication Flow

1. **Register a New Account**
   - Choose "Register New Account"
   - Enter username, email, name, and password
   - System creates user with CUSTOMER role

2. **Login**
   - Enter username and password
   - System validates credentials
   - Upon success, access main menu

### Main Menu Operations

#### 1. Account Management
- Create Savings/Checking accounts
- View account details
- Check balances
- Update account status
- Close accounts

#### 2. Transactions
- **Deposit**: Add funds to an account
- **Withdraw**: Remove funds (with validation)
- **Transfer**: Move funds between accounts
- View transaction history
- Filter transactions by type or date

#### 3. User Profile
- View profile information
- Change password
- Logout

### Example Workflow

```
1. Register User
   └─> Username: john_doe
   └─> Email: john@example.com
   └─> Password: SecurePass123

2. Login
   └─> Username: john_doe
   └─> Password: SecurePass123

3. Create Savings Account
   └─> Initial Deposit: $1000.00
   └─> Account Number: SAV123456789012

4. Deposit
   └─> Account: SAV123456789012
   └─> Amount: $500.00
   └─> New Balance: $1500.00

5. View Transaction History
   └─> Shows all deposits, withdrawals, transfers
```

## 📂 Project Structure

```
VaultSys/
├── java-service/
│   └── src/
│       └── com/
│           └── vaultsys/
│               ├── Main.java                 # Application entry point
│               ├── controllers/              # Presentation layer
│               │   ├── AccountController.java
│               │   ├── TransactionController.java
│               │   └── UserController.java
│               ├── services/                 # Business logic layer
│               │   ├── AccountService.java
│               │   ├── AuthService.java
│               │   ├── TransactionService.java
│               │   ├── ValidationService.java
│               │   └── InterestCalculator.java
│               ├── dao/                      # Data access layer
│               │   ├── interfaces/
│               │   │   ├── IAccountDAO.java
│               │   │   ├── ITransactionDAO.java
│               │   │   └── IUserDAO.java
│               │   └── impl/
│               │       ├── AccountDAOImpl.java
│               │       ├── TransactionDAOImpl.java
│               │       └── UserDAOImpl.java
│               ├── models/                   # Data models
│               │   ├── Account.java
│               │   ├── SavingsAccount.java
│               │   ├── CheckingAccount.java
│               │   ├── Transaction.java
│               │   ├── User.java
│               │   ├── AccountStatus.java
│               │   └── TransactionType.java
│               ├── exceptions/               # Custom exceptions
│               │   ├── AccountException.java
│               │   ├── AccountNotFoundException.java
│               │   ├── AuthenticationException.java
│               │   ├── DatabaseException.java
│               │   ├── InsufficientFundsException.java
│               │   └── InvalidTransactionException.java
│               └── utils/                    # Utility classes
│                   ├── ConfigLoader.java
│                   ├── DatabaseConnection.java
│                   ├── DateUtils.java
│                   ├── Logger.java
│                   └── PasswordHasher.java
├── lib/                                      # External libraries
│   └── postgresql-42.x.x.jar
├── resources/
│   └── application.properties                # Configuration file
└── README.md
```

## 🔒 Security Features

### Password Security
- **SHA-256 Hashing**: All passwords are hashed with salt
- **Secure Random Salt**: 16-byte random salt per password
- **10,000 Iterations**: PBKDF2-like iteration for added security
- **Password Requirements**:
  - Minimum 8 characters
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit

### Transaction Security
- All transactions are logged with reference numbers
- Balance validation before withdrawals/transfers
- Status tracking (PENDING, COMPLETED, FAILED)
- Audit trail with timestamps

### Database Security
- Connection pooling prevents connection exhaustion
- Parameterized queries prevent SQL injection
- User role-based access control
- Account status validation

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
```
Error: Database connection failed
```
**Solution**: 
- Verify PostgreSQL is running: `sudo service postgresql status`
- Check credentials in `application.properties`
- Ensure database `vaultsys` exists

**JDBC Driver Not Found**
```
ClassNotFoundException: org.postgresql.Driver
```
**Solution**: 
- Download PostgreSQL JDBC driver
- Add to classpath: `-cp "lib/postgresql-42.x.x.jar"`

**Login Failed**
```
Error: Invalid username or password
```
**Solution**:
- Verify user exists in database
- Check password meets requirements
- Ensure account is active

**Insufficient Funds**
```
Error: Insufficient funds for withdrawal
```
**Solution**:
- Check account balance
- Verify withdrawal amount
- Consider overdraft limit for checking accounts

## 📊 Database Schema Details

### Users Table
- Stores user credentials and profile information
- Password hashed with salt for security
- Role-based access control

### Accounts Table
- Supports multiple account types (Savings, Checking)
- Tracks balance, status, and account-specific features
- Foreign key relationship to users

### Transactions Table
- Complete transaction history
- Supports deposits, withdrawals, and transfers
- Tracks balance before/after each transaction

## 🚀 Future Enhancements

- [ ] Web-based GUI (Spring Boot + React)
- [ ] RESTful API for external integrations
- [ ] Mobile application support
- [ ] Email notifications for transactions
- [ ] Two-factor authentication (2FA)
- [ ] Transaction limits and daily quotas
- [ ] Account statements and reports
- [ ] Scheduled payments
- [ ] Bill payment integration
- [ ] Investment accounts
- [ ] Loan management
- [ ] Credit card processing
- [ ] ATM simulation
- [ ] Check processing
- [ ] Wire transfers
- [ ] Multi-currency support

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards
- Follow Java naming conventions
- Write JavaDoc comments for public methods
- Include unit tests for new features
- Maintain consistent code formatting
- Handle exceptions appropriately

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

**Manmohit Singh**
- GitHub: [@Manmohit-24-Singh](https://github.com/Manmohit-24-Singh)
- Project Link: [https://github.com/Manmohit-24-Singh/VaultSys](https://github.com/Manmohit-24-Singh/VaultSys)



## 📞 Support

For support, issues, or feature requests:
- Open an issue on [GitHub Issues](https://github.com/Manmohit-24-Singh/VaultSys/issues)
- Contact the maintainer through GitHub

---
