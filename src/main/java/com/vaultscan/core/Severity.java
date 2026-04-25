package com.vaultscan.core;

import java.util.Locale;

public enum Severity {
    NONE(99),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int rank;

    Severity(int rank) {
        this.rank = rank;
    }

    public boolean isAtLeast(Severity threshold) {
        return threshold != NONE && rank >= threshold.rank;
    }

    public static Severity fromThreshold(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "none" -> NONE;
            case "low" -> LOW;
            case "medium" -> MEDIUM;
            case "high" -> HIGH;
            case "critical" -> CRITICAL;
            default -> throw new IllegalArgumentException("Unknown severity threshold: " + value);
        };
    }
}
