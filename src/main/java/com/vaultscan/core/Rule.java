package com.vaultscan.core;

import java.util.regex.Pattern;

public record Rule(
        String id,
        String title,
        Severity severity,
        Pattern pattern,
        String remediation) {
}
