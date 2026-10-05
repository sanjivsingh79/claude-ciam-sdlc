# CLAUDE.md

## Project
- Path: `ciam-api/ciam-api`
- Java 21, Spring Boot 4.0.8
- Gradle 9.7.1 with Groovy build scripts
- Spring MVC (servlet-based)
- Bean Validation, Spring Boot Actuator
- Tests: JUnit 5

## Architecture
- Layers: `api` / `service` / `domain` / `infrastructure`.
- Controllers must not access repositories directly.
- Use DTOs at API boundaries; never expose persistence entities directly.

## CIAM security
- Never log passwords, tokens, OTPs, or full PII.
- Do not reveal whether a user account exists through different error responses.
- Security-sensitive changes require explicit developer review.

## Development rules
- Run tests after implementation changes.
- Ask first before modifying:
  - dependency versions
  - security configuration
  - database schema
  - public API contracts
  - Git configuration
- Never push to Git.
