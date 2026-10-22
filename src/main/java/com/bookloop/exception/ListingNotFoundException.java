package com.bookloop.exception;

/**
 * Thrown when a listing with the given ID cannot be found.
 */
public class ListingNotFoundException extends RuntimeException {
    public ListingNotFoundException(String message) {
        super(message);
    }
}
