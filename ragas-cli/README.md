# ragas-cli

Command-line interface for the Java Ragas API. The CLI submits JSON requests and prints JSON responses.

## Run

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="--help"
```

## Commands

- `evaluate-sync --request-file <path>`
- `evaluate-async --request-file <path>`
- `run-status <run-id>`

Example:

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="evaluate-sync --request-file request.json"
```
