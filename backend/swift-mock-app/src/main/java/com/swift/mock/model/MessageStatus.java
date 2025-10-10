package com.swift.mock.model;

/**
 * SWIFT Message Status enumeration
 */
public enum MessageStatus {
    PENDING("Awaiting transmission"),
    SENT("Transmitted to SWIFT network"),
    CONFIRMED("Confirmation received"),
    FAILED("Transmission failed"),
    RECONCILED("Matched with TRMS transaction"),
    UNRECONCILED("Mismatch detected");

    private final String description;

    MessageStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
