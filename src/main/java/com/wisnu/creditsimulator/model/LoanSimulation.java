package com.wisnu.creditsimulator.model;

import java.util.List;
import java.util.Objects;

public record LoanSimulation(LoanApplication application, List<YearlyInstallment> installments) {

    public LoanSimulation {
        Objects.requireNonNull(application, "application");
        installments = List.copyOf(installments);
    }
}
