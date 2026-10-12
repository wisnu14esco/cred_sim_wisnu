package com.wisnu.creditsimulator.service;

/**
 * Raised when user-supplied loan data breaks one of the business rules.
 */
public class ValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
