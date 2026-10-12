package com.wisnu.creditsimulator.service;

import com.wisnu.creditsimulator.model.Vehicle;
import java.math.BigDecimal;

/**
 * Interest grows year on year starting from the vehicle's base rate:
 * +0.1% on every year and +0.5% on every second year, alternating.
 */
public class YearOnYearInterestPolicy implements InterestRatePolicy {

    private static final BigDecimal DEFAULT_YEARLY_INCREMENT = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal DEFAULT_BIENNIAL_INCREMENT = new BigDecimal("0.005"); // 0.5%

    private final BigDecimal yearlyIncrement;
    private final BigDecimal biennialIncrement;

    public YearOnYearInterestPolicy() {
        this(DEFAULT_YEARLY_INCREMENT, DEFAULT_BIENNIAL_INCREMENT);
    }

    public YearOnYearInterestPolicy(BigDecimal yearlyIncrement, BigDecimal biennialIncrement) {
        this.yearlyIncrement = yearlyIncrement;
        this.biennialIncrement = biennialIncrement;
    }

    @Override
    public BigDecimal rateForYear(Vehicle vehicle, int year) {
        if (year < 1) {
            throw new IllegalArgumentException("Year must start at 1, got " + year);
        }
        BigDecimal rate = vehicle.baseInterestRate();
        for (int y = 2; y <= year; y++) {
            rate = rate.add(y % 2 == 0 ? yearlyIncrement : biennialIncrement);
        }
        return rate;
    }
}
