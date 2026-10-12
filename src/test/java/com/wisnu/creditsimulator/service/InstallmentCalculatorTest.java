package com.wisnu.creditsimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.LoanSimulation;
import com.wisnu.creditsimulator.model.VehicleFactory;
import com.wisnu.creditsimulator.model.YearlyInstallment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstallmentCalculatorTest {

    private final InstallmentCalculator calculator = new InstallmentCalculator(new YearOnYearInterestPolicy());

    @Test
    void matchesReferenceSpreadsheet() {
        // docs/Rumus.xlsx: price 100,000,000, DP 25%, tenure 3 years, Mobil
        LoanApplication application = new LoanApplication(
                VehicleFactory.create("Mobil", "Bekas", 2020),
                new BigDecimal("100000000"), 3, new BigDecimal("25000000"));

        List<YearlyInstallment> installments = calculator.calculate(application).installments();

        assertEquals(3, installments.size());
        assertMoney("2250000.00", installments.get(0).monthlyInstallment());
        assertMoney("2432250.00", installments.get(1).monthlyInstallment());
        assertMoney("2641423.50", installments.get(2).monthlyInstallment());
        assertMoney("81000000.00", installments.get(0).totalWithInterest());
        assertMoney("58374000.00", installments.get(1).totalWithInterest());
        assertMoney("31697082.00", installments.get(2).totalWithInterest());
    }

    @Test
    void singleYearTenureChargesBaseRateOnce() {
        LoanApplication application = new LoanApplication(
                VehicleFactory.create("Motor", "Baru", 2026),
                new BigDecimal("20000000"), 1, new BigDecimal("8000000"));

        LoanSimulation simulation = calculator.calculate(application);

        // 12,000,000 * 1.09 / 12
        assertEquals(1, simulation.installments().size());
        assertMoney("1090000.00", simulation.installments().get(0).monthlyInstallment());
    }

    @Test
    void loanIsFullyRepaidAtEndOfTenure() {
        LoanApplication application = new LoanApplication(
                VehicleFactory.create("Mobil", "Baru", 2025),
                new BigDecimal("1000000000"), 6, new BigDecimal("500000000"));

        List<YearlyInstallment> installments = calculator.calculate(application).installments();

        assertEquals(6, installments.size());
        YearlyInstallment last = installments.get(5);
        assertMoney("0.00", last.totalWithInterest().subtract(last.yearlyInstallment()));
        assertEquals(0, new BigDecimal("0.093").compareTo(last.interestRate()));
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(new BigDecimal(expected), actual.setScale(2, RoundingMode.HALF_UP));
    }
}
