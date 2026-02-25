# Run Configuration

Evaluation runs accept optional configuration fields to control parallelism and timeouts.

## Fields

- `batchSize` (integer, default `10`)
- `rowTimeoutMs` (integer milliseconds, default `30000`)
- `maxWorkers` (integer, default `max(1, availableProcessors)`)

## Defaults

If you omit these fields, the API uses `EvaluationRunConfig.defaults()`:

- `batchSize`: 10
- `rowTimeoutMs`: 30000
- `maxWorkers`: number of available processors (minimum 1)

## Example

```json
{
  "datasetRows": [
    {
      "user_input": "What is X?",
      "response": "X is ..."
    }
  ],
  "metrics": ["answer_relevancy"],
  "batchSize": 5,
  "rowTimeoutMs": 15000,
  "maxWorkers": 2
}
```
