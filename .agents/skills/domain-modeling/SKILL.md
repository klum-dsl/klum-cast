---
name: domain-modeling
description: Build and sharpen repository domain language and record durable architectural decisions.
---

Read root `CONTEXT.md` and relevant files under `docs/adr/` before changing domain language or architecture.

Challenge vague or conflicting terms with concrete source and usage scenarios. Use the repository's canonical vocabulary
in issues, tests, proposals, and documentation. When a term is confirmed, update `CONTEXT.md` using
`CONTEXT-FORMAT.md`; keep implementation details and unconfirmed designs out of the glossary.

Create an ADR using `ADR-FORMAT.md` only when the accepted decision is hard to reverse, surprising without context, and
the result of a real trade-off. Surface conflicts with existing ADRs instead of silently overriding them.
