package com.wisnu.creditsimulator.service;

import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.VehicleCondition;
import com.wisnu.creditsimulator.model.VehicleType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class LoanValidator {

    public static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000");
    public static final int MIN_TENURE = 1;
    public static final int MAX_TENURE = 6;

    private static final Pattern YEAR = Pattern.compile("\\d{4}");
    private static final Pattern AMOUNT = Pattern.compile("\\d+(\\.\\d{1,2})?");
    private static final Pattern INTEGER = Pattern.compile("\\d{1,9}");

    private final Clock clock;

    public LoanValidator(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public int currentYear() {
        return Year.now(clock).getValue();
    }

    public VehicleType parseVehicleType(String raw) {
        try {
            return VehicleType.parse(raw);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public VehicleCondition parseVehicleCondition(String raw) {
        try {
            return VehicleCondition.parse(raw);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public int parseVehicleYear(String raw, VehicleCondition condition) {
        String value = trim(raw);
        if (!YEAR.matcher(value).matches()) {
            throw new ValidationException("Tahun kendaraan harus angka 4 digit, bukan '" + value + "'");
        }
        int year = Integer.parseInt(value);
        validateVehicleYear(year, condition);
        return year;
    }

    public BigDecimal parseLoanAmount(String raw) {
        BigDecimal amount = parseAmount(raw, "Jumlah pinjaman");
        validateLoanAmount(amount);
        return amount;
    }

    public int parseTenure(String raw) {
        String value = trim(raw);
        if (!INTEGER.matcher(value).matches()) {
            throw new ValidationException("Tenor harus angka " + MIN_TENURE + "-" + MAX_TENURE + ", bukan '" + value + "'");
        }
        int tenure = Integer.parseInt(value);
        validateTenure(tenure);
        return tenure;
    }

    public BigDecimal parseDownPayment(String raw, BigDecimal loanAmount, VehicleCondition condition) {
        BigDecimal downPayment = parseAmount(raw, "Jumlah DP");
        validateDownPayment(downPayment, loanAmount, condition);
        return downPayment;
    }

    /** Validates an application built outside the interactive flow, e.g. loaded from a web service. */
    public void validate(LoanApplication application) {
        VehicleCondition condition = application.vehicle().condition();
        validateVehicleYear(application.vehicle().year(), condition);
        validateLoanAmount(application.totalLoanAmount());
        validateTenure(application.tenureYears());
        validateDownPayment(application.downPayment(), application.totalLoanAmount(), condition);
    }

    public void validateVehicleYear(int year, VehicleCondition condition) {
        int currentYear = currentYear();
        if (year < 1000 || year > currentYear) {
            throw new ValidationException("Tahun kendaraan harus 4 digit dan tidak boleh lebih dari " + currentYear);
        }
        int minimumNewYear = currentYear - 1;
        if (condition == VehicleCondition.BARU && year < minimumNewYear) {
            throw new ValidationException("Kendaraan Baru tidak boleh lebih tua dari tahun " + minimumNewYear);
        }
    }

    public void validateLoanAmount(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new ValidationException("Jumlah pinjaman harus lebih dari 0");
        }
        if (amount.compareTo(MAX_LOAN_AMOUNT) > 0) {
            throw new ValidationException("Jumlah pinjaman tidak boleh lebih dari Rp. " + money(MAX_LOAN_AMOUNT));
        }
    }

    public void validateTenure(int tenure) {
        if (tenure < MIN_TENURE || tenure > MAX_TENURE) {
            throw new ValidationException("Tenor pinjaman harus " + MIN_TENURE + "-" + MAX_TENURE + " tahun");
        }
    }

    public void validateDownPayment(BigDecimal downPayment, BigDecimal loanAmount, VehicleCondition condition) {
        if (downPayment.signum() < 0) {
            throw new ValidationException("Jumlah DP tidak boleh negatif");
        }
        if (downPayment.compareTo(loanAmount) >= 0) {
            throw new ValidationException("Jumlah DP harus lebih kecil dari jumlah pinjaman");
        }
        BigDecimal ratio = condition.minimumDownPaymentRatio();
        BigDecimal minimum = loanAmount.multiply(ratio);
        if (downPayment.compareTo(minimum) < 0) {
            String percent = ratio.movePointRight(2).stripTrailingZeros().toPlainString();
            throw new ValidationException("Jumlah DP kendaraan " + condition.displayName() + " minimal " + percent
                    + "% dari jumlah pinjaman (Rp. " + money(minimum) + ")");
        }
    }

    private static BigDecimal parseAmount(String raw, String field) {
        String value = trim(raw);
        if (!AMOUNT.matcher(value).matches()) {
            throw new ValidationException(field + " harus berupa angka, bukan '" + value + "'");
        }
        return new BigDecimal(value);
    }

    private static String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
