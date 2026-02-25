<INSTRUCTIONS>
# AGENTS.md

## Purpose
Defines ownership, execution rules, and acceptance gates for migrating Ragas from Python to Java (Spring Boot + Maven).

## Roles
- `Tech Lead`: approves architecture and module boundaries.
- `Core Engine Owner`: owns evaluation orchestration, validation, and execution contracts.
- `Metrics Owner`: owns metric interfaces and parity with Python metric outputs.
- `Platform Owner`: owns Spring Boot API, CLI, build/release pipeline, and runtime operations.

## Working agreement
- Keep the migration plan in `plan/ragas-java-spring-migration-plan.md` as the source of truth.
- Track active work in `docs/exec-plans/active/`.
- Move finished plans to `docs/exec-plans/completed/`.
- Record debt in `docs/exec-plans/tech-debt-tracker.md`.

## Acceptance gates
- Architecture constraints documented in `ARCHITECTURE.md` and enforced via tests (ArchUnit).
- Contract parity tests pass against Python golden fixtures for MVP metrics.
- CI must pass `mvn verify` with unit, integration, and static analysis checks.

## MVP scope (Java)
- Core evaluation engine.
- Dataset schema + validation.
- Default metrics set from Python quickstart path.
- OpenAI-first adapter.
- Spring Boot API + CLI.

## Out of scope (initial)
- Full parity for all optional integrations.
- Full tutorial/notebook parity.
</INSTRUCTIONS>
