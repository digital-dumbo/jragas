# Metrics

The Java port currently includes the following default single-turn metrics:

- `answer_relevancy`
- `faithfulness`
- `context_precision`
- `context_recall`

These defaults are exposed via `DefaultMetricSet.singleTurnDefaults()` and are the only metrics accepted by the API today.

## Required columns

Each metric requires a specific set of dataset columns:

- `answer_relevancy`: `user_input`, `response`
- `faithfulness`: `response`, `retrieved_contexts`
- `context_precision`: `response`, `retrieved_contexts`
- `context_recall`: `retrieved_contexts`, `reference_contexts`

If a required column is missing, the API returns a `400` response with a descriptive error message.
