package com.wisnu.creditsimulator.model;

import java.util.Locale;

/**
 * Kind of vehicle being financed.
 */
public enum VehicleType {
    MOBIL("Mobil"),
    MOTOR("Motor");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static VehicleType parse(String raw) {
        if (raw != null) {
            String value = raw.trim().toUpperCase(Locale.ROOT);
            for (VehicleType type : values()) {
                if (type.name().equals(value)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Jenis kendaraan harus Mobil atau Motor, bukan '" + raw + "'");
    }
}
