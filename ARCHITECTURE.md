# ARCHITECTURE.md

## Goal
Target architecture for Java Ragas using Spring Boot and Maven multi-module builds.

## Architecture principles
- Hexagonal boundaries: `api` -> `application` -> `domain` -> `infra`.
- No framework types in `domain`.
- All model providers implemented behind interfaces.
- Deterministic contracts before optimization.

## Planned module map
- `jragas`: dependency and plugin management.
- `ragas-domain`: domain model and contracts.
- `ragas-core`: orchestration, validation, execution pipeline.
- `ragas-metrics`: metric interfaces and metric implementations.
- `ragas-llm-adapters`: provider adapters.
- `ragas-embedding-adapters`: embedding providers.
- `ragas-integrations`: external integration adapters.
- `ragas-api`: Spring Boot REST surface.
- `ragas-cli`: CLI surface.
- `ragas-e2e-tests`: parity and end-to-end contracts.

## Enforced dependency rules
- `ragas-domain` depends on nothing internal.
- `ragas-core` can depend on `ragas-domain` only.
- `ragas-metrics` can depend on `ragas-core` and `ragas-domain`.
- `ragas-api` depends on `ragas-core` and adapter modules.
- adapters cannot depend on API.

## Data flow
1. API receives evaluation request.
2. Validation normalizes request into domain model.
3. Orchestrator executes per-row metric evaluations.
4. Metric engines call LLM/embedding interfaces.
5. Aggregation returns run-level and row-level results.

## Notes
- Initial behavior must mirror current Python evidence in `src/ragas`.
- Unknown deployment topology and DB schema remain open design items.
