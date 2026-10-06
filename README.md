# Claude Code CIAM Software Engineering POC

A hands-on proof of concept demonstrating how **Claude Code and AI-assisted software engineering** can be integrated into a Java/Spring Boot CIAM development lifecycle with architecture governance, security controls, reusable Skills, specialist agents, MCP tools, deterministic quality gates, Git/GitHub workflows, and AI evaluation.

The goal is not simply to demonstrate "Claude can write code".

The goal is to demonstrate how an enterprise can build a **controlled AI-enabled software engineering platform** around Claude.

---

## 1. What this project demonstrates

This repository demonstrates an AI-assisted CIAM software engineering lifecycle:

```text
CIAM Requirement
       │
       ▼
Developer + Claude Code
       │
       ├── CLAUDE.md project instructions
       ├── User-level instructions
       ├── Skills
       ├── Subagents
       ├── Hooks
       └── MCP tools
       │
       ▼
Understand existing code
       │
       ▼
Implement change
       │
       ▼
Run tests
       │
       ├── JUnit
       ├── ArchUnit
       └── Architecture validation
       │
       ▼
Security / API / Architecture review
       │
       ▼
Git commit
       │
       ▼
Pull Request
       │
       ├── Automated CI
       ├── Deterministic architecture gates
       ├── Evaluation dataset validation
       └── AI-assisted semantic review
       │
       ▼
Quality gates
       │
       ▼
Deployment
```

The key principle demonstrated throughout the project is:

> **Use AI for reasoning and assistance, but use deterministic controls to enforce security and architecture boundaries.**

---

# 2. Technology stack

## Application

* Java 21
* Spring Boot 4
* Spring MVC
* Gradle
* JUnit 5
* Bean Validation
* Spring Boot Actuator
* Embedded Tomcat

## AI / Developer tooling

* Claude Code
* Claude Code Skills
* Claude Code Subagents
* `CLAUDE.md`
* Claude Code Hooks
* MCP
* MCP Streamable HTTP
* Anthropic Claude Code GitHub Action

## Architecture / quality

* FINOS CALM
* ArchUnit
* Git
* GitHub
* GitHub Actions

## Evaluation

* Python
* JSON-based golden evaluation datasets
* Deterministic evaluation scripts
* Mock Claude review results

---

# 3. Repository structure

The important parts of the repository are:

```text
claude-ciam-sdlc/
│
├── ciam-api/
│   └── ciam-api/
│       ├── src/
│       │   ├── main/
│       │   │   └── java/
│       │   │       └── com/example/ciam/
│       │   │           ├── api/
│       │   │           ├── service/
│       │   │           ├── domain/
│       │   │           └── infrastructure/
│       │   └── test/
│       ├── build.gradle
│       ├── settings.gradle
│       └── gradlew
│
├── .claude/
│   ├── hooks/
│   │   └── block-git-push.js
│   │
│   ├── skills/
│   │   ├── ciam-security-review/
│   │   │   └── SKILL.md
│   │   ├── api-design-review/
│   │   │   └── SKILL.md
│   │   └── calm-architecture-review/
│   │       └── SKILL.md
│   │
│   └── settings.json
│
├── architecture/
│   └── calm/
│       └── ciam-system.architecture.json
│
├── evaluation/
│   ├── golden-dataset/
│   │   ├── security/
│   │   ├── architecture/
│   │   └── api-design/
│   ├── proposed/
│   ├── mock-results/
│   ├── schemas/
│   ├── README.md
│   ├── validate_dataset.py
│   ├── adapt_claude_review.py
│   └── evaluate_results.py
│
├── mock-test-data/
│   ├── service/
│   ├── mcp-server/
│   ├── mcp-client/
│   ├── test-data.json
│   └── requirements.txt
│
├── .github/
│   ├── workflows/
│   │   ├── ci.yml
│   │   └── claude-review.yml
│   └── claude-review-prompt.md
│
└── CLAUDE.md
```

---

# 4. CIAM application architecture

The sample application is intentionally simple so that the AI engineering and governance concepts remain visible.

The application follows:

