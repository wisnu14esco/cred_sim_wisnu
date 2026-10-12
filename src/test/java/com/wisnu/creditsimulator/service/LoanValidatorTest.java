package com.wisnu.creditsimulator.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.VehicleCondition;
import com.wisnu.creditsimulator.model.VehicleFactory;
import com.wisnu.creditsimulator.model.VehicleType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LoanValidatorTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
    private static final BigDecimal HUNDRED_MILLION = new BigDecimal("100000000");

    private final LoanValidator validator = new LoanValidator(CLOCK);

    @Test
    void parsesVehicleTypeAndConditionIgnoringCase() {
        assertEquals(VehicleType.MOTOR, validator.parseVehicleType("mOtOr"));
        assertEquals(VehicleCondition.BEKAS, validator.parseVehicleCondition("BEKAS"));
    }

    @Test
    void rejectsUnknownTypeAndConditionWithValidationException() {
        assertThrows(ValidationException.class, () -> validator.parseVehicleType("Sepeda"));
        assertThrows(ValidationException.class, () -> validator.parseVehicleCondition("1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25", "20255", "abcd", "", "20.5"})
    void yearMustBeFourDigits(String raw) {
        assertThrows(ValidationException.class, () -> validator.parseVehicleYear(raw, VehicleCondition.BEKAS));
    }

    @Test
    void newVehicleYearCannotBeOlderThanLastYear() {
        assertEquals(2025, validator.parseVehicleYear("2025", VehicleCondition.BARU));
        assertEquals(2026, validator.parseVehicleYear("2026", VehicleCondition.BARU));
        ValidationException error = assertThrows(ValidationException.class,
                () -> validator.parseVehicleYear("2024", VehicleCondition.BARU));
        assertTrue(error.getMessage().contains("2025"));
    }

    @Test
    void usedVehicleMayBeOlder() {
        assertEquals(2010, validator.parseVehicleYear("2010", VehicleCondition.BEKAS));
    }

    @Test
    void yearCannotBeInTheFuture() {
        assertThrows(ValidationException.class, () -> validator.parseVehicleYear("2027", VehicleCondition.BEKAS));
    }

    @Test
    void loanAmountUpToOneBillion() {
        assertEquals(0, new BigDecimal("1000000000").compareTo(validator.parseLoanAmount("1000000000")));
        assertThrows(ValidationException.class, () -> validator.parseLoanAmount("1000000001"));
        assertThrows(ValidationException.class, () -> validator.parseLoanAmount("0"));
        assertThrows(ValidationException.class, () -> validator.parseLoanAmount("-5"));
        assertThrows(ValidationException.class, () -> validator.parseLoanAmount("seratus"));
    }

    @Test
    void tenureBetweenOneAndSixYears() {
        assertEquals(1, validator.parseTenure("1"));
        assertEquals(6, validator.parseTenure(" 6 "));
        assertThrows(ValidationException.class, () -> validator.parseTenure("0"));
        assertThrows(ValidationException.class, () -> validator.parseTenure("7"));
        assertThrows(ValidationException.class, () -> validator.parseTenure("2.5"));
    }

    @Test
    void newVehicleNeedsAtLeastThirtyFivePercentDownPayment() {
        assertDoesNotThrow(() -> validator.parseDownPayment("35000000", HUNDRED_MILLION, VehicleCondition.BARU));
        ValidationException error = assertThrows(ValidationException.class,
                () -> validator.parseDownPayment("34999999", HUNDRED_MILLION, VehicleCondition.BARU));
        assertTrue(error.getMessage().contains("35%"));
    }

    @Test
    void usedVehicleNeedsAtLeastTwentyFivePercentDownPayment() {
        assertDoesNotThrow(() -> validator.parseDownPayment("25000000", HUNDRED_MILLION, VehicleCondition.BEKAS));
        assertThrows(ValidationException.class,
                () -> validator.parseDownPayment("24999999", HUNDRED_MILLION, VehicleCondition.BEKAS));
    }

    @Test
    void downPaymentMustBeLessThanLoanAmount() {
        assertThrows(ValidationException.class,
                () -> validator.parseDownPayment("100000000", HUNDRED_MILLION, VehicleCondition.BEKAS));
    }

    @Test
    void validatesWholeApplication() {
        LoanApplication valid = new LoanApplication(VehicleFactory.create("Mobil", "Baru", 2025),
                new BigDecimal("1000000000"), 6, new BigDecimal("500000000"));
        LoanApplication tooLong = new LoanApplication(VehicleFactory.create("Mobil", "Baru", 2025),
                new BigDecimal("1000000000"), 7, new BigDecimal("500000000"));

        assertDoesNotThrow(() -> validator.validate(valid));
        assertThrows(ValidationException.class, () -> validator.validate(tooLong));
    }
}
