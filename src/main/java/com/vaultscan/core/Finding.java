package com.vaultscan.core;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record Finding(
        String ruleId,
        String title,
        Severity severity,
        String file,
        int line,
        String evidence,
        String remediation) {

    public String fingerprint() {
        String source = ruleId + "|" + file + "|" + line + "|" + evidence;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(source.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static Finding of(
            Rule rule,
            Path root,
            Path file,
            int line,
            String evidence) {
        Path relativePath = root.relativize(file.toAbsolutePath().normalize());
        return new Finding(
                rule.id(),
                rule.title(),
                rule.severity(),
                relativePath.toString().replace('\\', '/'),
                line,
                evidence,
                rule.remediation());
    }
}
