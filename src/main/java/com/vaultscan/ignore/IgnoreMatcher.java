package com.vaultscan.ignore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class IgnoreMatcher {

    private final List<PathMatcher> matchers;

    private IgnoreMatcher(List<PathMatcher> matchers) {
        this.matchers = matchers;
    }

    public static IgnoreMatcher load(Path root) throws IOException {
        List<String> patterns = new ArrayList<>(List.of(
                ".git/**",
                "target/**",
                ".idea/**",
                ".vscode/**"));

        Path ignoreFile = root.resolve(".vaultscanignore");
        if (Files.exists(ignoreFile)) {
            Files.readAllLines(ignoreFile, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .forEach(patterns::add);
        }

        List<PathMatcher> matchers = patterns.stream()
                .map(IgnoreMatcher::toGlobMatcher)
                .toList();
        return new IgnoreMatcher(matchers);
    }

    public boolean isIgnored(Path root, Path file) {
        Path relativePath = root.relativize(file.toAbsolutePath().normalize());
        String normalized = relativePath.toString().replace('\\', '/');
        Path normalizedPath = Path.of(normalized);
        return matchers.stream().anyMatch(matcher -> matcher.matches(normalizedPath));
    }

    private static PathMatcher toGlobMatcher(String pattern) {
        String normalized = pattern.replace('\\', '/');
        if (normalized.endsWith("/")) {
            normalized = normalized + "**";
        }
        if (!normalized.contains("*") && !normalized.contains("?")) {
            normalized = normalized + "/**";
        }
        return FileSystems.getDefault().getPathMatcher("glob:" + normalized);
    }
}
