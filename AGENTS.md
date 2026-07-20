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

Follow `docs/agents/coding-style.md`. The current build targets Java 17 and verifies Groovy 3, 4, and 5 using the commands
and isolated-lane policy in `docs/agents/testing.md`. The accepted cross-repository contract is recorded in KlumAST issue
#455 and ADR 0011 there; do not reintroduce whole-build Groovy version selection.

## License plugin

If license-plugin configuration conflicts with a planned change, ask for the plugin or its configuration to be changed.
Never rename, retype, or otherwise adapt files merely to circumvent license-header handling; for example, do not rename a
`.txt` file to `.java` because the plugin cannot handle `.txt`.

An outdated license-file year requires a dedicated issue. If an outdated year or related structural problem is discovered
incidentally during another task, ask for confirmation before creating the separate issue or task.

## Issue branches and commits

Implement issues on a dedicated branch using small, reasoned commits. Review and, when useful, rewrite local history before
first publication; once review begins, preserve reviewed commits and add follow-up commits. See `docs/agents/commits.md`.

## Pull requests and releases

Keep issue links, compatibility notes, test evidence, public documentation, and release-facing documentation consistent
with user-visible changes. Respond to review feedback once with a consolidated disposition. See
`docs/agents/pull-requests.md`. Follow `docs/agents/releases.md` for every release candidate or final release; its
protected publication path is the only supported public release path.