```text
                 REST API
                    │
                    ▼
             CustomerController
                    │
                    ▼
             CustomerService
                    │
                    ▼
          CustomerRepository
                    │
                    ▼
        InMemoryCustomerRepository
                    │
                    ▼
             Customer Store
```

The logical architecture is also represented as **FINOS CALM architecture-as-code**.

The CALM model defines:

```text
customer-api
      │
      ▼
customer-service
      │
      ▼
customer-repository
      │
      ▼
customer-store
```

This gives us two complementary forms of architecture governance:

### Runtime/code architecture

ArchUnit verifies implementation rules.

Example:

```text
Controller
   │
   ├── allowed → Service
   │
   └── forbidden → Repository
```

### Architecture-as-code

FINOS CALM describes the intended architecture independently of the Java implementation.

```text
CALM model
     +
ArchUnit
     +
Code review
```

This provides a useful defence-in-depth architecture model.

---

# 5. CIAM security principles

The application demonstrates several CIAM-specific security requirements.

Examples include:

* Do not log passwords.
* Do not log tokens.
* Do not log OTPs.
* Do not expose unnecessary PII.
* Do not reveal whether a customer account exists through different error responses.
* Use DTOs at API boundaries.
* Do not expose persistence entities directly.
* Validate input.
* Return controlled error responses.
* Avoid leaking implementation details.
* Security-sensitive changes require explicit review.

The project-level `CLAUDE.md` communicates these rules to Claude Code.

However:

> `CLAUDE.md` is an instruction mechanism, not a security boundary.

A developer or AI agent must not be able to bypass actual security controls merely by changing an instruction file.

---

# 6. CLAUDE.md

The root `CLAUDE.md` provides project-level instructions covering:

* Technology stack
* Architecture
* Layering
* CIAM security rules
* Development rules
* Protected changes
* Git behaviour

For example:

```text
Controllers must not access repositories directly.

Use DTOs at API boundaries.

Never log passwords, tokens, OTPs or full PII.

Run tests after implementation changes.

Ask before modifying:
- dependency versions
- security configuration
- database schema
- public API contracts
- Git configuration

Never push to Git.
```

This demonstrates how project-specific engineering context can be supplied to Claude Code without repeatedly putting it into every prompt.

---

# 7. User-level Claude Code instructions

The project also demonstrates the difference between **user-level instructions** and **project-level instructions**.

The user-level configuration can contain personal preferences and delegation rules.

Example:

```text
For CIAM test-data selection tasks,
delegate to the test-data-agent.
```

This was deliberately tested by removing the equivalent project-level rule.

The experiment demonstrated that the user-level instruction was sufficient to influence Claude Code's delegation behaviour.

This is useful for understanding the scope hierarchy:

```text
Enterprise / managed policies
          │
          ▼
User-level configuration
          │
          ▼
Project CLAUDE.md
          │
          ▼
Skills
          │
          ▼
Subagents
          │
          ▼
Hooks
```

The exact precedence and enforcement model depends on the Claude Code configuration and enterprise deployment model.

---

# 8. Claude Code Skills

The repository contains three custom review Skills.

## Security review

```text
.claude/skills/ciam-security-review/SKILL.md
```

A read-only CIAM security review.

It examines areas such as:

* Authentication
* Authorization
* Account enumeration
* PII
* Secrets
* Logging
* Validation
* Error handling
* CORS/CSRF
* Rate limiting
* Actuator exposure
* Dependency concerns
* OWASP API risks

The Skill is explicitly read-only.

It must not modify the application.

---

## API design review

```text
.claude/skills/api-design-review/SKILL.md
```

Checks:

* HTTP methods
* Resource design
* Status codes
* DTOs
* Validation
* Error responses
* Pagination
* Filtering
* Sorting
* Idempotency
* Breaking changes
* API consistency
* PII exposure
* Versioning
* Tests

---

## CALM architecture review

```text
.claude/skills/calm-architecture-review/SKILL.md
```

Checks implementation against the authoritative FINOS CALM architecture model.

The Skill:

1. Validates the CALM model.
2. Inspects the implementation.
3. Maps implementation components to CALM nodes.
4. Checks forbidden dependencies.
5. Detects architecture drift.

For example:

