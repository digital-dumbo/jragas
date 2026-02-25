# API Usage

The Spring Boot API exposes evaluation endpoints under `/api/v1`.

Base URL (local): `http://localhost:8080`

## Endpoints

### GET /api/v1/ping

Health check endpoint.

### POST /api/v1/evaluate

Run a synchronous evaluation.

Request body: `EvaluateRequest`

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
  "metrics": ["answer_relevancy", "faithfulness"],
  "batchSize": 10,
  "rowTimeoutMs": 30000,
  "maxWorkers": 4
}
```

Response body: `EvaluateResponse`

```json
{
  "runId": "c0b5d1e7-6f9e-4c97-9a52-6c8b0b3c431b",
  "status": "COMPLETED",
  "rows": [
    {
      "rowIndex": 0,
      "metrics": {
        "answer_relevancy": 0.5
      },
      "error": null
    }
  ],
  "aggregated": {
    "answer_relevancy": 0.5
  },
  "error": null
}
```

The `rows.metrics` values are the raw metric scores for each row. The `aggregated` map contains a mean for numeric metrics, or a frequency map for non-numeric metrics.

### POST /api/v1/evaluate/async

Submit an asynchronous evaluation run.

Response body: `AsyncRunCreatedResponse`

```json
{
  "runId": "c0b5d1e7-6f9e-4c97-9a52-6c8b0b3c431b",
  "status": "RUNNING"
}
```

### GET /api/v1/runs/{runId}

Check run status and results.

Response body: `RunResponse`

```json
{
  "runId": "c0b5d1e7-6f9e-4c97-9a52-6c8b0b3c431b",
  "status": "COMPLETED",
  "progress": {
    "totalRows": 1,
    "completedRows": 1,
    "failedRows": 0,
    "status": "COMPLETED"
  },
  "rows": [
    {
      "rowIndex": 0,
      "metrics": {
        "answer_relevancy": 0.5
      },
      "error": null
    }
  ],
  "aggregated": {
    "answer_relevancy": 0.5
  },
  "error": null
}
```

### DELETE /api/v1/runs/{runId}

Cancel an in-flight run. Returns `202 Accepted` if cancellation was requested, or `404 Not Found` if the run does not exist.

### POST /api/v1/integrations/r2r/transform

Transform R2R inputs into Ragas dataset rows.

Request body: `R2rTransformRequest`

```json
{
  "userInputs": ["Question 1", "Question 2"],
  "responses": ["Response 1", "Response 2"],
  "retrievedContexts": [["ctx a"], ["ctx b"]],
  "referenceContexts": [["ref ctx a"], ["ref ctx b"]],
  "references": ["ref 1", "ref 2"]
}
```

Response body: `R2rTransformResponse`

```json
{
  "datasetRows": [
    {
      "user_input": "Question 1",
      "response": "Response 1",
      "retrieved_contexts": ["ctx a"],
      "reference_contexts": ["ref ctx a"],
      "reference": "ref 1"
    }
  ]
}
```

## Trace IDs and errors

All responses include an `X-Trace-Id` header. If a request does not supply `X-Trace-Id`, the server generates one and returns it.

Error responses use the following shapes:

```json
{
  "error": "Invalid request",
  "details": "...",
  "traceId": "..."
}
```

```json
{
  "error": "Internal server error",
  "traceId": "..."
}
```
