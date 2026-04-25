package com.vaultscan.core;

import java.util.List;
import java.util.regex.Pattern;

public final class RuleSet {

    private RuleSet() {
    }

    public static List<Rule> defaultRules() {
        return List.of(
                secret("secret.aws.access_key", "AWS access key id", Severity.HIGH, "AKIA[0-9A-Z]{16}",
                        "Rotate the key in AWS IAM and replace it with a CI/CD secret reference."),
                secret("secret.aws.secret_key", "AWS secret access key", Severity.CRITICAL,
                        "(?i)aws(.{0,20})?(secret|private)?(.{0,20})?(key)?\\s*[:=]\\s*[\"']?[A-Za-z0-9/+=]{35,}[\"']?",
                        "Rotate the AWS credential and remove it from source control history."),
                secret("secret.github.token", "GitHub token", Severity.CRITICAL,
                        "gh[pousr]_[A-Za-z0-9_]{20,}",
                        "Revoke the token in GitHub and use Actions secrets or an external vault."),
                secret("secret.slack.token", "Slack token", Severity.HIGH,
                        "xox[baprs]-[A-Za-z0-9-]{10,}",
                        "Revoke the Slack token and inject it through your pipeline secret store."),
                secret("secret.stripe.key", "Stripe API key", Severity.HIGH,
                        "sk_(live|test)_[A-Za-z0-9]{16,}",
                        "Rotate the Stripe key and load it from environment-specific secret storage."),
                secret("secret.private_key", "Private key block", Severity.CRITICAL,
                        "-----BEGIN (RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----",
                        "Remove the private key, rotate it, and store it in a managed secret vault."),
                secret("secret.database_url", "Database connection string with credentials", Severity.HIGH,
                        "(?i)(postgres|mysql|mongodb|redis)://[^\\s:@]+:[^\\s@]+@[^\\s]+",
                        "Move database credentials to a secret manager and rotate exposed passwords."),
                secret("secret.jwt", "JWT token", Severity.MEDIUM,
                        "eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}",
                        "Treat committed JWTs as compromised and remove them from source control."),
                iac("iac.terraform.public_cidr", "Terraform resource allows traffic from the public internet",
                        Severity.HIGH, "cidr_blocks\\s*=\\s*\\[[^\\]]*\"0\\.0\\.0\\.0/0\"",
                        "Restrict CIDR ranges to approved networks or require explicit justification."),
                iac("iac.terraform.public_s3_acl", "Terraform S3 bucket appears publicly readable",
                        Severity.HIGH, "acl\\s*=\\s*\"public-read",
                        "Use private ACLs and S3 bucket public access blocks."),
                iac("iac.kubernetes.secret_manifest", "Kubernetes Secret manifest committed to source",
                        Severity.MEDIUM, "kind\\s*:\\s*Secret",
                        "Use sealed secrets, external secret operators, or runtime secret injection."),
                iac("iac.dockerfile.secret_env", "Dockerfile sets a likely secret through ENV/ARG",
                        Severity.HIGH, "(?i)^\\s*(ENV|ARG)\\s+[^\\s]*(PASSWORD|TOKEN|SECRET|KEY)[^\\s]*\\s*=\\s*\\S+",
                        "Do not bake secrets into images; pass them at runtime from a secret store."));
    }

    private static Rule secret(String id, String title, Severity severity, String regex, String remediation) {
        return new Rule(id, title, severity, Pattern.compile(regex), remediation);
    }

    private static Rule iac(String id, String title, Severity severity, String regex, String remediation) {
        return new Rule(id, title, severity, Pattern.compile(regex), remediation);
    }
}
