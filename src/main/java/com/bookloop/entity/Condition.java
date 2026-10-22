package com.bookloop.entity;

/**
 * The physical condition of a used textbook listing.
 */
public enum Condition {
    NEW("New"),
    LIKE_NEW("Like New"),
    GOOD("Good"),
    ACCEPTABLE("Acceptable");

    private final String displayName;

    Condition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
