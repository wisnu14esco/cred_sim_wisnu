package com.wisnu.creditsimulator.controller.commands;

import com.wisnu.creditsimulator.controller.Command;
import com.wisnu.creditsimulator.controller.CommandException;
import com.wisnu.creditsimulator.controller.SimulatorSession;
import com.wisnu.creditsimulator.model.LoanSimulation;
import com.wisnu.creditsimulator.view.ConsoleView;
import com.wisnu.creditsimulator.view.SimulationFormatter;
import java.util.List;

/**
 * view: shows the current calculation again.
 */
public class ViewCommand implements Command {

    private final ConsoleView view;
    private final SimulationFormatter formatter;
    private final SimulatorSession session;

    public ViewCommand(ConsoleView view, SimulationFormatter formatter, SimulatorSession session) {
        this.view = view;
        this.formatter = formatter;
        this.session = session;
    }

    @Override
    public String name() {
        return "view";
    }

    @Override
    public List<Usage> usages() {
        return List.of(new Usage("view", "Tampilkan lagi hasil perhitungan saat ini"));
    }

    @Override
    public void execute(List<String> args) {
        LoanSimulation simulation = session.currentSimulation()
                .orElseThrow(() -> new CommandException("Belum ada perhitungan. Jalankan 'calc' atau 'load' dulu."));
        view.println("Sheet: " + session.activeSheet());
        view.printLines(formatter.format(simulation));
    }
}
