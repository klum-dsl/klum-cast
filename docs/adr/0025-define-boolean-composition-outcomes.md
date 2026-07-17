# Define boolean composition outcomes

Status: accepted for the 0.4 migration line.

KlumCast composition evaluates an ordered sequence of branch outcomes. A branch is one of:

- **not applicable** when its applicability filters exclude the target;
- **passed** when it is applicable and returns no diagnostics;
- **failed** when it is applicable and returns one or more diagnostics; or
- a **technical failure** when binding, construction, execution, or engine work throws unexpectedly.

Not-applicable is not a passing branch and is not Boolean `false`; it is excluded from the applicable branch set. A
technical failure is never converted into a failed branch or a constraint diagnostic. It propagates with its original
cause. Branches run eagerly in ascending fully qualified annotation-type-name order. This makes ordinary outcomes and
their diagnostics deterministic and ensures a later technical failure is not hidden by an earlier passing branch. A
technical failure stops the evaluation at that point.

For every strategy, zero declared branches and any number of filtered branches therefore have the same outcome: not
applicable. With one applicable branch, AND, OR, and XOR use that branch's pass/fail outcome. With two or more
applicable branches, their defined truth tables are:

| Strategy | Pass | Fail |
| --- | --- | --- |
| AND | all applicable branches pass | one or more applicable branches fail |
| OR | one or more applicable branches pass | all applicable branches fail |
| XOR | exactly one applicable branch passes | zero or more than one applicable branches pass |

The delivered tracer is OR only. Its direct branches are validation annotations placed on the composed validation
annotation alongside `@OneCheckMustMatch`; the engine no longer derives new declarations from annotation-valued members.
When OR fails, it emits every failing branch diagnostic in branch order plus an engine-owned
`klum-cast.composition.or.no-match` summary diagnostic. The summary has the validated annotation's source position,
related branch positions, and the composition strategy's provenance. A custom `message` on `@OneCheckMustMatch` changes
only that summary; it does not erase branch codes, messages, positions, bindings, composition paths, related nodes, or
technical causes.

`@OneCheckMustMatch` remains source- and binary-compatible for the 0.4 migration line. Existing compiled declarations
with annotation-valued branch members use a deterministic, deprecated compatibility bridge. Recompiled source must move
those branch annotations directly onto the composed annotation; the marker's binary name and no-argument use remain
valid, and the optional `message` element has a default value.

AND and XOR have accepted outcome semantics but are deferred: 0.4 exposes no public annotation syntax for either.
Consumer-framework-specific conditional composition and a general conditional public syntax are rejected for this
tracer slice; a future proposal needs a concrete consumer-independent use case and must preserve these outcome and
technical-failure boundaries.
