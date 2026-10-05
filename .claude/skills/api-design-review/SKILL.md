---
name: api-design-review
description: Read-only REST API design review of a Spring Boot change. Inspects the git diff and related controllers/DTOs/config, flags breaking changes, and reports findings with severity, file:line evidence and remediation, separating confirmed issues from requirement-dependent recommendations. Use when asked to review an API, endpoint, controller, DTO, or API contract change. Never modifies files.
argument-hint: "[base ref or path, optional]"
allowed-tools: Read, Grep, Glob, Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git show:*), Bash(git ls-files:*), Bash(git rev-parse:*)
---

# API Design Review

A review procedure only. **Do not edit, create, or delete files. Do not apply fixes, run formatters, or change git state.** Report findings and recommendations; the developer decides what to change.

Read `CLAUDE.md` first. Its rules are authoritative: treat any violation as a confirmed issue and cite the rule. Do not restate those rules here.

**Do not invent API requirements.** A finding may only be based on:
1. `CLAUDE.md`
2. HTTP/IETF standards (RFC 9110 HTTP semantics, RFC 9457 Problem Details — obsoletes RFC 7807)
3. Conventions already established by existing code in this repository (cite the example)
4. Internal inconsistency or a defect visible in the change itself

Anything else is a **Proposed convention** and must be labelled as such.

For deep security analysis (authentication, enumeration, abuse, Actuator, etc.), defer to the `ciam-security-review` skill; here, only note API-contract aspects of PII and entity exposure.

## Procedure

### 1. Determine scope
- If `$ARGUMENTS` names a base ref, review `git diff <base>...HEAD` plus uncommitted changes.
- If it names a path, review that path.
- Otherwise review uncommitted changes: `git status --porcelain`, `git diff`, `git diff --cached`.
- If the repo has no commits or files are untracked, include untracked files (`git ls-files --others --exclude-standard`).
- State the scope at the top of the report. If no API-related code is in scope, say so and stop.

### 2. Gather context
- Changed and related `@RestController` / `@Controller` classes, request/response DTOs, `@RestControllerAdvice` / exception handlers, validation constraints, mappers, and the domain/persistence types they touch.
- Existing endpoints elsewhere in the repo, to identify established conventions (naming, status codes, error format, pagination, versioning).
- API-related configuration: `application*.properties` / `application*.yml` (e.g. `server.servlet.context-path`, `spring.mvc.problemdetails.enabled`, `server.error.*`, Jackson settings).
- OpenAPI specs or annotations, if any exist.
- Tests covering the API (`@WebMvcTest`, `@SpringBootTest`, MockMvc / MockMvcTester).
- For backward compatibility, the previous version of changed files: `git show <base>:<path>`. If there is no prior version (new file or no commits), record that the API is new and no prior contract exists in the repo.

### 3. Check each area
Work through every area. Record "No issues found" when checked with nothing to report, and "Not applicable" with a reason (e.g. no collection endpoints → pagination not applicable).

**A. Resource naming and HTTP methods**
- Resource paths are nouns; consistent pluralization and casing with existing endpoints.
- Method semantics per RFC 9110: GET safe, PUT/DELETE idempotent, POST for creation/non-idempotent actions, PATCH for partial update.
- No state changes via GET; no verbs in paths unless modelling an action deliberately.

**B. Request/response DTO design**
- Separate request and response types; no fields the client must not set (IDs, timestamps, server-managed state) unless required by the requirement.
- Field naming consistent with existing DTOs; consistent types (e.g. timestamps, IDs, enums).
- Nullability and optional fields are explicit and intentional.
- Response contains what the client needs and no more.

**C. HTTP status codes**
- Status matches outcome per RFC 9110 (e.g. 201 for creation, 204 for no body, 400 for invalid input, 404, 409, 415, 422 if used consistently).
- `201 Created` and the `Location` header: if the change uses one, check consistency; if an addressable resource exists, note absence of `Location`.
- No `200` with an error payload; no `500` for client errors.

**D. Validation**
- `@Valid` / `@Validated` on request bodies, path variables, and query params where constraints exist.
- Constraints match the documented/required contract (required fields, formats, lengths).
- Validation failures produce a consistent client error, not a 500.

**E. Error responses / RFC 9457 ProblemDetail**
- Errors use a consistent format; if `ProblemDetail` is used, check `type`, `title`, `status`, `detail`, `instance`, and extension members are used consistently.
- All relevant exceptions are mapped (validation, malformed JSON, unsupported media type, method not allowed, not found, conflict).
- No stack traces, internal class names, or rejected sensitive values in error bodies.