```text
CustomerController
       │
       ├── CustomerService     ✓
       │
       └── CustomerRepository  ✗
```

---

# 9. Skill vs Subagent

This repository demonstrates an important distinction:

```text
Subagent = WHO should perform the work?

Skill = HOW should the work be performed?
```

Example:

```text
Main Claude Agent
       │
       ├── Security Subagent
       │       │
       │       └── Security Review Skill
       │
       ├── API Review Subagent
       │       │
       │       └── API Design Skill
       │
       └── Test Data Subagent
               │
               └── Test Data Selection Skill
```

A Skill can therefore be reused by different agents.

---

# 10. MCP integration

The repository contains a complete local MCP demonstration.

The mock CIAM test-data system consists of:

```text
Claude / MCP Client
        │
        │ MCP / Streamable HTTP
        ▼
MCP Server :9000
        │
        │ REST / HTTP
        ▼
Test Data Service :8090
        │
        ▼
test-data.json
```

The MCP server exposes:

```text
find_test_customer
```

The tool can select synthetic CIAM identities based on attributes such as:

* Scenario
* Status
* MFA enabled

Example scenarios include:

```text
mfa_timeout
mfa_enrolment
oauth_invalid_state
locked_account
```

All data is synthetic.

No production customer information is required.

---

# 11. MCP vs REST API

This project intentionally uses REST underneath the MCP server.

That demonstrates an important architectural distinction:

```text
REST API
    =
application/service integration interface

MCP
    =
AI-facing tool interface
```

The MCP server does not replace the underlying service.

Instead:

```text
AI Agent
   │
   ▼
MCP
   │
   ▼
Existing service/API
```

This allows an enterprise to expose carefully selected capabilities to AI agents without redesigning every existing backend service around AI.

---

# 12. MCP Gateway vs API Gateway

A production architecture could look like:

```text
Claude / AI Agent
        │
        ▼
   MCP Gateway
        │
        ▼
   MCP Server
        │
        ▼
   API Gateway
        │
        ▼
   CIAM Service
        │
        ▼
      Data
```

The responsibilities are different.

### MCP Gateway

Controls AI-agent access to:

* Approved tools
* Tool versions
* Agent identity
* Tool authorization
* AI-specific policies
* Tool auditing
* AI usage/meters
* Tool routing

### API Gateway

Controls application/API traffic:

* Authentication
* Authorization
* Rate limiting
* Routing
* API policies
* Network controls
* API observability

The MCP layer should not replace conventional API and resource-level security.

---

# 13. Production security principle

One of the most important lessons demonstrated by the POC is:

> **Instructions influence AI behaviour; controls enforce security boundaries.**

For example, telling Claude:

```text
Do not access production customer data.
```

is not sufficient.

A production system should additionally enforce:

```text
Claude
   │
   ▼
MCP Gateway
   │
   ├── policy
   ├── authorization
   └── audit
   │
   ▼
MCP Server
   │
   ▼
API Gateway
   │
   ▼
Production service
   │
   ▼
Resource-level authorization
```

The downstream resource remains the ultimate security boundary.

---

# 14. MCP in GitHub Actions

The repository also demonstrates running the MCP runtime inside a GitHub-hosted CI runner.

This is different from the developer's local MCP server.

A GitHub-hosted runner is ephemeral.

Therefore:

```text
Workflow A
    └── MCP server

Workflow B
    └── does NOT automatically see Workflow A's MCP server
```

The MCP runtime must either:

### Option 1 — start inside the same workflow

```text
GitHub Runner
   │
   ├── Start REST service
   ├── Start MCP server
   ├── Configure .mcp.json
   └── Run Claude
```

### Option 2 — use an enterprise-hosted MCP service

```text
GitHub Runner
       │
       ▼
Claude
       │
       ▼
Enterprise MCP Gateway
       │
       ▼
MCP Servers
```

Option 1 is demonstrated here as a CI/CD POC.

Option 2 is the more realistic enterprise architecture.

---

# 15. Deterministic architecture enforcement

The project uses **ArchUnit** to enforce architecture rules independently of Claude.

Example rule:

