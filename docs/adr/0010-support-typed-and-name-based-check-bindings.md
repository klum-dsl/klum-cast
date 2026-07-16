# Support typed and name-based check bindings

Status: accepted after the proof of concept under `docs/implementation/prototypes/check-binding/` passed with Groovy 3
and 4.

KlumCast supports both typed check-class bindings and fully qualified implementation-class-name bindings. The metadata
artifact owns name-based binding for lightweight and cyclic split-module designs. The SPI artifact owns strongly typed
`Class<? extends Check>` binding for co-located and nested checks. A binding selects exactly one mode, and the compiler
normalizes both forms. Named implementations are resolved with the compilation classloader and verified against the check
SPI; missing, incompatible, or ambiguous bindings are technical configuration failures rather than annotation-use
diagnostics.

The existing mixed annotation's typed member may become a deprecated metadata-owned `Class<?>` migration bridge for a
documented window. The POC proved that this preserves the JVM method descriptor but loses source-level type rejection;
new typed declarations therefore use the SPI-owned annotation. It also proved nested-check ergonomics, compile-time type
rejection, name-based split-module ordering, class loading, and automatic-module behavior. KlumCast does not abstract
Groovy AST types to manufacture a dependency-free check interface.
