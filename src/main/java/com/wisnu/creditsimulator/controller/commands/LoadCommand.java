package com.wisnu.creditsimulator.controller.commands;

import com.wisnu.creditsimulator.client.LoanApplicationClient;
import com.wisnu.creditsimulator.controller.Command;
import com.wisnu.creditsimulator.controller.SimulatorSession;
import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.LoanSimulation;
import com.wisnu.creditsimulator.service.LoanSimulationService;
import com.wisnu.creditsimulator.service.ValidationException;
import com.wisnu.creditsimulator.view.ConsoleView;
import com.wisnu.creditsimulator.view.SimulationFormatter;
import java.util.List;

/**
 * {@code load}: fetches an existing calculation from the web service and displays the result.
 */
public class LoadCommand implements Command {

    private final ConsoleView view;
    private final LoanApplicationClient client;
    private final LoanSimulationService service;
    private final SimulationFormatter formatter;
    private final SimulatorSession session;

    public LoadCommand(ConsoleView view, LoanApplicationClient client, LoanSimulationService service,
                       SimulationFormatter formatter, SimulatorSession session) {
        this.view = view;
        this.client = client;
        this.service = service;
        this.formatter = formatter;
        this.session = session;
    }

    @Override
    public String name() {
        return "load";
    }

    @Override
    public List<Usage> usages() {
        return List.of(new Usage("load", "Muat perhitungan dari web service dan tampilkan hasilnya"));
    }

    @Override
    public void execute(List<String> args) {
        view.println("Memuat data dari web service...");
        LoanApplication application = client.fetch();

        LoanSimulation simulation;
        try {
            simulation = service.simulate(application);
        } catch (ValidationException e) {
            throw new ValidationException("Data dari web service tidak memenuhi aturan: " + e.getMessage());
        }
        session.setCurrentSimulation(simulation);

        view.println();
        view.printLines(formatter.format(simulation));
    }
}