```java
noClasses()
    .that().resideInAPackage("..api..")
    .and().haveSimpleNameEndingWith("Controller")
    .should().dependOnClassesThat()
    .haveSimpleNameEndingWith("Repository");
```

A deliberate violation was introduced:

```text
CustomerController
       │
       └── CustomerRepository
```

ArchUnit correctly failed the build.

After removing the dependency, the build passed again.

This demonstrates:

```text
AI recommendation
        ≠
architecture enforcement
```

The architecture rule is deterministic.

---

# 16. FINOS CALM architecture validation

The repository contains:

```text
architecture/calm/ciam-system.architecture.json
```

The CALM architecture is validated using the FINOS CALM CLI.

The CI pipeline runs:

```bash
calm validate -a ./architecture/calm/ciam-system.architecture.json
```

A deliberately invalid CALM document was tested by changing the architecture `unique-id` to an invalid value.

The validator failed.

The original model was then restored and validation passed.

This demonstrates architecture-as-code validation independently of AI.

---

# 17. Claude Code Hooks

The project contains a Claude Code hook:

```text
.claude/hooks/block-git-push.js
```

The hook prevents Claude Code from executing:

```bash
git push
```

The hook operates before the tool execution.

Conceptually:

```text
Claude
  │
  ▼
PreToolUse Hook
  │
  ├── git push? ──► DENY
  │
  └── otherwise ──► ALLOW
```

This is stronger than merely telling Claude:

```text
Never push to Git.
```

The project therefore demonstrates the difference between:

```text
Instruction
    vs
Tool-level enforcement
```

---

# 18. Hook lifecycle concepts

The project also explores Claude Code hook stages.

Conceptually:

```text
SessionStart
      │
      ▼
UserPromptSubmit
      │
      ▼
Claude reasoning
      │
      ▼
PreToolUse
      │
      ▼
Tool execution
      │
      ▼
PostToolUse
      │
      ▼
Claude continues
      │
      ▼
PreCompact (if required)
      │
      ▼
Claude continues
      │
      ▼
Stop
```

Different hooks serve different purposes.

For example:

* `PreToolUse` → prevent or control a tool action
* `PostToolUse` → inspect what happened
* `UserPromptSubmit` → validate/classify incoming requests
* `SessionStart` → initialise context
* `Stop` → validate completion

---

# 19. Golden evaluation dataset

The repository contains:

```text
evaluation/
└── golden-dataset/
```

The dataset contains approved AI evaluation cases.

Example categories:

```text
security
architecture
api-design
```

Example case:

```text
SEC-001
```

tests whether an AI reviewer identifies customer PII being written to application logs.

The expected result includes:

```json
{
  "finding": true,
  "severity": "HIGH",
  "rule": "CIAM-PII-LOGGING"
}
```

Another case tests controller-to-repository architecture violations.

Another tests a valid API change where:

```text
finding = false
```

This is important because a good AI evaluator must test both:

```text
True positive
False positive
False negative
Severity accuracy
```

---

# 20. Evaluation dataset governance

The evaluation dataset follows this lifecycle:

```text
Developer discovers AI failure
          │
          ▼
      proposed/
          │
          ▼
     Pull Request
          │
          ▼
     Expert review
          │
          ▼
 golden-dataset/
          │
          ▼
    Evaluation CI
```

The golden dataset is treated as a **quality asset**.

It must not be casually changed merely to make an AI system pass.

Ownership is split by expertise:

```text
AI Platform / AI Engineering
        │
        └── Evaluation framework

Security Team
        │
        └── Security cases

Architecture Team
        │
        └── Architecture cases

API Team
        │
        └── API cases

CIAM Domain Experts
        │
        └── CIAM-specific cases
```

---

# 21. Evaluation validation

The repository contains:

```text
evaluation/validate_dataset.py
```

It validates:

* Required fields
* Case IDs
* Expected result structure
* Boolean finding values
* Duplicate IDs

A deliberate invalid dataset case was tested by removing the evaluation case ID.

The validator failed.

After restoring the ID, validation passed.

The validation also runs in GitHub Actions.

---

# 22. AI evaluation gate

The repository demonstrates an additional evaluation gate.

The flow is:

