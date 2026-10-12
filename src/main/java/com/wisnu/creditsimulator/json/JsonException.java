package com.wisnu.creditsimulator.json;

/**
 * Raised for malformed JSON or JSON that does not have the expected shape.
 */
public class JsonException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JsonException(String message) {
        super(message);
    }
}
