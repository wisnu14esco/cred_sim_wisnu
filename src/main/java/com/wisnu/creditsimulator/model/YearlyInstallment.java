package com.wisnu.creditsimulator.model;

import java.math.BigDecimal;

/**
 * Installment for one year of the loan tenure.
 *
 * @param year               1-based year of the tenure
 * @param interestRate       interest rate applied in this year, e.g. 0.081 for 8.1%
 * @param totalWithInterest  outstanding principal at the start of the year plus this year's interest
 * @param monthlyInstallment installment to pay every month during this year
 * @param yearlyInstallment  total paid during this year (monthly installment x 12)
 */
public record YearlyInstallment(
        int year,
        BigDecimal interestRate,
        BigDecimal totalWithInterest,
        BigDecimal monthlyInstallment,
        BigDecimal yearlyInstallment) {
}
