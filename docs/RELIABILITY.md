# RELIABILITY

## Objectives
- Deterministic evaluation results for fixed inputs/config.
- Predictable timeout/retry behavior.
- Safe cancellation for async runs.

## Service level objectives (SLO)
- API availability: 99.9% monthly.
- Evaluation success rate (non-user-error): >= 99.5% weekly.
- P95 sync evaluation latency (<= 100 rows, default metrics): <= 5 seconds.
- Async run completion success rate: >= 99.0% weekly.

## Error budgets
- Availability budget at 99.9%: 43m 49s/month.
- Success-rate budget at 99.5%: 0.5% failed internal runs/week.

## Operational controls
- Per-row timeout via `EvaluationRunConfig.rowTimeout`.
- Cooperative cancellation through `EvaluationRunHandle.cancel()`.
- Request trace IDs (`X-Trace-Id`) for incident correlation.

## Incident response policy
- Page on-call if availability drops below 99.5% over rolling 1 hour.
- Freeze new feature releases if weekly error budget is exhausted.
- Open post-incident action items in `docs/exec-plans/tech-debt-tracker.md`.
