package com.bookloop.entity;

/**
 * The listing status of a Product.
 */
public enum ListingStatus {
    AVAILABLE("Available"),
    SOLD("Sold"),
    HIDDEN("Hidden");

    private final String displayName;

    ListingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
