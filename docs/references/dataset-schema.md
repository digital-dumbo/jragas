# Dataset Schema (Single-Turn)

The API accepts `datasetRows`, an array of JSON objects. Each row is mapped into a `SingleTurnSample`.

Supported fields:

- `user_input` (string)
- `retrieved_contexts` (array of string)
- `reference_contexts` (array of string)
- `retrieved_context_ids` (array of values)
- `reference_context_ids` (array of values)
- `response` (string)
- `multi_responses` (array of string)
- `reference` (string)
- `rubrics` (object map of string to string)
- `persona_name` (string)
- `query_style` (string)
- `query_length` (string)

Minimum required fields depend on the selected metrics. See `references/metrics.md` for required columns.

Example row:

```json
{
  "user_input": "What is the capital of France?",
  "response": "Paris is the capital of France.",
  "retrieved_contexts": ["France's capital city is Paris."],
  "reference_contexts": ["Paris is the capital and most populous city of France."],
  "reference": "Paris"
}
```

Multi-turn datasets are defined in the domain model but are not yet supported by default metrics in this Java port.