**F. API consistency**
- Compare naming, status codes, error shapes, date/time formats, and envelope style with existing endpoints. Cite the existing example when flagging an inconsistency.

**G. Pagination (where applicable)**
- Collection endpoints: bounded page size, deterministic ordering, consistent pagination parameters and response metadata.

**H. Filtering and sorting (where applicable)**
- Allowed filter/sort fields are explicit; unknown fields rejected or ignored consistently; no sorting on unindexed/sensitive fields without reason.

**I. Idempotency (where applicable)**
- PUT/DELETE are idempotent in implementation.
- Repeated POSTs (client retries) — what happens? Duplicate detection, natural keys, or idempotency keys. Treat the choice of mechanism as requirement-dependent.

**J. Backward compatibility and breaking changes**
Compared with the previous version, flag as **breaking**:
- Removed or renamed endpoints, paths, methods, fields, or query params.
- Changed field types, formats, or nullability (nullable → non-null in responses is usually safe; in requests, newly required is breaking).
- New required request fields; stricter validation on existing fields.
- Changed status codes, error format, or content type.
- Removed or renamed enum values; new enum values in responses (potentially breaking for strict clients).
- Changed default behaviour (sorting, page size, filtering).
Note: per `CLAUDE.md`, public API contract changes require asking the developer first — flag any such change.

**K. PII exposure (contract level)**
- Response or error bodies returning PII not needed by the client.
- PII in paths or query strings.

**L. Accidental persistence-entity exposure**
- Controllers accepting or returning entity/domain persistence types, or DTOs that wrap them.
- Jackson annotations on entities as a sign they are being serialized.

**M. API versioning**
- If the repo already has a versioning scheme, check the change follows it.
- If none exists, do not flag its absence as an issue; at most add a requirement-dependent recommendation, especially when a breaking change is present.

**N. OpenAPI / documentation**
- If OpenAPI specs or annotations exist, check they match the implementation (paths, schemas, status codes, error responses).
- If none exist, record as a requirement-dependent recommendation, not a confirmed issue.

**O. Test coverage of the API contract**
- Tests assert status codes, response body shape, headers (e.g. `Location`, `Content-Type`), and error format.
- Negative cases: each validation rule, malformed JSON, wrong media type, unknown resource, conflict.
- Tests would fail if a field were renamed or removed (contract protection).

### 4. Classify findings

**Type**
- **Confirmed issue** — violates `CLAUDE.md`, an HTTP/IETF standard, an established repo convention, or is a visible defect/inconsistency.
- **Recommendation (requirement-dependent)** — reasonable improvement whose correctness depends on requirements not established in the project. Label any convention it introduces as **Proposed convention**.

**Severity**

| Severity | Meaning |
|---|---|
| **High** | Breaking change, `CLAUDE.md` violation, entity exposure, or contract defect clients will hit (wrong status, unhandled errors → 500). |
| **Medium** | Inconsistency with existing API, missing validation or error mapping, missing contract tests for core behaviour. |
| **Low** | Minor naming, documentation, or ergonomics issue. |
| **Info** | Observation or question for the developer. |

Only report issues backed by evidence. Do not pad the report with generic advice.

### 5. Report

Use this format:

```
## API Design Review

Scope: <what was reviewed>
Prior contract: <exists at <base> | none in repo (new API)>
CLAUDE.md rules applied: <list any violated, or "none violated">

### Summary
<1–3 sentences; counts by severity; breaking changes yes/no>

### Breaking changes
<each with path:line, what changed, client impact — or "None identified">

### Confirmed issues

#### [SEVERITY] <short title>
- Area: <A–O area name>
- Location: <path:line> (one or more)
- Basis: CLAUDE.md | RFC <n> | Repo convention (<path:line of example>) | Defect
- Evidence: <code excerpt or precise description>
- Impact: <effect on API clients>
- Remediation: <specific recommended change — described, not applied>

(repeat, ordered High → Info)

### Recommendations (requirement-dependent)

#### [SEVERITY] <short title>
- Area / Location: <...>
- Depends on: <the requirement or decision needed>
- Proposed convention: <yes — describe | no>
- Recommendation: <...>

### Areas checked with no findings
<list>

### Not applicable
<area — reason>

### Questions for the developer
<requirements or decisions needed to resolve recommendations>
```

End the review there. Do not offer to apply fixes as part of the report; if the developer asks for fixes afterwards, that is a separate task.
