package com.wisnu.creditsimulator.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @param vehicle         the financed vehicle
 * @param totalLoanAmount the total loan amount
 * @param tenureYears     loan tenure in years
 * @param downPayment     down payment amount
 */
public record LoanApplication(Vehicle vehicle, BigDecimal totalLoanAmount, int tenureYears, BigDecimal downPayment) {

    public LoanApplication {
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(totalLoanAmount, "totalLoanAmount");
        Objects.requireNonNull(downPayment, "downPayment");
    }

    public BigDecimal principal() {
        return totalLoanAmount.subtract(downPayment);
    }
}
