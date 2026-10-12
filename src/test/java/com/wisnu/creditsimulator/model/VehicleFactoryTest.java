package com.wisnu.creditsimulator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VehicleFactoryTest {

    @Test
    void createsCarWithEightPercentBaseRate() {
        Vehicle vehicle = VehicleFactory.create(VehicleType.MOBIL, VehicleCondition.BARU, 2025);

        assertInstanceOf(Car.class, vehicle);
        assertEquals(VehicleType.MOBIL, vehicle.type());
        assertEquals(0, new BigDecimal("0.08").compareTo(vehicle.baseInterestRate()));
        assertEquals(2025, vehicle.year());
    }

    @Test
    void createsMotorcycleWithNinePercentBaseRate() {
        Vehicle vehicle = VehicleFactory.create(VehicleType.MOTOR, VehicleCondition.BEKAS, 2018);

        assertInstanceOf(Motorcycle.class, vehicle);
        assertEquals(0, new BigDecimal("0.09").compareTo(vehicle.baseInterestRate()));
    }

    @Test
    void minimumDownPaymentDependsOnCondition() {
        assertEquals(0, new BigDecimal("0.35").compareTo(
                VehicleFactory.create("Mobil", "Baru", 2025).minimumDownPaymentRatio()));
        assertEquals(0, new BigDecimal("0.25").compareTo(
                VehicleFactory.create("Mobil", "Bekas", 2015).minimumDownPaymentRatio()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mobil", "MOBIL", "Mobil", " mObIl "})
    void parsesVehicleTypeIgnoringCase(String raw) {
        assertEquals(VehicleType.MOBIL, VehicleType.parse(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"baru", "BARU", "Baru"})
    void parsesConditionIgnoringCase(String raw) {
        assertEquals(VehicleCondition.BARU, VehicleCondition.parse(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "truk", "mobil1", "123"})
    void rejectsUnknownVehicleType(String raw) {
        assertThrows(IllegalArgumentException.class, () -> VehicleType.parse(raw));
    }

    @Test
    void rejectsNullAndUnknownCondition() {
        assertThrows(IllegalArgumentException.class, () -> VehicleCondition.parse(null));
        assertThrows(IllegalArgumentException.class, () -> VehicleCondition.parse("lama"));
    }

    @Test
    void vehiclesWithSameAttributesAreEqual() {
        assertEquals(VehicleFactory.create("motor", "baru", 2026), VehicleFactory.create("Motor", "Baru", 2026));
    }
}