```text
Claude Review
      │
      ▼
AI review result
      │
      ▼
Normalization / adapter
      │
      ▼
Structured result
      │
      ▼
Golden expected result
      │
      ▼
Evaluation
      │
      ├── PASS
      └── FAIL
```

For the current POC, the adapter normalizes the mock Claude review into a structured JSON result.

The evaluator checks:

```text
finding
severity
rule
```

A deliberate experiment changed:

```text
HIGH
```

to:

```text
LOW
```

The evaluation correctly failed.

Restoring `HIGH` caused the evaluation to pass.

This demonstrates that an AI-generated review can itself become subject to a quality gate.

---

# 23. Important distinction: deterministic CI vs AI evaluation

The project intentionally keeps these separate.

### Deterministic controls

```text
JUnit
ArchUnit
FINOS CALM
Dataset validation
SAST
Dependency scanning
Secret scanning
```

These should not depend on an LLM.

### AI-assisted controls

```text
Security reasoning
API semantic review
Architecture semantic review
Code understanding
Recommendations
Natural-language analysis
```

AI adds semantic reasoning where deterministic rules become difficult or expensive.

The intended architecture is therefore:

```text
                 CI Pipeline
                     │
       ┌─────────────┴─────────────┐
       │                           │
Deterministic checks          AI checks
       │                           │
       ├── Tests                   ├── Security review
       ├── ArchUnit                ├── API review
       ├── CALM                    ├── Architecture review
       ├── SAST                    └── Recommendations
       └── Dependency checks
```

---

# 24. Local setup

## Prerequisites

Install:

* Java 21
* Git
* Python 3.12+
* Node.js 22+
* npm
* Claude Code

Optional:

* IntelliJ IDEA
* FINOS CALM CLI

---

# 25. Clone the repository

```bash
git clone https://github.com/sanjivsingh79/claude-ciam-sdlc.git
cd claude-ciam-sdlc
```

---

# 26. Run the Spring Boot application

Navigate to:

```text
ciam-api/ciam-api
```

Windows:

```powershell
.\gradlew.bat clean test
```

Run the application:

```powershell
.\gradlew.bat bootRun
```

The application exposes the Spring Boot API locally.

Health check:

```text
GET /actuator/health
```

---

# 27. Run tests

From:

```text
ciam-api/ciam-api
```

run:

```powershell
.\gradlew.bat clean test
```

This executes:

* Unit tests
* Controller tests
* Service tests
* Repository tests
* ArchUnit architecture tests

---

# 28. Run the architecture validation locally

Install the FINOS CALM CLI if required:

```bash
npm install -g @finos/calm-cli
```

Validate:

```bash
calm validate -a ./architecture/calm/ciam-system.architecture.json
```

Expected result:

```text
Architecture validation successful
```

---

# 29. Run the MCP test-data service locally

Install Python dependencies:

```powershell
python -m pip install -r mock-test-data\requirements.txt
```

Start the test-data REST service:

```powershell
cd mock-test-data\service
python server.py
```

The service runs on:

```text
http://127.0.0.1:8090
```

---

# 30. Start the MCP server locally

Open another terminal.

From:

```text
mock-test-data/mcp-server
```

run:

```powershell
python server.py
```

The MCP server runs on:

```text
http://127.0.0.1:9000/mcp
```

---

# 31. Test the MCP tool

Open another terminal.

From:

```text
mock-test-data/mcp-client
```

run:

```powershell
python test_client.py
```

The expected architecture is:

```text
MCP Client
    │
    ▼
MCP Server :9000
    │
    ▼
REST Test Data Service :8090
    │
    ▼
test-data.json
```

The client should discover and invoke:

```text
find_test_customer
```

---

# 32. Use Claude Code locally

From the repository root:

```bash
claude
```

Claude Code will discover project-level configuration from:

```text
CLAUDE.md
.claude/
```

The project Skills are available under:

```text
.claude/skills/
```

The hooks are configured through:

```text
.claude/settings.json
```

---

# 33. Example Claude Code workflow

A developer can ask Claude Code to:

```text
Understand the existing customer registration implementation.
```

Then:

```text
Implement the requested customer lookup endpoint.
```

Then:

