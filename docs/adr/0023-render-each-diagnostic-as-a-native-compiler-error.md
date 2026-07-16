# Render each diagnostic as a native compiler error

The compiler adapter emits one native Groovy compiler error for each KlumCast diagnostic, includes the stable diagnostic
code in rendered text, and uses the diagnostic's primary AST node for positioning. Related locations and composition
provenance render as supplementary context rather than extra failures. Independent diagnostics are emitted in deterministic
source and binding order instead of being collapsed. Technical failures are reported separately with binding identity and
preserved causes. The structured diagnostic is authoritative; Groovy-specific rendering remains an adapter detail.
