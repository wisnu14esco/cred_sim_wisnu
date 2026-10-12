package com.wisnu.creditsimulator.controller;

import com.wisnu.creditsimulator.view.ConsoleView;
import com.wisnu.creditsimulator.view.EndOfInputException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Main loop: reads a command line, dispatches it to the matching command and reports errors.
 */
public class CreditSimulatorController {

    private final ConsoleView view;
    private final CommandRegistry registry;
    private final SimulatorSession session;

    public CreditSimulatorController(ConsoleView view, CommandRegistry registry, SimulatorSession session) {
        this.view = Objects.requireNonNull(view, "view");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.session = Objects.requireNonNull(session, "session");
    }

    /**
     * Runs until exit or EOF.
     *
     * @return process exit code: 1 if any command failed while reading from a file, otherwise 0
     */
    public int run() {
        view.println("==== Credit Simulator ====");
        view.println("Ketik 'show' untuk melihat semua perintah.");
        int failures = 0;

        while (session.isRunning()) {
            view.println();
            String line = view.readLine("[" + session.activeSheet() + "] > ");
            if (line == null) {
                break;
            }
            if (ConsoleView.isBlankOrComment(line)) {
                continue;
            }
            if (!dispatch(line.trim())) {
                failures++;
            }
        }
        return failures > 0 && !view.isInteractive() ? 1 : 0;
    }

    private boolean dispatch(String line) {
        List<String> words = Arrays.asList(line.split("\\s+"));
        String keyword = words.get(0);
        Optional<Command> command = registry.find(keyword);
        if (command.isEmpty()) {
            view.error("Perintah '" + keyword + "' tidak dikenal. Ketik 'show' untuk melihat semua perintah.");
            return false;
        }
        try {
            command.get().execute(words.subList(1, words.size()));
            return true;
        } catch (EndOfInputException e) {
            view.error(e.getMessage());
            session.stop();
            return false;
        } catch (RuntimeException e) {
            view.error(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return false;
        }
    }
}
