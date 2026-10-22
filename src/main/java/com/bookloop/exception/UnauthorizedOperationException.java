package com.bookloop.exception;

/**
 * Thrown when a user attempts to modify a listing they do not own.
 */
public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
