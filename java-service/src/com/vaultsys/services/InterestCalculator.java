package com.vaultsys.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Service for calculating interest on savings accounts.
 * Supports simple interest, compound interest, and daily interest calculations.
 */
public class InterestCalculator {
    
    private static final int DECIMAL_PLACES = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DAYS_IN_YEAR = 365;
    private static final int MONTHS_IN_YEAR = 12;
    
    /**
     * Calculate simple interest
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal, e.g., 0.05 for 5%)
     * @param years Number of years
     * @return Interest amount
     */
    public static BigDecimal calculateSimpleInterest(BigDecimal principal, 
                                                      BigDecimal annualRate, 
                                                      double years) {
        if (principal == null || annualRate == null) {
            return BigDecimal.ZERO;
        }
        
        // Interest = Principal × Rate × Time
        BigDecimal interest = principal
            .multiply(annualRate)
            .multiply(BigDecimal.valueOf(years));
        
        return interest.setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }
    
    /**
     * Calculate simple interest for a number of days
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @param days Number of days
     * @return Interest amount
     */
    public static BigDecimal calculateSimpleInterestForDays(BigDecimal principal, 
                                                             BigDecimal annualRate, 
                                                             long days) {
        if (principal == null || annualRate == null) {
            return BigDecimal.ZERO;
        }
        
        // Interest = Principal × Rate × (Days / 365)
        BigDecimal years = BigDecimal.valueOf(days)
            .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, ROUNDING_MODE);
        
