package com.bookloop.controller;

import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.exception.UnauthorizedOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler for controller-level exceptions.
 * Maps common exceptions to user-friendly error pages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ListingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ListingNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Listing Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(UnauthorizedOperationException ex, Model model) {
        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorTitle", "Invalid Request");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/400";
    }
}