```text
Run the tests.
```

Then:

```text
Run the security review.
```

Then:

```text
Run the API design review.
```

Then:

```text
Run the CALM architecture review.
```

The important point is that the review Skills are deliberately read-only.

---

# 34. Git workflow

A typical developer workflow is:

```text
Create feature branch
       │
       ▼
Ask Claude to understand change
       │
       ▼
Implement
       │
       ▼
Run tests
       │
       ▼
Run AI reviews
       │
       ▼
Review changes
       │
       ▼
Commit
       │
       ▼
Push
       │
       ▼
Pull Request
```

The Claude Code hook prevents Claude itself from performing `git push`.

The developer remains responsible for the final Git operation.

---

# 35. GitHub Actions CI

The repository contains automated CI.

The pipeline performs:

```text
Checkout
   │
   ├── Java tests
   │
   ├── ArchUnit
   │
   ├── FINOS CALM validation
   │
   └── Evaluation dataset validation
```

This ensures that AI-assisted development remains subject to conventional engineering quality gates.

---

# 36. Claude PR review workflow

The repository also contains a separate workflow for Claude-assisted PR review:

```text
.github/workflows/claude-review.yml
```

The intended architecture is:

```text
GitHub Pull Request
        │
        ▼
Claude Code Action
        │
        ├── CLAUDE.md
        ├── Review Skills
        ├── Git diff
        └── MCP tools
        │
        ▼
Claude PR Review
        │
        ▼
Evaluation gate
```

The Claude workflow is intentionally separated from the normal deterministic CI pipeline.

This allows organizations to control:

* When AI reviews execute
* Which users can trigger them
* Which tools Claude can access
* Which MCP servers are available
* What permissions the workflow receives
* How AI results are evaluated

---

# 37. Enterprise-scale architecture

The local POC uses:

```text
Developer
   │
   ▼
Claude Code
   │
   ├── CLAUDE.md
   ├── Skills
   ├── Agents
   ├── Hooks
   └── Local MCP
```

An enterprise implementation could evolve into:

```text
                    Enterprise AI Platform
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
 Enterprise policies      Skill Registry      Evaluation
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                             ▼
                       Developer
                             │
                             ▼
                        Claude Code
                             │
             ┌───────────────┼────────────────┐
             │               │                │
             ▼               ▼                ▼
          Skills          Agents            Hooks
             │               │
             └───────────────┤
                             ▼
                       MCP Gateway
                             │
                 ┌───────────┼───────────┐
                 │           │           │
                 ▼           ▼           ▼
              MCP Tool    MCP Tool    MCP Tool
                 │           │           │
                 └───────────┼───────────┘
                             ▼
                       API Gateway
                             │
                             ▼
                      Enterprise APIs
                             │
                             ▼
                         Services
```

---

# 38. Governance at scale

For a large engineering organization, AI governance should not depend on every developer manually creating their own rules.

A central platform can provide:

```text
Enterprise standards
       │
       ▼
Shared Skills
       │
       ▼
Approved agents
       │
       ▼
Approved MCP tools
       │
       ▼
Evaluation datasets
       │
       ▼
CI/CD quality gates
```

Developers can still contribute domain-specific extensions.

For example:

```text
Enterprise
 ├── Security Skill
 ├── API Skill
 ├── Architecture Skill
 └── Secure coding rules

CIAM domain
 ├── CIAM security Skill
 ├── CIAM test-data agent
 ├── CIAM MCP tools
 └── CIAM evaluation cases
```

---

# 39. Key architectural lessons

This POC demonstrates several principles.

### 1. AI instructions are not security controls

```text
CLAUDE.md
Skills
Prompts
```

guide behaviour.

They should not be treated as the final security boundary.

---

### 2. Tool access must be governed

MCP introduces a new security boundary.

An enterprise needs:

```text
Identity
Authorization
Approved tools
Tool versioning
Audit
Rate limits
Policy
Network controls
```

---

### 3. AI should not replace deterministic engineering controls

Use:

```text
ArchUnit
CALM
JUnit
SAST
Dependency scanning
Secret scanning
```

where deterministic validation is possible.

Use AI where semantic reasoning provides additional value.

