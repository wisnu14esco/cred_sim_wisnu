package com.wisnu.creditsimulator.controller;

import java.util.List;
public interface Command {

    /** Keyword typed by the user. */
    String name();

    /** Alternative keywords. */
    default List<String> aliases() {
        return List.of();
    }

    /** Usage lines displayed by the show menu. */
    List<Usage> usages();

    void execute(List<String> args);

    /**
     * @param syntax      how to type the command.
     * @param description 
     */
    record Usage(String syntax, String description) {
    }
}
