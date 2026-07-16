# Pull requests and release-facing documentation

## Scope and issue links

- Use closing keywords only for issues whose confirmed behavior is fully delivered.
- Treat GitHub closing keywords as mechanical syntax, not prose. In pull-request titles and bodies, never place `close`,
  `closes`, `closed`, `fix`, `fixes`, `fixed`, `resolve`, `resolves`, or `resolved` before an issue reference unless the
  pull request is intended to close that issue automatically. Negation, quotation, code formatting, and explanatory
  context do not make the pattern safe. For non-closing relationships, use neutral wording such as `Related: #123`,
  `Issue #123 remains open`, or `This pull request leaves the issue state unchanged`.
- Reference related work that remains deferred and state explicitly what the pull request does not implement.
- Keep the summary, compatibility impact, and validation evidence current as commits are added.

## Quality evidence

- Review changed source against `docs/agents/coding-style.md`.
- Follow `docs/agents/testing.md`: run focused tests during development and every supported Groovy lane before final handoff
  when the task changes code or build behavior.
- Inspect all required CI checks for the current revision. Green CI proves the tested state, not that unresolved product
  decisions are complete.
- Fix reliability and security findings. Retain a maintainability finding only with a localized suppression and a concrete
  reason.

## Review follow-up

- Preserve reviewed commits and address feedback with additive commits as required by `docs/agents/commits.md`.
- Post one consolidated follow-up response covering every observation: what changed, what intentionally did not change and
  why, and which focused tests, compatibility lanes, CI checks, and static analysis support the result.
- A request to address review feedback authorizes that consolidated response after changes are pushed. It does not
  authorize resolving threads, dismissing reviews, or submitting a review.

## Public and release documentation

- Update `README.md` for changes to installation, artifact roles, supported environments, extension contracts, or user
  behavior.
- Record user-visible features, fixes, deprecations, and compatibility breaks in the repository's release-facing
  documentation once its confirmed location exists.
- Do not present implementation notes under `docs/` as user documentation unless the repository explicitly adopts them for
  that purpose.
