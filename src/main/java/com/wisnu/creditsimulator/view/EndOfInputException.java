package com.wisnu.creditsimulator.view;

/**
 * EOF Exception.
 */
public class EndOfInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EndOfInputException() {
        super("Input berakhir sebelum semua data diisi");
    }
}
