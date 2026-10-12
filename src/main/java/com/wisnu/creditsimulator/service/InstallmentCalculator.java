package com.wisnu.creditsimulator.service;

import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.LoanSimulation;
import com.wisnu.creditsimulator.model.YearlyInstallment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Calculates yearly installments following docs/Rumus.xlsx:
 *
 * principal      = totalLoanAmount - downPayment
 * total(n)       = remaining(n) * (1 + rate(n))          
 * monthly(n)     = total(n) / (12 * (tenure - n + 1))
 * yearly(n)      = monthly(n) * 12
 * remaining(1)   = principal
 * remaining(n+1) = total(n) - yearly(n)
 *
 * Rounding happens only when displayed.
 */
public class InstallmentCalculator {

    private static final int SCALE = 10;
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    private final InterestRatePolicy interestRatePolicy;

    public InstallmentCalculator(InterestRatePolicy interestRatePolicy) {
        this.interestRatePolicy = Objects.requireNonNull(interestRatePolicy, "interestRatePolicy");
    }

    public LoanSimulation calculate(LoanApplication application) {
        int tenure = application.tenureYears();
        List<YearlyInstallment> installments = new ArrayList<>(tenure);
        BigDecimal remaining = application.principal();

        for (int year = 1; year <= tenure; year++) {
            BigDecimal rate = interestRatePolicy.rateForYear(application.vehicle(), year);
            BigDecimal total = remaining.multiply(BigDecimal.ONE.add(rate));
            BigDecimal monthsLeft = MONTHS_PER_YEAR.multiply(BigDecimal.valueOf(tenure - year + 1L));
            BigDecimal monthly = total.divide(monthsLeft, SCALE, RoundingMode.HALF_UP);
            BigDecimal yearly = monthly.multiply(MONTHS_PER_YEAR);

            installments.add(new YearlyInstallment(year, rate, total, monthly, yearly));
            remaining = total.subtract(yearly);
        }
        return new LoanSimulation(application, installments);
    }
}
