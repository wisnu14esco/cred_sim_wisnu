package com.wisnu.creditsimulator.controller;

import com.wisnu.creditsimulator.model.LoanSimulation;
import java.util.Objects;
import java.util.Optional;

/**
 * State shared by the commands while the application runs.
 */
public class SimulatorSession {

    public static final String DEFAULT_SHEET = "default";

    private LoanSimulation currentSimulation;
    private String activeSheet = DEFAULT_SHEET;
    private boolean running = true;

    public Optional<LoanSimulation> currentSimulation() {
        return Optional.ofNullable(currentSimulation);
    }

    public void setCurrentSimulation(LoanSimulation simulation) {
        this.currentSimulation = Objects.requireNonNull(simulation, "simulation");
    }

    public String activeSheet() {
        return activeSheet;
    }

    public void setActiveSheet(String activeSheet) {
        this.activeSheet = Objects.requireNonNull(activeSheet, "activeSheet");
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }
}
