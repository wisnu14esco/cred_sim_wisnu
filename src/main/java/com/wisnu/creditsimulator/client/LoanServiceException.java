package com.wisnu.creditsimulator.client;

/**
 * Raised when the loan web service is unreachable or returns an unusable response.
 */
public class LoanServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LoanServiceException(String message) {
        super(message);
    }

    public LoanServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
