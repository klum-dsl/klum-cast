# Publish stable automatic module names first

KlumCast assigns stable module identities `com.blackbuild.klum.cast.annotations`, `com.blackbuild.klum.cast.spi`, and
`com.blackbuild.klum.cast.compiler`, initially through `Automatic-Module-Name` while retaining classpath support. Explicit
module descriptors require a Groovy 3/4/5 feasibility proof covering compilation, service loading, and classpath use,
because Groovy's module identity changes from `org.codehaus.groovy` to `org.apache.groovy`. That proof coordinates with
KlumAST #455 without selecting its multi-Groovy design.
