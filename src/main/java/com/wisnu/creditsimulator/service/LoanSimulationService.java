package com.wisnu.creditsimulator.service;

import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.LoanSimulation;
import java.util.Objects;

/**
 * Entry point for simulating a loan.
 */
public class LoanSimulationService {

    private final LoanValidator validator;
    private final InstallmentCalculator calculator;

    public LoanSimulationService(LoanValidator validator, InstallmentCalculator calculator) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.calculator = Objects.requireNonNull(calculator, "calculator");
    }

    public LoanValidator validator() {
        return validator;
    }

    /** Validates the application against the business rules, then calculates it. */
    public LoanSimulation simulate(LoanApplication application) {
        validator.validate(application);
        return calculator.calculate(application);
    }

    /**
     * Calculates an application that was already validated when it was first entered.
     */
    public LoanSimulation recalculate(LoanApplication application) {
        return calculator.calculate(application);
    }
}
