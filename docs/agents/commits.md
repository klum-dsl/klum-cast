# Issue branches and commit history

## Dedicated branches

- Create a branch dedicated to the current issue or design task before the first commit.
- Keep unrelated work and user-owned worktree changes out of the branch history.
- On a newly created issue branch, agents may create and amend commits without asking until the branch is published for
  review. Obtain permission before committing on shared or unrelated branches.

## Reasoned commits

- Make each commit one self-contained reasoning step with a concise imperative subject.
- Treat commit subjects and bodies as GitHub issue-linking input. Do not place a GitHub closing keyword before an issue
  reference unless merging that commit is intended to close the issue automatically; negated wording can still trigger
  GitHub's pattern matching. Use neutral issue references as described in `docs/agents/pull-requests.md`.
- Keep a test and the production change that makes it pass in the same commit.
- Run the relevant verification before each commit. A deliberately transitional commit must make the reasoning clearer,
  be repaired immediately, and explain the boundary.
- Documentation must match the final branch state.

## Before first publication

Inspect the complete issue-branch history and final diff. Combine, split, reorder, or reword unpublished commits when that
improves focus and reviewability, then rerun verification on the final tip.

## After review begins

Human review and automated findings such as static-analysis results freeze the reviewed commits. Address feedback with
additive, focused follow-up commits unless the maintainer explicitly requests a history rewrite.
