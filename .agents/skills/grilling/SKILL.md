---
name: grilling
description: Grill the user about a plan or design, resolving one decision at a time before implementation.
---

Explore the repository before asking questions. If a fact is discoverable from source, tests, history, documentation, or
the issue tracker, find it rather than asking the user.

Walk the design tree in dependency order. Ask exactly one decision question at a time, give a recommended answer with its
evidence and trade-offs, and wait for the user's response. Do not reopen an accepted answer without contradictory evidence.

Record confirmed domain language in `CONTEXT.md` immediately. Offer an ADR only for a confirmed decision that is hard to
reverse, surprising without context, and the result of a real trade-off. Do not enact unconfirmed product or architecture
decisions.
