package com.vaultsys.services;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Service for validating business rules and data constraints.
 * Provides centralized validation logic for the VaultSys application.
 */
public class ValidationService {
    
    // Regex patterns
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = 
        Pattern.compile("^[A-Z]{3}\\d{9}$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_-]{3,50}$");
    
    // Validation constraints
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 100;
    
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("50000.00");
    private static final BigDecimal MAX_BALANCE = new BigDecimal("1000000.00");
    
    /**
     * Validate email address format
     * @param email Email to validate
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number format
     * @param phone Phone number to validate
     * @return true if valid
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate account number format
     * @param accountNumber Account number to validate
     * @return true if valid
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches();
    }
    
    /**
     * Validate username format and length
     * @param username Username to validate
     * @return true if valid
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        if (trimmed.length() < MIN_USERNAME_LENGTH || 
            trimmed.length() > MAX_USERNAME_LENGTH) {
            return false;
        }
        
        return USERNAME_PATTERN.matcher(trimmed).matches();
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if valid
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH || 
            password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        
        // Check for required character types
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Validate name (first name, last name)
     * @param name Name to validate
     * @return true if valid
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || 
            trimmed.length() > MAX_NAME_LENGTH) {
            return false;
        }
        
        // Check for valid characters (letters, spaces, hyphens, apostrophes)
        return trimmed.matches("^[a-zA-Z\\s'-]+$");
    }
    
    /**
     * Validate transaction amount
     * @param amount Amount to validate
     * @return true if valid
     */
    public static boolean isValidTransactionAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        
        return amount.compareTo(MIN_TRANSACTION_AMOUNT) >= 0 && 
               amount.compareTo(MAX_TRANSACTION_AMOUNT) <= 0;
    }
    
    /**
     * Validate account balance
     * @param balance Balance to validate
     * @return true if valid
     */
    public static boolean isValidBalance(BigDecimal balance) {
        if (balance == null) {
            return false;
        }
        
        return balance.compareTo(BigDecimal.ZERO) >= 0 && 
               balance.compareTo(MAX_BALANCE) <= 0;
    }
    
    /**
     * Validate interest rate (must be between 0 and 1)
     * @param rate Interest rate to validate
     * @return true if valid
     */
    public static boolean isValidInterestRate(BigDecimal rate) {
        if (rate == null) {
            return false;
        }
        
        return rate.compareTo(BigDecimal.ZERO) >= 0 && 
               rate.compareTo(BigDecimal.ONE) <= 0;
    }
    
    /**
     * Validate that amount is positive
     * @param amount Amount to validate
     * @return true if positive
     */
    public static boolean isPositiveAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Validate that amount is non-negative
     * @param amount Amount to validate
     * @return true if non-negative
     */
    public static boolean isNonNegativeAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    /**
     * Validate string is not null or empty
     * @param str String to validate
     * @return true if not null or empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validate string length is within range
     * @param str String to validate
     * @param minLength Minimum length (inclusive)
     * @param maxLength Maximum length (inclusive)
     * @return true if within range
     */
    public static boolean isLengthInRange(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Validate that ID is positive
     * @param id ID to validate
     * @return true if positive
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }
    
    /**
     * Validate currency code (3-letter ISO code)
     * @param currency Currency code to validate
     * @return true if valid
     */
    public static boolean isValidCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }
        return currency.trim().matches("^[A-Z]{3}$");
    }
    
    /**
     * Validate account type
     * @param accountType Account type to validate
     * @return true if valid
     */
    public static boolean isValidAccountType(String accountType) {
        if (accountType == null || accountType.trim().isEmpty()) {
            return false;
        }
        
        String type = accountType.trim().toUpperCase();
        return type.equals("SAVINGS") || type.equals("CHECKING");
    }
    
    /**
     * Validate transaction type
     * @param transactionType Transaction type to validate
     * @return true if valid
     */
    public static boolean isValidTransactionType(String transactionType) {
        if (transactionType == null || transactionType.trim().isEmpty()) {
            return false;
        }
        
        String type = transactionType.trim().toUpperCase();
        String[] validTypes = {
            "DEPOSIT", "WITHDRAWAL", "TRANSFER", "TRANSFER_RECEIVED",
            "INTEREST", "FEE", "REFUND", "PAYMENT", "ATM_WITHDRAWAL",
            "CHECK_DEPOSIT", "WIRE_TRANSFER", "WIRE_RECEIVED",
            "DIRECT_DEPOSIT", "OVERDRAFT_FEE", "MAINTENANCE_FEE",
            "REVERSAL", "ADJUSTMENT"
        };
        
        for (String validType : validTypes) {
            if (validType.equals(type)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate account status
     * @param status Account status to validate
     * @return true if valid
     */
    public static boolean isValidAccountStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        
        String statusUpper = status.trim().toUpperCase();
        String[] validStatuses = {
            "ACTIVE", "SUSPENDED", "CLOSED", "PENDING",
            "FROZEN", "DORMANT", "RESTRICTED", "UNDER_REVIEW"
        };
        
        for (String validStatus : validStatuses) {
            if (validStatus.equals(statusUpper)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate user role
     * @param role User role to validate
     * @return true if valid
     */
    public static boolean isValidUserRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }
        
        String roleUpper = role.trim().toUpperCase();
        return roleUpper.equals("CUSTOMER") || 
               roleUpper.equals("ADMIN") || 
               roleUpper.equals("MANAGER");
    }
    
    /**
     * Validate description length
     * @param description Description to validate
     * @param maxLength Maximum allowed length
     * @return true if valid
     */
    public static boolean isValidDescription(String description, int maxLength) {
        if (description == null) {
            return true; // Description is optional
        }
        
        return description.length() <= maxLength;
    }
    
    /**
     * Validate overdraft limit
     * @param limit Overdraft limit to validate
     * @return true if valid
     */
    public static boolean isValidOverdraftLimit(BigDecimal limit) {
        if (limit == null) {
            return false;
        }
        
        BigDecimal maxOverdraft = new BigDecimal("5000.00");
        return limit.compareTo(BigDecimal.ZERO) >= 0 && 
               limit.compareTo(maxOverdraft) <= 0;
    }
    
    /**
     * Validate minimum balance requirement
     * @param minBalance Minimum balance to validate
     * @return true if valid
     */
    public static boolean isValidMinimumBalance(BigDecimal minBalance) {
        if (minBalance == null) {
            return false;
        }
        
        BigDecimal maxMinBalance = new BigDecimal("10000.00");
        return minBalance.compareTo(BigDecimal.ZERO) >= 0 && 
               minBalance.compareTo(maxMinBalance) <= 0;
    }
    
    /**
     * Validate that withdrawal amount doesn't exceed balance
     * @param withdrawalAmount Amount to withdraw
     * @param currentBalance Current account balance
     * @param overdraftLimit Available overdraft limit
     * @return true if withdrawal is valid
     */
    public static boolean canWithdraw(BigDecimal withdrawalAmount, 
                                     BigDecimal currentBalance, 
                                     BigDecimal overdraftLimit) {
        if (withdrawalAmount == null || currentBalance == null) {
            return false;
        }
        
        BigDecimal availableBalance = currentBalance;
        if (overdraftLimit != null) {
            availableBalance = availableBalance.add(overdraftLimit);
        }
        
        return withdrawalAmount.compareTo(availableBalance) <= 0;
    }
    
    /**
     * Validate transfer between accounts
     * @param fromAccountId Source account ID
     * @param toAccountId Destination account ID
     * @param amount Transfer amount
     * @return true if transfer is valid
     */
    public static boolean isValidTransfer(Long fromAccountId, 
                                         Long toAccountId, 
                                         BigDecimal amount) {
        if (!isValidId(fromAccountId) || !isValidId(toAccountId)) {
            return false;
        }
        
        if (fromAccountId.equals(toAccountId)) {
            return false; // Cannot transfer to same account
        }
        
        return isValidTransactionAmount(amount);
    }
    
    /**
     * Get validation error message for password
     * @param password Password to validate
     * @return Error message or null if valid
     */
    public static String getPasswordValidationError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "Password must not exceed " + MAX_PASSWORD_LENGTH + " characters";
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        
        if (!hasUpper) {
            return "Password must contain at least one uppercase letter";
        }
        if (!hasLower) {
            return "Password must contain at least one lowercase letter";
        }
        if (!hasDigit) {
            return "Password must contain at least one digit";
        }
        
        return null; // Valid
    }
    
    /**
     * Get validation error message for email
     * @param email Email to validate
     * @return Error message or null if valid
     */
    public static String getEmailValidationError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Invalid email format";
        }
        
        return null; // Valid
    }
    
    /**
     * Get validation error message for transaction amount
     * @param amount Amount to validate
     * @return Error message or null if valid
     */
    public static String getAmountValidationError(BigDecimal amount) {
        if (amount == null) {
            return "Amount is required";
        }
        
        if (amount.compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            return "Amount must be at least " + MIN_TRANSACTION_AMOUNT;
        }
        
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            return "Amount cannot exceed " + MAX_TRANSACTION_AMOUNT;
        }
        
        return null; // Valid
    }
}