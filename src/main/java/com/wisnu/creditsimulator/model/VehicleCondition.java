package com.wisnu.creditsimulator.model;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Condition of the vehicle. Each condition carries its minimum down payment ratio.
 */
public enum VehicleCondition {
    BARU("Baru", new BigDecimal("0.35")), //35%
    BEKAS("Bekas", new BigDecimal("0.25")); //25%

    private final String displayName;
    private final BigDecimal minimumDownPaymentRatio;

    VehicleCondition(String displayName, BigDecimal minimumDownPaymentRatio) {
        this.displayName = displayName;
        this.minimumDownPaymentRatio = minimumDownPaymentRatio;
    }

    public String displayName() {
        return displayName;
    }

    public BigDecimal minimumDownPaymentRatio() {
        return minimumDownPaymentRatio;
    }

    public static VehicleCondition parse(String raw) {
        if (raw != null) {
            String value = raw.trim().toUpperCase(Locale.ROOT);
            for (VehicleCondition condition : values()) {
                if (condition.name().equals(value)) {
                    return condition;
                }
            }
        }
        throw new IllegalArgumentException("Kondisi kendaraan harus Baru atau Bekas, bukan '" + raw + "'");
    }
}
