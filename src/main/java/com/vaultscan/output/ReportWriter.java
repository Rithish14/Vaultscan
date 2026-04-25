package com.vaultscan.output;

import com.vaultscan.cli.CliOptions;
import com.vaultscan.core.Finding;
import com.vaultscan.core.ScanResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

public final class ReportWriter {

    private ReportWriter() {
    }

    public static void write(ScanResult result, CliOptions options, PrintStream out) throws IOException {
        String report = switch (options.getFormat()) {
            case TEXT -> text(result);
            case JSON -> json(result);
            case SARIF -> sarif(result);
            case JUNIT -> junit(result);
        };

        if (options.getOutputPath() != null) {
            Files.writeString(options.getOutputPath(), report, StandardCharsets.UTF_8);
            out.println("Wrote " + options.getFormat().name().toLowerCase() + " report to " + options.getOutputPath());
            return;
        }

        out.print(report);
    }

    private static String text(ScanResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("Vaultscan scanned ")
                .append(result.filesScanned())
                .append(" files and found ")
                .append(result.findings().size())
                .append(" findings.")
                .append(System.lineSeparator());

        sortedFindings(result).forEach(finding -> builder
                .append(System.lineSeparator())
                .append("[")
                .append(finding.severity())
                .append("] ")
                .append(finding.title())
                .append(" (")
                .append(finding.ruleId())
                .append(")")
                .append(System.lineSeparator())
                .append("  Location: ")
                .append(finding.file())
                .append(":")
                .append(finding.line())
                .append(System.lineSeparator())
                .append("  Evidence: ")
                .append(finding.evidence())
                .append(System.lineSeparator())
                .append("  Fix: ")
                .append(finding.remediation())
                .append(System.lineSeparator())
                .append("  Fingerprint: ")
                .append(finding.fingerprint())
                .append(System.lineSeparator()));

        return builder.toString();
    }

    private static String json(ScanResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"tool\": \"vaultscan\",\n");
        builder.append("  \"filesScanned\": ").append(result.filesScanned()).append(",\n");
        builder.append("  \"findingCount\": ").append(result.findings().size()).append(",\n");
        builder.append("  \"findings\": [\n");
        List<Finding> findings = sortedFindings(result);
        for (int index = 0; index < findings.size(); index++) {
            Finding finding = findings.get(index);
            builder.append("    {\n");
            builder.append("      \"ruleId\": \"").append(escape(finding.ruleId())).append("\",\n");
            builder.append("      \"title\": \"").append(escape(finding.title())).append("\",\n");
            builder.append("      \"severity\": \"").append(finding.severity()).append("\",\n");
            builder.append("      \"file\": \"").append(escape(finding.file())).append("\",\n");
            builder.append("      \"line\": ").append(finding.line()).append(",\n");
            builder.append("      \"evidence\": \"").append(escape(finding.evidence())).append("\",\n");
            builder.append("      \"remediation\": \"").append(escape(finding.remediation())).append("\",\n");
            builder.append("      \"fingerprint\": \"").append(finding.fingerprint()).append("\"\n");
            builder.append("    }");
            if (index < findings.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("  ]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private static String sarif(ScanResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"version\": \"2.1.0\",\n");
        builder.append("  \"$schema\": \"https://json.schemastore.org/sarif-2.1.0.json\",\n");
        builder.append("  \"runs\": [\n");
        builder.append("    {\n");
        builder.append("      \"tool\": { \"driver\": { \"name\": \"Vaultscan\", \"informationUri\": \"https://github.com/Rithish14/Vaultscan\" } },\n");
        builder.append("      \"results\": [\n");
        List<Finding> findings = sortedFindings(result);
        for (int index = 0; index < findings.size(); index++) {
            Finding finding = findings.get(index);
            builder.append("        {\n");
            builder.append("          \"ruleId\": \"").append(escape(finding.ruleId())).append("\",\n");
            builder.append("          \"level\": \"").append(sarifLevel(finding)).append("\",\n");
            builder.append("          \"message\": { \"text\": \"").append(escape(finding.title() + ": " + finding.remediation())).append("\" },\n");
            builder.append("          \"locations\": [ { \"physicalLocation\": { \"artifactLocation\": { \"uri\": \"")
                    .append(escape(finding.file()))
                    .append("\" }, \"region\": { \"startLine\": ")
                    .append(finding.line())
                    .append(" } } } ],\n");
            builder.append("          \"partialFingerprints\": { \"vaultscanFingerprint\": \"")
                    .append(finding.fingerprint())
                    .append("\" }\n");
            builder.append("        }");
            if (index < findings.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("      ]\n");
        builder.append("    }\n");
        builder.append("  ]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private static String junit(ScanResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<testsuite name=\"vaultscan\" tests=\"")
                .append(result.findings().size())
                .append("\" failures=\"")
                .append(result.findings().size())
                .append("\">\n");
        for (Finding finding : sortedFindings(result)) {
            builder.append("  <testcase classname=\"")
                    .append(xml(finding.ruleId()))
                    .append("\" name=\"")
                    .append(xml(finding.file() + ":" + finding.line()))
                    .append("\">\n");
            builder.append("    <failure message=\"")
                    .append(xml(finding.title()))
                    .append("\">")
                    .append(xml(finding.severity() + " " + finding.remediation() + " Evidence: " + finding.evidence()))
                    .append("</failure>\n");
            builder.append("  </testcase>\n");
        }
        builder.append("</testsuite>\n");
        return builder.toString();
    }

    private static List<Finding> sortedFindings(ScanResult result) {
        return result.findings().stream()
                .sorted(Comparator.comparing(Finding::file).thenComparingInt(Finding::line).thenComparing(Finding::ruleId))
                .toList();
    }

    private static String sarifLevel(Finding finding) {
        return switch (finding.severity()) {
            case CRITICAL, HIGH -> "error";
            case MEDIUM -> "warning";
            case LOW, NONE -> "note";
        };
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String xml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
