# SECURITY

## Scope
Security posture for Java/Spring migration artifacts.

## Baseline controls
- Secrets only from environment or secret manager.
- No provider credentials in source control.
- Dependency vulnerability scanning in CI.
- Secure HTTP defaults and input validation at API boundaries.

## Implemented controls (Phase 7)
- Java runtime policy enforced (`maven-enforcer-plugin`: Java 21+).
- Dependency vulnerability scan profile (`mvn -Psecurity-scan verify`) using OWASP Dependency-Check.
- API error responses hide internal stack traces and include trace IDs only.
- Request correlation header `X-Trace-Id` generated/propagated by API filter.

## Required CI checks
- `mvn verify`
- `mvn -Psecurity-scan verify`

## Secret handling
- `OPENAI_API_KEY` and other provider keys must come from CI/env secret stores.
- No `.env` files committed to git.
