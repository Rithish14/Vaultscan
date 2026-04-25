package com.vaultscan;

import com.vaultscan.cli.CliOptions;
import com.vaultscan.cli.PreCommitHookInstaller;
import com.vaultscan.core.Finding;
import com.vaultscan.core.ScanResult;
import com.vaultscan.core.ScannerEngine;
import com.vaultscan.output.ReportWriter;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

public class App {

    public static void main(String[] args) {
        int exitCode = new App().run(args, System.out, System.err);
        System.exit(exitCode);
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (IllegalArgumentException exception) {
            err.println(exception.getMessage());
            CliOptions.printUsage(err);
            return 2;
        }

        if (options.isHelp()) {
            CliOptions.printUsage(out);
            return 0;
        }

        try {
            if (options.isInstallHook()) {
                Path hookPath = PreCommitHookInstaller.install(options.getScanPath());
                out.println("Installed pre-commit hook at " + hookPath);
                return 0;
            }

            ScannerEngine scanner = new ScannerEngine();
            ScanResult result = scanner.scan(options);
            ReportWriter.write(result, options, out);

            List<Finding> blockingFindings = result.findingsAtOrAbove(options.getFailOn());
            return blockingFindings.isEmpty() ? 0 : 1;
        } catch (IOException exception) {
            err.println("Vaultscan error: " + exception.getMessage());
            return 2;
        }
    }
}
