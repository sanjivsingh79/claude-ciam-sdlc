---
name: ciam-security-review
description: Read-only security review of a Spring Boot CIAM change. Inspects the git diff and related source/config, reports findings by severity with file:line evidence and remediation. Use when asked to security-review a change, branch, endpoint, or auth/identity-related code. Never modifies files.
argument-hint: "[base ref or path, optional]"
allowed-tools: Read, Grep, Glob, Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git show:*), Bash(git ls-files:*), Bash(git rev-parse:*)
---

# CIAM Security Review

A review procedure only. **Do not edit, create, or delete files. Do not apply fixes, run formatters, or change git state.** Report findings and recommendations; the developer decides what to change.

Project-specific rules live in `CLAUDE.md`. Read it first and treat any violation of it as a finding, citing the rule. Do not restate those rules here or invent new project rules.

## Procedure

### 1. Determine scope
- If `$ARGUMENTS` names a base ref, review `git diff <base>...HEAD` plus uncommitted changes.
- If it names a path, review that path.
- Otherwise review uncommitted changes: `git status --porcelain`, `git diff`, `git diff --cached`.
- If the repo has no commits or files are untracked, include untracked files from `git status --porcelain` / `git ls-files --others --exclude-standard`.
- State the scope used at the top of the report. If there is nothing to review, say so and stop.

### 2. Gather context
For each changed file, read enough surrounding code to judge risk:
- Callers and callees of changed methods (controller → service → domain → infrastructure).
- Related DTOs, entities, exception handlers, and `@RestControllerAdvice` classes.
- Configuration: `application*.properties` / `application*.yml`, any `SecurityFilterChain` / `@Configuration` classes, `build.gradle`, `settings.gradle`, `gradle/wrapper/gradle-wrapper.properties`.
- Existing tests touching the changed code (note security cases that are missing).

Also check unchanged code when the change depends on it (e.g. a new endpoint relies on an existing security config).

### 3. Check each area
Work through every area below. Record "No issues found" for areas checked with nothing to report, and "Not applicable" with a reason where relevant.

**A. Authentication and authorization**
- New or changed endpoints: who can call them? Is there authentication at all?
- `permitAll()`, missing `@PreAuthorize` / method security, overly broad request matchers, matcher order.
- Object-level authorization (BOLA/IDOR): can a caller read or modify another customer's data by changing an ID?
- Function-level authorization: admin operations reachable by normal users.
- Token handling: validation of signature, issuer, audience, expiry; token lifetime; refresh/rotation; storage.

**B. Account enumeration**
- Different status codes, messages, or response bodies for existing vs. non-existing accounts (registration, login, password reset, MFA, lookup).
- Observable timing differences (e.g. skipping password hashing when the user does not exist).
- Validation or conflict errors that reveal whether an email/phone/ID is registered.

**C. PII exposure**
- Responses returning more PII than needed; persistence entities serialized directly.
- PII in URLs/query strings (ends up in access logs, browser history, proxies).
- Java `record` / Lombok `@Data` / generated `toString()` on types holding PII, passwords, tokens, or OTPs.
- PII copied into exceptions, metrics tags, or trace attributes.

**D. Secrets and sensitive data in logs**
- Log statements including passwords, tokens, OTPs, emails, phone numbers, full request/response bodies, or headers such as `Authorization` / `Cookie`.
- Logging of whole objects or exceptions whose message contains sensitive values.
- Hard-coded secrets, keys, or credentials in source, config, tests, or committed `.env` files.
- Debug/trace logging levels enabled for security or web packages in non-local config.

**E. Input validation**
- `@Valid` / `@Validated` present where request bodies, params, and path variables enter.
- Constraints present and sensible: required fields, formats, length limits on every string.
- Normalization (trim, case) before uniqueness checks or comparisons.
- Mass assignment: DTOs accept only intended fields; no binding directly to entities.
- Injection risks: string-built queries, unvalidated input in redirects, headers, or file paths.

**F. Error responses**
- Stack traces, exception class names, SQL, or internal paths returned to clients.
- `server.error.include-stacktrace`, `include-message`, `include-binding-errors`, `include-exception` settings.
- Rejected input values echoed back in validation errors.
- Consistent error format; unhandled exceptions falling through to default handlers.

**G. CORS and CSRF (where applicable)**
- CORS: wildcard origins, `allowCredentials(true)` with broad origins, overly broad methods/headers.
- CSRF: disabled for cookie/session-authenticated endpoints; acceptable only for stateless token-based APIs — verify which applies.

**H. Rate limiting / abuse protection**
- Login, registration, password reset, OTP verification, and lookup endpoints without throttling, lockout, or other abuse controls.
- Unbounded inputs or result sets (pagination limits, request size limits).
- OTP/code brute-force: attempt limits, expiry, single use.

**I. Sensitive Actuator exposure**
- `management.endpoints.web.exposure.include` exposing `env`, `configprops`, `heapdump`, `threaddump`, `loggers`, `beans`, `mappings`, `shutdown`, or `*`.
- `management.endpoint.health.show-details` / `show-components` set to `always`.
- Actuator reachable without authentication or on the public port.

**J. Dependency and security configuration**
- Changes to `build.gradle` dependencies, plugin versions, or repositories; new dependencies and why they are needed.
- Gradle wrapper changes (`distributionUrl`, checksum validation disabled).
- Changes to security config, TLS settings, cookie flags (`Secure`, `HttpOnly`, `SameSite`), session settings.
- Known-vulnerable or unmaintained libraries, if identifiable from the change (state if unverifiable offline).

**K. OWASP API Security Top 10 cross-check**
Confirm coverage of: broken object-level authorization, broken authentication, broken object property-level authorization (excessive data exposure / mass assignment), unrestricted resource consumption, broken function-level authorization, unrestricted access to sensitive business flows, SSRF, security misconfiguration, improper inventory management (undocumented/shadow endpoints), unsafe consumption of third-party APIs.

### 4. Classify findings

| Severity | Meaning |
|---|---|
| **Critical** | Directly exploitable; exposes credentials, tokens, or bulk PII, or bypasses authentication. |
| **High** | Exploitable with modest effort or conditions; e.g. BOLA, account enumeration on a public flow, secrets in logs. |
| **Medium** | Weakens defenses or needs other flaws to exploit; e.g. missing rate limiting, verbose errors, weak validation. |
| **Low** | Hardening or defense-in-depth improvement with limited direct impact. |
| **Info** | Observation, missing test coverage, or question for the developer. |

For each finding also state **confidence**: Confirmed (evidence in code) or Suspected (depends on context not visible, e.g. deployment or a missing config).

Only report issues backed by evidence in the code or config. Do not pad the report with generic advice.

### 5. Report

Use this format:

```
## CIAM Security Review

Scope: <what was reviewed>
CLAUDE.md rules applied: <yes/no; list any violated>

### Summary
<1–3 sentences; counts by severity>

### Findings

#### [SEVERITY] <short title>
- Area: <A–K area name>
- Location: <path:line> (one or more)
- Confidence: Confirmed | Suspected
- Evidence: <code/config excerpt or precise description>
- Risk: <what an attacker could do>
- Remediation: <specific recommended change — described, not applied>

(repeat, ordered Critical → Info)

### Areas checked with no findings
<list>

### Not applicable
<area — reason>

### Questions for the developer
<context needed to confirm Suspected findings>

### Requires explicit developer review
<findings that touch security-sensitive areas per CLAUDE.md>
```

End the review there. Do not offer to apply fixes as part of the report; if the developer asks for fixes afterwards, that is a separate task.
