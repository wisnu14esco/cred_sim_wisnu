package com.wisnu.creditsimulator.controller.commands;

import com.wisnu.creditsimulator.controller.Command;
import com.wisnu.creditsimulator.controller.CommandRegistry;
import com.wisnu.creditsimulator.view.ConsoleView;
import java.util.List;

/**
 * show: lists every command in the registry, so new commands appear here automatically.
 */
public class ShowCommand implements Command {

    private final CommandRegistry registry;
    private final ConsoleView view;

    public ShowCommand(CommandRegistry registry, ConsoleView view) {
        this.registry = registry;
        this.view = view;
    }

    @Override
    public String name() {
        return "show";
    }

    @Override
    public List<String> aliases() {
        return List.of("help");
    }

    @Override
    public List<Usage> usages() {
        return List.of(new Usage("show", "Tampilkan semua perintah yang bisa digunakan"));
    }

    @Override
    public void execute(List<String> args) {
        view.println("Perintah yang tersedia:");
        for (Command command : registry.commands()) {
            for (Usage usage : command.usages()) {
                view.println(String.format("  %-22s %s", usage.syntax(), usage.description()));
            }
            if (!command.aliases().isEmpty()) {
                view.println(String.format("  %-22s alias dari '%s'", String.join(", ", command.aliases()),
                        command.name()));
            }
        }
    }
}
