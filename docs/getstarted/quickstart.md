# Quickstart

This quickstart walks through running the Spring Boot API and submitting a single-turn evaluation.

## 1) Start the API

```bash
mvn -pl ragas-api -am spring-boot:run
```

## 2) Create a request payload

Create `request.json`:

```json
{
  "datasetRows": [
    {
      "user_input": "What is the capital of France?",
      "response": "Paris is the capital of France.",
      "retrieved_contexts": ["France's capital city is Paris."],
      "reference_contexts": ["Paris is the capital and most populous city of France."],
      "reference": "Paris"
    }
  ],
  "metrics": ["answer_relevancy", "faithfulness", "context_precision", "context_recall"],
  "batchSize": 10,
  "rowTimeoutMs": 30000,
  "maxWorkers": 4
}
```

## 3) Submit the evaluation

```bash
curl -sS -X POST http://localhost:8080/api/v1/evaluate \
  -H 'Content-Type: application/json' \
  -d @request.json
```

## 4) Use the CLI (optional)

```bash
mvn -pl ragas-cli -am exec:java \
  -Dexec.mainClass=io.ragas.cli.RagasCli \
  -Dexec.args="evaluate-sync --request-file request.json"
```

## Next steps

- See [API usage](../howtos/api.md) for endpoint details.
- See [Dataset schema](../references/dataset-schema.md) for available fields.
- See [Metrics](../references/metrics.md) for default metrics and requirements.
