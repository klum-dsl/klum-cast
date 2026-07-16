# Use structured, source-positioned diagnostics

A KlumCast constraint diagnostic is immutable and contains a stable check-scoped code, a rendered human-readable message,
a primary Groovy AST node, optional structured message arguments and related AST nodes, and engine-attached provenance for
the check binding and composition path. Constraint diagnostics remain compiler errors initially; customizable severity
requires a concrete warning use case. Technical failures remain outside the diagnostic payload. Message override mechanics
for validation annotations are a separate decision.
