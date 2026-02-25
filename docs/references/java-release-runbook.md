# Java Release Runbook

## Preconditions
- All phase tracker tasks marked complete for the target milestone.
- Green CI on `java/**` modules.
- Security scan clean or approved exceptions documented.

## Release checklist
1. Run local checks:
   - `mvn -pl ragas-domain,ragas-core,ragas-metrics,ragas-integrations,ragas-api,ragas-cli verify`
   - `mvn -Psecurity-scan verify`
2. Update changelog/release notes.
3. Tag release candidate branch.
4. Run GitHub workflow `java-migration-ci` on tag.
5. Publish artifacts (internal registry first, then Maven Central when enabled).
6. Validate API smoke checks on deployed environment.

## Rollback
1. Mark release as failed in release notes.
2. Revert deployment to previous artifact version.
3. Open incident and root-cause tasks in `docs/exec-plans/tech-debt-tracker.md`.
