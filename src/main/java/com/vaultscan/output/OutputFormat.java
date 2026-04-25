package com.vaultscan.output;

import java.util.Locale;

public enum OutputFormat {
    TEXT,
    JSON,
    SARIF,
    JUNIT;

    public static OutputFormat from(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "text" -> TEXT;
            case "json" -> JSON;
            case "sarif" -> SARIF;
            case "junit" -> JUNIT;
            default -> throw new IllegalArgumentException("Unknown format: " + value);
        };
    }
}
