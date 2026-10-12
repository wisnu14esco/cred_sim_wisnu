package com.wisnu.creditsimulator.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Console input/output. Works in two modes:
 * interactive: an invalid answer shows the error and asks the same question again
 * file: each line read is echoed so the output reads like a transcript, and an invalid
 * answer aborts the current command (asking again would consume the next line of the file).
 */
public class ConsoleView {

    private static final int BYTE_ORDER_MARK = 0xFEFF;

    private final BufferedReader in;
    private final PrintStream out;
    private final boolean interactive;
    private boolean firstLine = true;

    public ConsoleView(BufferedReader in, PrintStream out, boolean interactive) {
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
        this.interactive = interactive;
    }

    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Reads one line after showing the prompt. Returns null if the end of input is reached.
     */
    public String readLine(String prompt) {
        if (interactive) {
            out.print(prompt);
            out.flush();
            String line = readRaw();
            if (line == null) {
                out.println();
            }
            return line;
        }
        String line;
        do {
            line = readRaw();
        } while (line != null && isBlankOrComment(line));
        if (line != null) {
            out.println(prompt + line);
        }
        return line;
    }

    private String readRaw() {
        String line;
        try {
            line = in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca input", e);
        }
        if (line != null && firstLine && !line.isEmpty() && line.charAt(0) == BYTE_ORDER_MARK) {
            line = line.substring(1);
        }
        firstLine = false;
        return line;
    }

    public <T> T ask(String question, Function<String, T> parser) {
        while (true) {
            String answer = readLine(question + ": ");
            if (answer == null) {
                throw new EndOfInputException();
            }
            try {
                return parser.apply(answer.trim());
            } catch (IllegalArgumentException e) {
                if (!interactive) {
                    throw e;
                }
                error(e.getMessage());
            }
        }
    }

    public void println() {
        out.println();
    }

    public void println(String line) {
        out.println(line);
    }

    public void printLines(List<String> lines) {
        lines.forEach(out::println);
    }

    public void error(String message) {
        out.println("Error: " + message);
    }

    public static boolean isBlankOrComment(String line) {
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }
}
