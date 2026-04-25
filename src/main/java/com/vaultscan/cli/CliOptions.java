package com.vaultscan.cli;

import com.vaultscan.core.Severity;
import com.vaultscan.output.OutputFormat;

import java.io.PrintStream;
import java.nio.file.Path;
public class CliOptions {

    private final Path scanPath;
    private final OutputFormat format;
    private final Severity failOn;
    private final Path baselinePath;
    private final Path outputPath;
    private final boolean help;
    private final boolean installHook;

    private CliOptions(
            Path scanPath,
            OutputFormat format,
            Severity failOn,
            Path baselinePath,
            Path outputPath,
            boolean help,
            boolean installHook) {
        this.scanPath = scanPath;
        this.format = format;
        this.failOn = failOn;
        this.baselinePath = baselinePath;
        this.outputPath = outputPath;
        this.help = help;
        this.installHook = installHook;
    }

    public static CliOptions parse(String[] args) {
        if (args.length == 0) {
            return defaults(Path.of("."));
        }

        if ("--help".equals(args[0]) || "-h".equals(args[0])) {
            return new CliOptions(Path.of("."), OutputFormat.TEXT, Severity.LOW, null, null, true, false);
        }

        boolean installHook = false;
        int index = 0;
        if ("scan".equals(args[0])) {
            index = 1;
        } else if ("install-hook".equals(args[0])) {
            installHook = true;
            index = 1;
        } else if (args[0].startsWith("-")) {
            index = 0;
        } else {
            throw new IllegalArgumentException("Unknown command: " + args[0]);
        }

        Path scanPath = Path.of(".");
        OutputFormat format = OutputFormat.TEXT;
        Severity failOn = Severity.HIGH;
        Path baselinePath = null;
        Path outputPath = null;

        while (index < args.length) {
            String arg = args[index];
            switch (arg) {
                case "--format":
                    format = OutputFormat.from(nextValue(args, ++index, arg));
                    break;
                case "--fail-on":
                    failOn = Severity.fromThreshold(nextValue(args, ++index, arg));
                    break;
                case "--baseline":
                    baselinePath = Path.of(nextValue(args, ++index, arg));
                    break;
                case "--output":
                    outputPath = Path.of(nextValue(args, ++index, arg));
                    break;
                case "--help":
                case "-h":
                    return new CliOptions(scanPath, format, failOn, baselinePath, outputPath, true, installHook);
                default:
                    if (arg.startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
                    scanPath = Path.of(arg);
                    break;
            }
            index++;
        }

        return new CliOptions(scanPath, format, failOn, baselinePath, outputPath, false, installHook);
    }

    private static CliOptions defaults(Path scanPath) {
        return new CliOptions(scanPath, OutputFormat.TEXT, Severity.HIGH, null, null, false, false);
    }

    private static String nextValue(String[] args, int index, String optionName) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + optionName);
        }
        return args[index];
    }

    public static void printUsage(PrintStream out) {
        out.println("Vaultscan - DevSecOps secret and IaC scanner");
        out.println();
        out.println("Usage:");
        out.println("  vaultscan scan [path] [--format text|json|sarif|junit] [--fail-on low|medium|high|critical|none]");
        out.println("  vaultscan scan [path] [--baseline vaultscan-baseline.txt] [--output report.json]");
        out.println("  vaultscan install-hook [repo-path]");
        out.println();
        out.println("Exit codes: 0 no blocking findings, 1 findings at threshold, 2 scanner/config error.");
    }

    public Path getScanPath() {
        return scanPath;
    }

    public OutputFormat getFormat() {
        return format;
    }

    public Severity getFailOn() {
        return failOn;
    }

    public Path getBaselinePath() {
        return baselinePath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public boolean isHelp() {
        return help;
    }

    public boolean isInstallHook() {
        return installHook;
    }
}
