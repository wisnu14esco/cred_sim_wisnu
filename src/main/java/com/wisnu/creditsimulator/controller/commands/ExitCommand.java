package com.wisnu.creditsimulator.controller.commands;

import com.wisnu.creditsimulator.controller.Command;
import com.wisnu.creditsimulator.controller.SimulatorSession;
import com.wisnu.creditsimulator.view.ConsoleView;
import java.util.List;

/**
 * exit: stops the app.
 */
public class ExitCommand implements Command {

    private final ConsoleView view;
    private final SimulatorSession session;

    public ExitCommand(ConsoleView view, SimulatorSession session) {
        this.view = view;
        this.session = session;
    }

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public List<String> aliases() {
        return List.of("quit");
    }

    @Override
    public List<Usage> usages() {
        return List.of(new Usage("exit", "Keluar dari aplikasi"));
    }

    @Override
    public void execute(List<String> args) {
        view.println("Terima kasih!");
        session.stop();
    }
}
