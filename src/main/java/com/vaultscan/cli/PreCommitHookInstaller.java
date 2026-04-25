package com.vaultscan.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class PreCommitHookInstaller {

    private PreCommitHookInstaller() {
    }

    public static Path install(Path repoPath) throws IOException {
        Path root = repoPath.toAbsolutePath().normalize();
        Path hooksDirectory = root.resolve(".git").resolve("hooks");
        if (!Files.isDirectory(hooksDirectory)) {
            throw new IOException("No .git/hooks directory found under " + root);
        }

        Path hookPath = hooksDirectory.resolve("pre-commit");
        String script = "#!/usr/bin/env sh\n"
                + "set -eu\n"
                + "mvn -q -DskipTests package >/dev/null\n"
                + "java -jar target/vaultscan-1.0-SNAPSHOT.jar scan . --fail-on high\n";
        Files.writeString(hookPath, script, StandardCharsets.UTF_8);

        try {
            Files.setPosixFilePermissions(hookPath, Set.of(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                    java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                    java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                    java.nio.file.attribute.PosixFilePermission.GROUP_READ,
                    java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE,
                    java.nio.file.attribute.PosixFilePermission.OTHERS_READ,
                    java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE));
        } catch (UnsupportedOperationException ignored) {
            // Windows marks shell hook executability differently.
        }

        return hookPath;
    }
}