---

### 4. Evaluation must be treated as a first-class engineering capability

An AI system can regress after:

```text
Prompt changes
Model changes
Skill changes
Tool changes
Context changes
```

Therefore:

```text
AI change
   │
   ▼
Evaluation dataset
   │
   ▼
Quality gate
```

should become part of the engineering lifecycle.

---

### 5. MCP is an interface, not the security boundary

The architecture should remain:

```text
AI
 ↓
MCP
 ↓
API
 ↓
Service
 ↓
Resource
```

with authorization enforced at appropriate downstream boundaries.

---

# 40. POC status

The repository currently demonstrates:

* [x] Java 21 Spring Boot CIAM application
* [x] Layered architecture
* [x] CIAM security rules
* [x] `CLAUDE.md`
* [x] User-level Claude Code instructions
* [x] Claude Code Skills
* [x] Specialist subagent
* [x] MCP server
* [x] MCP client
* [x] MCP Streamable HTTP
* [x] Synthetic CIAM test data
* [x] Local MCP runtime
* [x] GitHub Actions MCP runtime
* [x] ArchUnit
* [x] FINOS CALM architecture-as-code
* [x] CALM CI validation
* [x] Claude Code hook
* [x] Git push protection
* [x] Golden evaluation dataset
* [x] Evaluation dataset validation
* [x] AI result normalization
* [x] AI evaluation gate
* [x] GitHub Actions CI
* [x] Pull Request workflow foundation

The Claude PR review execution itself is intentionally kept separate from the MCP runtime validation so that AI authentication and runtime-tooling concerns can be tested independently.

---

# 41. Why this POC exists

This project is intended as a practical demonstration of how organizations can move from:

```text
"Developers use AI coding assistants"
```

towards:

```text
"AI is an integrated, governed software engineering capability."
```

The objective is to combine:

```text
Developer productivity
        +
Software architecture
        +
Security
        +
AI governance
        +
Evaluation
        +
CI/CD
        +
Enterprise tool integration
```

without making the AI system itself the ultimate authority.

---

# 42. Recommended next evolution

Potential future enhancements include:

1. Connect the real Claude PR review workflow.
2. Replace the mock Claude review with live structured AI output.
3. Expand the evaluation dataset.
4. Add negative/false-positive evaluation cases.
5. Add model/prompt A/B evaluation.
6. Add latency and token-cost measurements.
7. Add SAST and dependency scanning.
8. Add secret scanning.
9. Add MCP Gateway simulation.
10. Add production-style workload identity.
11. Add policy-based MCP tool authorization.
12. Add versioned enterprise Skills.
13. Add automated evaluation reports to Pull Requests.
14. Add CIAM-specific coding agents.
15. Add deployment quality gates.

---

## 43. Final architecture

The overall concept demonstrated by this repository is:

```text
                         ┌───────────────────────┐
                         │   Enterprise Policy   │
                         └───────────┬───────────┘
                                     │
                                     ▼
Developer ───────────────► Claude Code
                              │
             ┌────────────────┼────────────────┐
             │                │                │
             ▼                ▼                ▼
        CLAUDE.md          Skills          Subagents
             │                │                │
             └────────────────┼────────────────┘
                              │
                              ▼
                            Hooks
                              │
                              ▼
                            MCP
                              │
                              ▼
                       Enterprise Tools
                              │
                              ▼
                         Application
                              │
                 ┌────────────┼─────────────┐
                 │            │             │
                 ▼            ▼             ▼
              JUnit       ArchUnit       CALM
                 │            │             │
                 └────────────┼─────────────┘
                              │
                              ▼
                           GitHub
                              │
                              ▼
                         Pull Request
                              │
             ┌────────────────┼────────────────┐
             │                │                │
             ▼                ▼                ▼
       Deterministic      AI Review        Evaluation
           CI                 │                │
             │                └───────┬────────┘
             │                        │
             └────────────────────────┘
                              │
                              ▼
                         Quality Gate
                              │
                              ▼
                          Deployment
```

The central design principle is:

> **AI accelerates engineering work; architecture, security, deterministic controls and evaluation govern what is allowed into the engineering lifecycle.**
