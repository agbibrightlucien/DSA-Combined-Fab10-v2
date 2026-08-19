package com.allocation.domain;

public enum Status {
    AVAILABLE,
    UNAVAILABLE,
    BUSY,
    PENDING,
    ASSIGNED,
    QUEUED,
    IN_PROGRESS;

    public static Status fromString(String value) {
        if (value == null) return UNAVAILABLE;
        try {
            return Status.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }

    }
}
