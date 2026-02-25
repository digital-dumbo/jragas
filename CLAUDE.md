# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This repository is the Java/Spring Boot migration of Ragas. It focuses on the core evaluation engine, dataset schema validation, default metrics, an OpenAI-first adapter module, and a Spring Boot API + CLI.

The repository contains:

1. **Core evaluation engine** - Orchestration, validation, and execution contracts (`ragas-core`)
2. **Domain model** - Dataset schema and sample types (`ragas-domain`)
3. **Metrics** - Default metrics and aggregation (`ragas-metrics`)
4. **Adapters** - Provider adapter building blocks (`ragas-llm-adapters`, `ragas-embedding-adapters`)
5. **Integrations** - External integration helpers (`ragas-integrations`)
6. **API + CLI** - Spring Boot API and Picocli CLI (`ragas-api`, `ragas-cli`)
7. **E2E tests** - Contract and integration tests (`ragas-e2e-tests`)

## Development Environment Setup

### Prerequisites

- Java 21
- Maven 3.9+

### Build

```bash
mvn clean verify
```

### Run the API

```bash
mvn -pl ragas-api -am spring-boot:run
```

### Run the CLI

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="--help"
```

### Security scan

```bash
mvn -Psecurity-scan verify
```

## Common Commands

```bash
# Build and test all modules
mvn clean verify

# Run API
mvn -pl ragas-api -am spring-boot:run

# Run CLI
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="evaluate-sync --request-file request.json"

# Dependency vulnerability scan
mvn -Psecurity-scan verify
```

## Repository Structure

```sh
/                          # Root project
├── ragas-domain           # Dataset schema and sample types
├── ragas-core             # Evaluation orchestration and validation
├── ragas-metrics          # Default metrics and aggregation
├── ragas-llm-adapters     # Provider adapters (OpenAI-first)
├── ragas-embedding-adapters # Embedding adapters
├── ragas-integrations     # Integration helpers (R2R, tracing)
├── ragas-api              # Spring Boot API
├── ragas-cli              # CLI (Picocli)
├── ragas-e2e-tests         # End-to-end tests
├── docs                   # Documentation
├── plan                   # Migration plan
└── README.md              # Repository overview
```

## Notes for Agents

- Use `plan/ragas-java-spring-migration-plan.md` as the source of truth for scope.
- Update docs under `docs/` when APIs, metrics, or schema change.
- CI must pass `mvn verify` and `mvn -Psecurity-scan verify`.
