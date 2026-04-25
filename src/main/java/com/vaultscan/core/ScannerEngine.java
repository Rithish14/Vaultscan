package com.vaultscan.core;

import com.vaultscan.cli.CliOptions;
import com.vaultscan.ignore.IgnoreMatcher;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class ScannerEngine {

    private static final int MAX_FILE_BYTES = 1_000_000;
    private static final String HIGH_ENTROPY_CHARS = "[A-Za-z0-9+/=_-]{32,}";

    private final List<Rule> rules;

    public ScannerEngine() {
        this(RuleSet.defaultRules());
    }

    ScannerEngine(List<Rule> rules) {
        this.rules = rules;
    }

    public ScanResult scan(CliOptions options) throws IOException {
        Path root = options.getScanPath().toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            throw new IOException("Scan path does not exist: " + root);
        }

        IgnoreMatcher ignoreMatcher = IgnoreMatcher.load(root);
        Set<String> baseline = readBaseline(options.getBaselinePath());
        List<Finding> findings = new ArrayList<>();
        int[] filesScanned = {0};

        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> !ignoreMatcher.isIgnored(root, path))
                    .filter(this::isSafeToRead)
                    .forEach(path -> {
                        try {
                            filesScanned[0]++;
                            scanFile(root, path, findings, baseline);
                        } catch (IOException ignored) {
                            // Unreadable files should not stop CI; scanner config errors still do.
                        }
                    });
        }

        return new ScanResult(root, findings, filesScanned[0]);
    }

    private void scanFile(Path root, Path file, List<Finding> findings, Set<String> baseline) throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (CharacterCodingException exception) {
            return;
        }

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            int lineNumber = index + 1;
            boolean ruleMatched = false;
            for (Rule rule : rules) {
                Matcher matcher = rule.pattern().matcher(line);
                while (matcher.find()) {
                    ruleMatched = true;
                    addFinding(Finding.of(rule, root, file, lineNumber, redact(matcher.group())), findings, baseline);
                }
            }
            if (!ruleMatched) {
                detectHighEntropy(root, file, lineNumber, line, findings, baseline);
            }
        }
    }

    private void detectHighEntropy(
            Path root,
            Path file,
            int lineNumber,
            String line,
            List<Finding> findings,
            Set<String> baseline) {
        Matcher matcher = java.util.regex.Pattern.compile(HIGH_ENTROPY_CHARS).matcher(line);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (candidate.length() >= 40 && shannonEntropy(candidate) >= 4.2) {
                Rule entropyRule = new Rule(
                        "secret.high_entropy",
                        "High entropy token",
                        Severity.MEDIUM,
                        java.util.regex.Pattern.compile(HIGH_ENTROPY_CHARS),
                        "Review the value; if it is a secret, rotate it and move it to managed secret storage.");
                addFinding(Finding.of(entropyRule, root, file, lineNumber, redact(candidate)), findings, baseline);
            }
        }
    }

    private void addFinding(Finding finding, List<Finding> findings, Set<String> baseline) {
        if (!baseline.contains(finding.fingerprint())) {
            findings.add(finding);
        }
    }

    private boolean isSafeToRead(Path file) {
        try {
            return Files.size(file) <= MAX_FILE_BYTES;
        } catch (IOException exception) {
            return false;
        }
    }

    private Set<String> readBaseline(Path baselinePath) throws IOException {
        if (baselinePath == null || !Files.exists(baselinePath)) {
            return Set.of();
        }
        Set<String> fingerprints = new HashSet<>();
        for (String line : Files.readAllLines(baselinePath, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                fingerprints.add(trimmed);
            }
        }
        return fingerprints;
    }

    private static String redact(String evidence) {
        String compact = evidence.strip();
        if (compact.length() <= 12) {
            return "***";
        }
        return compact.substring(0, Math.min(6, compact.length()))
                + "***"
                + compact.substring(Math.max(compact.length() - 4, 6));
    }

    private static double shannonEntropy(String value) {
        int[] counts = new int[256];
        for (char character : value.toCharArray()) {
            if (character < 256) {
                counts[character]++;
            }
        }
        double entropy = 0.0;
        for (int count : counts) {
            if (count > 0) {
                double probability = (double) count / value.length();
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }
        return entropy;
    }
}
