# Vaultscan

Vaultscan is a Java-based DevSecOps CLI that scans source repositories for committed secrets and common infrastructure-as-code risks before they reach production.

It is designed for local developer checks, pre-commit hooks, Jenkins pipelines, and GitHub Actions.

## Features

- Detects common secrets such as AWS keys, GitHub tokens, Slack tokens, Stripe keys, private keys, JWTs, and database URLs.
- Uses high-entropy detection to flag unknown token-like values.
- Includes DevOps/IaC checks for public Terraform CIDR ranges, public S3 ACLs, Kubernetes Secret manifests, and Dockerfile secret ENV/ARG values.
- Supports `.vaultscanignore` for project-specific exclusions.
- Supports baseline fingerprints so known accepted findings do not keep breaking builds.
- Emits `text`, `json`, `sarif`, and `junit` reports.
- Uses CI-friendly exit codes.

## Build

```bash
mvn -B test package
```

## Usage

```bash
java -jar target/vaultscan-1.0-SNAPSHOT.jar scan .
java -jar target/vaultscan-1.0-SNAPSHOT.jar scan . --format json
java -jar target/vaultscan-1.0-SNAPSHOT.jar scan . --format sarif --output vaultscan.sarif
java -jar target/vaultscan-1.0-SNAPSHOT.jar scan . --fail-on high
java -jar target/vaultscan-1.0-SNAPSHOT.jar scan . --baseline vaultscan-baseline.txt
java -jar target/vaultscan-1.0-SNAPSHOT.jar install-hook .
```

## Exit Codes

- `0`: scan completed with no findings at or above the configured threshold
- `1`: scan completed and found findings at or above the configured threshold
- `2`: scanner configuration or runtime error

## Ignore Rules

Add patterns to `.vaultscanignore`:

```gitignore
target/
.git/
docs/examples/
```

## Baselines

Each finding includes a stable fingerprint. To accept an existing finding temporarily, place its fingerprint in a baseline file:

```text
# vaultscan-baseline.txt
17b1c45...
```

Then run:

```bash
java -jar target/vaultscan-1.0-SNAPSHOT.jar scan . --baseline vaultscan-baseline.txt
```

## GitHub Actions

The included workflow builds, tests, scans the repository, and uploads SARIF to GitHub code scanning.

## Jenkins

The included Jenkins pipeline publishes Vaultscan findings as JUnit XML:

```bash
java -jar target/vaultscan-1.0-SNAPSHOT.jar scan . --format junit --output target/vaultscan-junit.xml --fail-on high
```

## Current Rule Coverage

Vaultscan currently focuses on high-signal rules that are useful in CI/CD:

- Secrets: AWS, GitHub, Slack, Stripe, private keys, JWTs, database URLs, high entropy values
- Terraform: public CIDR exposure, public S3 bucket ACLs
- Kubernetes: committed Secret manifests
- Dockerfile: likely secret values in `ENV` or `ARG`

Future rule packs can extend this into cloud-specific policy checks, custom org policies, and verified secret validation.
