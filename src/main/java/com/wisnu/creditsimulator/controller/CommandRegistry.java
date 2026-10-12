package com.wisnu.creditsimulator.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Commands available in the console, in registration order, looked up by name or alias.
 */
public class CommandRegistry {

    private final List<Command> commands = new ArrayList<>();
    private final Map<String, Command> byKeyword = new HashMap<>();

    public CommandRegistry register(Command command) {
        List<String> keywords = new ArrayList<>();
        keywords.add(command.name());
        keywords.addAll(command.aliases());
        for (String keyword : keywords) {
            String key = keyword.toLowerCase(Locale.ROOT);
            if (byKeyword.containsKey(key)) {
                throw new IllegalStateException("Duplicate command keyword: " + keyword);
            }
            byKeyword.put(key, command);
        }
        commands.add(command);
        return this;
    }

    public Optional<Command> find(String keyword) {
        return Optional.ofNullable(byKeyword.get(keyword.toLowerCase(Locale.ROOT)));
    }

    public List<Command> commands() {
        return List.copyOf(commands);
    }
}
