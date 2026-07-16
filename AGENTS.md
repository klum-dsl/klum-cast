# Agent guidance

## Issue tracker

Issues and design work for this repository live in GitHub Issues. External pull requests are not a request surface. See
`docs/agents/issue-tracker.md`.

## Triage roles

Use the canonical triage roles and exact repository labels documented in `docs/agents/triage-labels.md`. Apply a readiness
label only after the issue's actual state supports that role.

## Domain documentation

This repository is one bounded context. Read root `CONTEXT.md` before architecture or API work and record accepted,
durable trade-offs under `docs/adr/`. See `docs/agents/domain.md`.

## Coding and testing

Follow `docs/agents/coding-style.md`. The current build targets JDK 11 and verifies Groovy 2.4, 3, and 4 using the commands
and lane policy in `docs/agents/testing.md`. Issue #455 owns any cross-repository multi-Groovy redesign.

## Issue branches and commits

Implement issues on a dedicated branch using small, reasoned commits. Review and, when useful, rewrite local history before
first publication; once review begins, preserve reviewed commits and add follow-up commits. See `docs/agents/commits.md`.

## Pull requests and releases

Keep issue links, compatibility notes, test evidence, public documentation, and release-facing documentation consistent
with user-visible changes. Respond to review feedback once with a consolidated disposition. See
`docs/agents/pull-requests.md`.
