package com.vaultscan;

import com.vaultscan.cli.CliOptions;
import com.vaultscan.core.ScanResult;
import com.vaultscan.core.ScannerEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScannerEngineTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsSecretAndIacFindings() throws Exception {
        Files.writeString(
                tempDir.resolve("app.properties"),
                "github.token=ghp_16charactersfakeGitHubtokenforTesting123\n",
                StandardCharsets.UTF_8);
        Files.writeString(
                tempDir.resolve("main.tf"),
                "cidr_blocks = [\"0.0.0.0/0\"]\n",
                StandardCharsets.UTF_8);

        ScanResult result = new ScannerEngine().scan(CliOptions.parse(new String[]{"scan", tempDir.toString()}));

        assertEquals(2, result.findings().size());
        assertTrue(result.findings().stream().anyMatch(finding -> finding.ruleId().equals("secret.github.token")));
        assertTrue(result.findings().stream().anyMatch(finding -> finding.ruleId().equals("iac.terraform.public_cidr")));
    }

    @Test
    void honorsVaultscanIgnore() throws Exception {
        Files.createDirectories(tempDir.resolve("ignored"));
        Files.writeString(tempDir.resolve(".vaultscanignore"), "ignored/**\n", StandardCharsets.UTF_8);
        Files.writeString(
                tempDir.resolve("ignored").resolve("secret.txt"),
                "token=ghp_16charactersfakeGitHubtokenforTesting123\n",
                StandardCharsets.UTF_8);

        ScanResult result = new ScannerEngine().scan(CliOptions.parse(new String[]{"scan", tempDir.toString()}));

        assertEquals(0, result.findings().size());
    }

    @Test
    void honorsBaselineFingerprints() throws Exception {
        Path secretFile = tempDir.resolve("secret.txt");
        Files.writeString(secretFile, "token=ghp_16charactersfakeGitHubtokenforTesting123\n", StandardCharsets.UTF_8);
        ScanResult firstResult = new ScannerEngine().scan(CliOptions.parse(new String[]{"scan", tempDir.toString()}));

        Path baseline = tempDir.resolve("vaultscan-baseline.txt");
        Files.writeString(baseline, firstResult.findings().get(0).fingerprint() + "\n", StandardCharsets.UTF_8);

        ScanResult secondResult = new ScannerEngine().scan(CliOptions.parse(new String[]{
                "scan", tempDir.toString(), "--baseline", baseline.toString()
        }));

        assertEquals(0, secondResult.findings().size());
    }
}
