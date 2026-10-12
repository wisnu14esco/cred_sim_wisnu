package com.wisnu.creditsimulator.json;

import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.Vehicle;
import com.wisnu.creditsimulator.model.VehicleFactory;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps a LoanApplication to and from the JSON shape used by the web service:
 *
 * { "vehicleType": "Mobil", "vehicleCondition": "Baru", "vehicleYear": 2025,
 *   "totalLoanAmount": 1000000000, "loanTenure": 6, "downPayment": 500000000 } 
 */
public final class LoanApplicationJson {

    public static final String VEHICLE_TYPE = "vehicleType";
    public static final String VEHICLE_CONDITION = "vehicleCondition";
    public static final String VEHICLE_YEAR = "vehicleYear";
    public static final String TOTAL_LOAN_AMOUNT = "totalLoanAmount";
    public static final String LOAN_TENURE = "loanTenure";
    public static final String DOWN_PAYMENT = "downPayment";

    private LoanApplicationJson() {
    }

    public static LoanApplication fromJson(String json) {
        return fromMap(JsonParser.parseObject(json));
    }

    public static LoanApplication fromMap(Map<?, ?> json) {
        Vehicle vehicle = VehicleFactory.create(
                requireString(json, VEHICLE_TYPE),
                requireString(json, VEHICLE_CONDITION),
                requireInt(json, VEHICLE_YEAR));
        return new LoanApplication(
                vehicle,
                requireNumber(json, TOTAL_LOAN_AMOUNT),
                requireInt(json, LOAN_TENURE),
                requireNumber(json, DOWN_PAYMENT));
    }

    public static Map<String, Object> toMap(LoanApplication application) {
        Vehicle vehicle = application.vehicle();
        Map<String, Object> json = new LinkedHashMap<>();
        json.put(VEHICLE_TYPE, vehicle.type().displayName());
        json.put(VEHICLE_CONDITION, vehicle.condition().displayName());
        json.put(VEHICLE_YEAR, vehicle.year());
        json.put(TOTAL_LOAN_AMOUNT, application.totalLoanAmount());
        json.put(LOAN_TENURE, application.tenureYears());
        json.put(DOWN_PAYMENT, application.downPayment());
        return json;
    }

    private static Object require(Map<?, ?> json, String field) {
        Object value = json.get(field);
        if (value == null) {
            throw new JsonException("Field '" + field + "' wajib diisi");
        }
        return value;
    }

    private static String requireString(Map<?, ?> json, String field) {
        Object value = require(json, field);
        if (!(value instanceof String)) {
            throw new JsonException("Field '" + field + "' harus berupa teks");
        }
        return (String) value;
    }

    private static BigDecimal requireNumber(Map<?, ?> json, String field) {
        Object value = require(json, field);
        if (!(value instanceof BigDecimal)) {
            throw new JsonException("Field '" + field + "' harus berupa angka");
        }
        return (BigDecimal) value;
    }

    private static int requireInt(Map<?, ?> json, String field) {
        try {
            return requireNumber(json, field).intValueExact();
        } catch (ArithmeticException e) {
            throw new JsonException("Field '" + field + "' harus berupa bilangan bulat");
        }
    }
}
