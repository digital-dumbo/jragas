# CLI Usage

The CLI is a thin wrapper around the API endpoints. It submits requests and prints JSON responses.

## Run the CLI

Use Maven to execute the CLI main class:

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="--help"
```

## Evaluate (sync)

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="evaluate-sync --request-file request.json"
```

Override the API URL:

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="evaluate-sync --url http://localhost:8080/api/v1/evaluate --request-file request.json"
```

## Evaluate (async)

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="evaluate-async --request-file request.json"
```

## Run status

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="run-status <run-id>"
```

You can set the base URL with `--base-url`.
