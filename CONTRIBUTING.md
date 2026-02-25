# Development Guide for Ragas Java

This guide covers development workflows for the Java/Spring Boot migration of Ragas.

## Quick Start

```bash
# 1. Clone and enter the repository
# 2. Build all modules
mvn clean verify
```

## Repository layout

```
/                              # Root project
├── ragas-domain               # Dataset schema and sample types
├── ragas-core                 # Evaluation orchestration and validation
├── ragas-metrics              # Default metrics and aggregation
├── ragas-llm-adapters          # OpenAI adapter building blocks
├── ragas-embedding-adapters    # Embedding adapter scaffolding
├── ragas-integrations          # Integration helpers (R2R, tracing)
├── ragas-api                   # Spring Boot API
├── ragas-cli                   # CLI (Picocli)
├── ragas-e2e-tests             # End-to-end tests
├── docs                        # Documentation
└── plan                        # Migration plan
```

## Essential commands

- `mvn clean verify` - Build and run unit/integration tests
- `mvn -pl ragas-api -am spring-boot:run` - Run the API
- `mvn -pl ragas-cli -am exec:java -Dexec.mainClass=io.ragas.cli.RagasCli -Dexec.args="--help"` - Run the CLI
- `mvn -Psecurity-scan verify` - Dependency vulnerability scan

## Development workflow

1. Create a feature branch.
2. Make changes with tests or updates to docs.
3. Run `mvn clean verify`.
4. Open a pull request with a clear summary and testing notes.

## Documentation updates

If you change APIs, metrics, or schema, update the relevant docs under `docs/`:

- `docs/howtos/api.md`
- `docs/howtos/cli.md`
- `docs/references/dataset-schema.md`
- `docs/references/metrics.md`
- `docs/references/run-config.md`

## Code quality

- Keep code and documentation in sync.
- Prefer explicit error messages and clear validation.
- Avoid introducing provider secrets in source control.

## Support

Questions and coordination happen in Discord: https://discord.gg/5djav8GGNZ
