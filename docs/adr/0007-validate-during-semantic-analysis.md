# Validate during semantic analysis

KlumCast validates annotation state during Groovy's `SEMANTIC_ANALYSIS` phase. It guarantees that validation completes
before consumer transformations scheduled in later phases, so consumers that depend on successful validation should
perform their main mutation in `CANONICALIZATION` or later. KlumCast does not guarantee ordering relative to other
transformations in `SEMANTIC_ANALYSIS`; same-phase annotation synthesis or mutation requires a future explicit
coordination mechanism rather than relying on incidental transform order.
