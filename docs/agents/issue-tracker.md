# Issue tracker: GitHub

GitHub Issues are this repository's issue and design tracker. Infer the repository from `git remote -v` and use `gh` for
issue operations.

## Policy

- Read the complete issue, comments, labels, relationships, and current state before acting.
- Separate discoverable facts from decisions that require maintainer confirmation.
- Do not normalize issue bodies, labels, milestones, dependencies, or state until the intended result is confirmed.
- Use closing keywords only when the accepted issue scope is fully delivered.
- External pull requests are not treated as feature requests.
- Create implementation work on a dedicated issue branch as described in `docs/agents/commits.md`.

Typical read-only commands are `gh issue view <number> --comments` and `gh issue list --state open --json
number,title,body,labels,comments`.
