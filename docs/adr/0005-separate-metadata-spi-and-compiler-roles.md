# Separate metadata, check SPI, and compiler roles

KlumCast separates three artifact roles: lightweight declarative annotations and metadata without a transitive Groovy
compiler dependency; a Groovy-dependent public check SPI; and the compiler engine with built-in implementations and
activation. Exact coordinates and compatibility bridges remain open, but AST-dependent SPI types must not remain in the
metadata artifact while its published POM hides Groovy.
