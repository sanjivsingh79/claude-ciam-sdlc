---
name: calm-architecture-review
description: Read-only architecture review of a Spring Boot implementation against the project's FINOS CALM architecture-as-code model. Use when reviewing implementation changes for architecture conformance, forbidden dependencies, layer violations, or architecture drift. Never modifies source code.
argument-hint: "[base ref or path, optional]"
allowed-tools: Read, Grep, Glob, Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git show:*), Bash(git ls-files:*), Bash(git rev-parse:*), Bash(calm validate:*)
---

# CALM Architecture Review

## Purpose

Review the implementation against the project's FINOS Common Architecture Language Model (CALM) architecture.

The authoritative architecture model is:

`architecture/calm/ciam-system.architecture.json`

The current implementation is a Java 21 Spring Boot application.

## Review procedure

1. Read the project's `CLAUDE.md`.
2. Read `architecture/calm/ciam-system.architecture.json`.
3. Validate the architecture model using the FINOS CALM CLI:

   `calm validate -a .\architecture\calm\ciam-system.architecture.json`

4. If CALM validation reports errors, report the architecture-model validation failure and do not infer additional architecture violations from an invalid model.
5. Inspect the Git status and current diff.
6. Identify source files affected by the change.
7. Inspect the relevant Java source and configuration.
8. Map implementation dependencies to the nodes and relationships defined in the CALM architecture.
9. Identify confirmed architecture violations or architecture drift.
10. Report findings with file and line evidence where possible.
11. Do not modify source code, architecture files, tests, Git configuration, or other project files.

## Architecture conformance rules

The current CALM architecture defines these nodes:

- `customer-api`
- `customer-service`
- `customer-repository`
- `customer-store`

The architecture defines these interactions:

- `customer-api` interacts with `customer-service`
- `customer-service` interacts with `customer-repository`
- `customer-repository` interacts with `customer-store`

Therefore:

- API/controller code must delegate customer operations to the service layer.
- Controllers must not directly depend on repository implementations or repository abstractions.
- Service code should access persistence through the repository abstraction.
- Repository implementations own access to the persistence store.
- New dependencies must not bypass the defined architectural relationships without an explicit architecture decision.
- An implementation dependency that contradicts a CALM relationship is a confirmed architecture violation.
- Do not treat every implementation detail absent from the CALM model as a violation.

## Findings

Classify findings as:

- CRITICAL — severe architecture violation creating major security, reliability, or system-boundary risk.
- HIGH — clear violation of an architectural constraint.
- MEDIUM — significant architecture drift or dependency that should be reviewed.
- LOW — minor deviation or maintainability concern.
- INFO — observation or recommendation without a confirmed violation.

For every finding provide:

- Severity
- Description
- File and line
- Expected architecture
- Actual implementation
- Recommended remediation

Distinguish confirmed violations from recommendations or areas requiring architectural clarification.

Do not report a violation merely because an implementation detail is not represented in the current CALM model.

## Output

Start with:

Architecture Review Summary

Then provide:

1. Overall assessment
2. CALM validation result
3. Findings by severity
4. Architecture evidence
5. Recommended actions

If no violations are found, explicitly state:

`No confirmed architecture violations found.`