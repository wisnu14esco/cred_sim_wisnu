package com.wisnu.creditsimulator.model;

import java.util.Objects;

/**
 * Factory that creates the concrete {@link Vehicle} for a vehicle type.
 */
public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static Vehicle create(VehicleType type, VehicleCondition condition, int year) {
        Objects.requireNonNull(type, "type");
        switch (type) {
            case MOBIL:
                return new Car(condition, year);
            case MOTOR:
                return new Motorcycle(condition, year);
            default:
                throw new IllegalArgumentException("Unsupported vehicle type: " + type);
        }
    }

    public static Vehicle create(String type, String condition, int year) {
        return create(VehicleType.parse(type), VehicleCondition.parse(condition), year);
    }
}
