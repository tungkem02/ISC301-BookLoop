package com.bookloop.exception;

/**
 * Thrown when a registration is attempted with an email that already exists.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