        return calculateSimpleInterest(principal, annualRate, years.doubleValue());
    }
    
    /**
     * Calculate monthly interest (simple)
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @return Monthly interest amount
     */
    public static BigDecimal calculateMonthlyInterest(BigDecimal principal, 
                                                       BigDecimal annualRate) {
        if (principal == null || annualRate == null) {
            return BigDecimal.ZERO;
        }
        
        // Monthly Interest = Principal × (Annual Rate / 12)
        BigDecimal monthlyRate = annualRate
            .divide(BigDecimal.valueOf(MONTHS_IN_YEAR), 10, ROUNDING_MODE);
        
        BigDecimal interest = principal.multiply(monthlyRate);
        return interest.setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }
    
    /**
     * Calculate daily interest (simple)
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @return Daily interest amount
     */
    public static BigDecimal calculateDailyInterest(BigDecimal principal, 
                                                     BigDecimal annualRate) {
        if (principal == null || annualRate == null) {
            return BigDecimal.ZERO;
        }
        
        // Daily Interest = Principal × (Annual Rate / 365)
        BigDecimal dailyRate = annualRate
            .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, ROUNDING_MODE);
        
        BigDecimal interest = principal.multiply(dailyRate);
        return interest.setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }
    
    /**
     * Calculate compound interest
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @param years Number of years
     * @param compoundingFrequency Number of times interest compounds per year
     * @return Total amount (principal + interest)
     */
    public static BigDecimal calculateCompoundInterest(BigDecimal principal, 
                                                        BigDecimal annualRate, 
                                                        double years,
                                                        int compoundingFrequency) {
        if (principal == null || annualRate == null) {
            return principal;
        }
        
        // A = P(1 + r/n)^(nt)
        // Where: A = final amount, P = principal, r = annual rate, 
        //        n = compounding frequency, t = time in years
        
        BigDecimal rate = annualRate
            .divide(BigDecimal.valueOf(compoundingFrequency), 10, ROUNDING_MODE);
        BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
        
        double exponent = compoundingFrequency * years;
        double result = Math.pow(onePlusRate.doubleValue(), exponent);
        
        BigDecimal finalAmount = principal.multiply(BigDecimal.valueOf(result));
        return finalAmount.setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }
    
    /**
     * Calculate compound interest (interest only, not total amount)
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @param years Number of years
     * @param compoundingFrequency Number of times interest compounds per year
     * @return Interest amount only
     */
    public static BigDecimal calculateCompoundInterestOnly(BigDecimal principal, 
                                                            BigDecimal annualRate, 
                                                            double years,
                                                            int compoundingFrequency) {
        BigDecimal totalAmount = calculateCompoundInterest(principal, annualRate, 
                                                          years, compoundingFrequency);
        return totalAmount.subtract(principal);
    }
    
    /**
     * Calculate compound interest with monthly compounding
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @param months Number of months
     * @return Total amount (principal + interest)
     */
    public static BigDecimal calculateMonthlyCompoundInterest(BigDecimal principal, 
                                                               BigDecimal annualRate, 
                                                               int months) {
        double years = months / 12.0;
        return calculateCompoundInterest(principal, annualRate, years, MONTHS_IN_YEAR);
    }
    
    /**
     * Calculate compound interest with daily compounding
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @param days Number of days
     * @return Total amount (principal + interest)
     */
    public static BigDecimal calculateDailyCompoundInterest(BigDecimal principal, 
                                                             BigDecimal annualRate, 
                                                             long days) {
        double years = days / (double) DAYS_IN_YEAR;
        return calculateCompoundInterest(principal, annualRate, years, DAYS_IN_YEAR);
    }
    
    /**
     * Calculate interest between two dates
     * @param principal Principal amount
     * @param annualRate Annual interest rate (as decimal)
     * @param startDate Start date
     * @param endDate End date
     * @return Interest amount
     */
    public static BigDecimal calculateInterestBetweenDates(BigDecimal principal, 
                                                            BigDecimal annualRate,
                                                            LocalDate startDate, 
                                                            LocalDate endDate) {
        if (principal == null || annualRate == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }
        
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            return BigDecimal.ZERO;
        }
        
        return calculateSimpleInterestForDays(principal, annualRate, days);
    }
    
    /**
     * Calculate APY (Annual Percentage Yield) from APR
     * APY accounts for compounding, APR does not
     * @param apr Annual Percentage Rate (as decimal)
     * @param compoundingFrequency Number of times interest compounds per year
     * @return APY (as decimal)
     */
    public static BigDecimal calculateAPY(BigDecimal apr, int compoundingFrequency) {
        if (apr == null) {
            return BigDecimal.ZERO;
        }
        
        // APY = (1 + APR/n)^n - 1
        BigDecimal rate = apr
            .divide(BigDecimal.valueOf(compoundingFrequency), 10, ROUNDING_MODE);
        BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
        
        double result = Math.pow(onePlusRate.doubleValue(), compoundingFrequency);
        BigDecimal apy = BigDecimal.valueOf(result).subtract(BigDecimal.ONE);
        
        return apy.setScale(6, ROUNDING_MODE); // 6 decimal places for APY
    }
    
    /**
     * Calculate effective annual rate considering daily compounding
     * @param nominalRate Nominal annual rate (as decimal)
     * @return Effective annual rate (as decimal)
     */
    public static BigDecimal calculateEffectiveAnnualRate(BigDecimal nominalRate) {
        return calculateAPY(nominalRate, DAYS_IN_YEAR);
    }
    
    /**
     * Calculate future value with regular deposits
     * @param initialPrincipal Initial principal amount
     * @param monthlyDeposit Regular monthly deposit amount
     * @param annualRate Annual interest rate (as decimal)
     * @param months Number of months
     * @return Future value
     */
    public static BigDecimal calculateFutureValueWithDeposits(BigDecimal initialPrincipal,
                                                               BigDecimal monthlyDeposit,
                                                               BigDecimal annualRate,
                                                               int months) {
        if (initialPrincipal == null || monthlyDeposit == null || annualRate == null) {
            return BigDecimal.ZERO;
        }
        
        // Calculate monthly interest rate
        BigDecimal monthlyRate = annualRate
            .divide(BigDecimal.valueOf(MONTHS_IN_YEAR), 10, ROUNDING_MODE);
        
        // Future value of initial principal with compound interest
        double years = months / 12.0;
        BigDecimal principalFV = calculateMonthlyCompoundInterest(
            initialPrincipal, annualRate, months);
        
        // Future value of monthly deposits (annuity formula)
        // FV = PMT × [((1 + r)^n - 1) / r]
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        double power = Math.pow(onePlusRate.doubleValue(), months);
        BigDecimal numerator = BigDecimal.valueOf(power).subtract(BigDecimal.ONE);
        BigDecimal depositsFV = monthlyDeposit
            .multiply(numerator)
            .divide(monthlyRate, DECIMAL_PLACES, ROUNDING_MODE);
        
        return principalFV.add(depositsFV);
    }
    
    /**
     * Calculate time to reach target amount
     * @param principal Initial principal
     * @param targetAmount Target amount to reach
     * @param annualRate Annual interest rate (as decimal)
     * @param compoundingFrequency Compounding frequency per year
     * @return Number of years to reach target
     */
    public static double calculateTimeToTarget(BigDecimal principal,
                                               BigDecimal targetAmount,
                                               BigDecimal annualRate,
                                               int compoundingFrequency) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0 || 
            targetAmount.compareTo(principal) <= 0 ||
            annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        
        // t = ln(A/P) / (n × ln(1 + r/n))
        double ratio = targetAmount.doubleValue() / principal.doubleValue();
        double rate = annualRate.doubleValue() / compoundingFrequency;
        
        double years = Math.log(ratio) / 
                      (compoundingFrequency * Math.log(1 + rate));
        
        return Math.round(years * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    /**
     * Convert annual rate to monthly rate
     * @param annualRate Annual interest rate (as decimal)
     * @return Monthly interest rate (as decimal)
     */
    public static BigDecimal annualRateToMonthly(BigDecimal annualRate) {
        if (annualRate == null) {
            return BigDecimal.ZERO;
        }
        return annualRate.divide(BigDecimal.valueOf(MONTHS_IN_YEAR), 10, ROUNDING_MODE);
    }
    
    /**
     * Convert annual rate to daily rate
     * @param annualRate Annual interest rate (as decimal)
     * @return Daily interest rate (as decimal)
     */
    public static BigDecimal annualRateToDaily(BigDecimal annualRate) {
        if (annualRate == null) {
            return BigDecimal.ZERO;
        }
        return annualRate.divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, ROUNDING_MODE);
    }
}