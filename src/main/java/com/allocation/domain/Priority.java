package com.allocation.domain;

public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int rank;

    Priority(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
    public static Priority fromString(String value) {
        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value)) {
                return priority;

            }   if (value == null)return LOW;
            try {
                return Priority.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return LOW;
                
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }
}
