# Concepts

This Java port focuses on the core evaluation workflow used in the Python quickstart path.

## Evaluation flow

1. Ingest dataset rows.
2. Validate required columns for the chosen metrics.
3. Execute metric scoring per row.
4. Aggregate metric values into summary scores.

## Sample types

- Single-turn samples are fully supported.
- Multi-turn samples exist in the domain model but do not yet have default metrics in this Java port.
