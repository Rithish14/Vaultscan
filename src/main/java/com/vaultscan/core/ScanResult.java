package com.vaultscan.core;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public record ScanResult(Path root, List<Finding> findings, int filesScanned) {

    public List<Finding> findingsAtOrAbove(Severity threshold) {
        return findings.stream()
                .filter(finding -> finding.severity().isAtLeast(threshold))
                .sorted(Comparator.comparing(Finding::file).thenComparingInt(Finding::line))
                .toList();
    }
}
