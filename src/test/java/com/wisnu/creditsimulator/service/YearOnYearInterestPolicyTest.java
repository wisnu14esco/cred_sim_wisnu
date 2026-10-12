package com.wisnu.creditsimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wisnu.creditsimulator.model.Vehicle;
import com.wisnu.creditsimulator.model.VehicleFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class YearOnYearInterestPolicyTest {

    private final InterestRatePolicy policy = new YearOnYearInterestPolicy();

    @ParameterizedTest(name = "Mobil year {0} -> {1}")
    @CsvSource({"1, 0.08", "2, 0.081", "3, 0.086", "4, 0.087", "5, 0.092", "6, 0.093"})
    void carRatesFollowYearOnYearIncrements(int year, String expected) {
        Vehicle car = VehicleFactory.create("Mobil", "Baru", 2026);

        assertEquals(0, new BigDecimal(expected).compareTo(policy.rateForYear(car, year)));
    }

    @ParameterizedTest(name = "Motor year {0} -> {1}")
    @CsvSource({"1, 0.09", "2, 0.091", "3, 0.096", "6, 0.103"})
    void motorcycleRatesStartFromNinePercent(int year, String expected) {
        Vehicle motorcycle = VehicleFactory.create("Motor", "Bekas", 2020);

        assertEquals(0, new BigDecimal(expected).compareTo(policy.rateForYear(motorcycle, year)));
    }

    @Test
    void rejectsYearBeforeFirst() {
        Vehicle car = VehicleFactory.create("Mobil", "Baru", 2026);

        assertThrows(IllegalArgumentException.class, () -> policy.rateForYear(car, 0));
    }
}
