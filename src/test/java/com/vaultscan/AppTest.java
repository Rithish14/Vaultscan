package com.vaultscan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @TempDir
    Path tempDir;

    @Test
    void returnsOneWhenFindingMeetsThreshold() throws Exception {
        Files.writeString(
                tempDir.resolve("secret.txt"),
                "token=ghp_16charactersfakeGitHubtokenforTesting123\n",
                StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int exitCode = new App().run(
                new String[]{"scan", tempDir.toString(), "--fail-on", "high"},
                new PrintStream(out),
                System.err);

        assertEquals(1, exitCode);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("GitHub token"));
    }

    @Test
    void supportsJsonOutput() throws Exception {
        Files.writeString(
                tempDir.resolve("secret.txt"),
                "token=ghp_16charactersfakeGitHubtokenforTesting123\n",
                StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int exitCode = new App().run(
                new String[]{"scan", tempDir.toString(), "--format", "json", "--fail-on", "none"},
                new PrintStream(out),
                System.err);

        assertEquals(0, exitCode);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("\"ruleId\": \"secret.github.token\""));
    }
}
