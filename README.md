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

Vaultscan can be used as a reusable GitHub Action:

```yaml
name: Vaultscan

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

permissions:
  contents: read
  security-events: write

jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Run Vaultscan
        uses: Rithish14/Vaultscan@main
        with:
          scan-path: .
          format: sarif
          output: vaultscan.sarif
          fail-on: high
```

Action inputs:

| Input | Default | Description |
| --- | --- | --- |
| `scan-path` | `.` | Path to scan in the caller repository. |
| `format` | `sarif` | Report format: `text`, `json`, `sarif`, or `junit`. |
| `output` | `vaultscan.sarif` | Report output path. |
| `fail-on` | `high` | Minimum severity that fails the workflow: `low`, `medium`, `high`, `critical`, or `none`. |
| `baseline` | empty | Optional baseline fingerprint file. |
| `upload-sarif` | `true` | Upload SARIF to GitHub code scanning when `format` is `sarif`. |

For pull requests from forks, GitHub may block SARIF uploads. In that case set `upload-sarif: false` on pull request runs and upload SARIF only on trusted push runs.

The included workflow builds, tests, scans the repository through the local action wrapper, and uploads SARIF to GitHub code scanning on pushes.

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
