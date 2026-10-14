# AI Evaluation Dataset

## Purpose

This directory contains evaluation cases used to measure the quality and reliability of AI-assisted CIAM software engineering.

The dataset is version controlled and must be treated as a quality asset.

## Dataset lifecycle

Evaluation cases follow this lifecycle:

Developer discovers an AI failure or important scenario
→ proposed/
→ Pull Request
→ Expert review
→ golden-dataset/
→ Evaluation CI

## Proposed cases

The `proposed/` directory is for candidate evaluation cases.

Anyone working on the project may contribute a proposed case.

A proposed case must:

- describe the scenario clearly
- identify the relevant category
- provide representative code or change information
- define the expected result
- avoid real customer data, credentials, secrets, or production information

Proposed cases are not authoritative until approved.

## Golden dataset

The `golden-dataset/` directory contains approved evaluation cases.

Golden cases must:

- have a stable unique ID
- have a clearly defined expected result
- be reviewed by an appropriate technical owner
- contain only synthetic or non-sensitive information
- remain version controlled
- not be modified casually to make an AI evaluation pass

Changes to golden cases require Pull Request review.

## Ownership

The AI engineering/platform team owns the evaluation framework.

Domain experts contribute and approve domain-specific cases.

Examples:

- Security team → security cases
- Architecture team → architecture cases
- API/platform team → API design cases
- CIAM domain experts → CIAM-specific cases

## Evaluation principles

The evaluation dataset must contain both:

- cases where a finding is expected
- cases where no finding is expected

The dataset should test for:

- false positives
- false negatives
- severity accuracy
- rule identification
- recommendation quality
- consistency across model or prompt changes

## Security

Never place the following in evaluation data:

- real customer information
- production logs
- passwords
- access tokens
- API keys
- private certificates
- confidential business information

Synthetic data must be used wherever possible.