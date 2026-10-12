package com.wisnu.creditsimulator.service;

import com.wisnu.creditsimulator.model.Vehicle;
import java.math.BigDecimal;

public interface InterestRatePolicy {

    /**
     * @param vehicle the financed vehicle
     * @param year    1-based year of the tenure
     * @return the rate as a fraction
     */
    BigDecimal rateForYear(Vehicle vehicle, int year);
}
