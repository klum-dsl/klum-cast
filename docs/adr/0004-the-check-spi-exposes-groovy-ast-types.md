# The check SPI exposes Groovy AST types

The supported check SPI intentionally exposes Groovy compiler AST types because KlumCast is Groovy-specific and custom
checks need the compiler's full target model. The artifact that owns the SPI must therefore publish Groovy as an explicit
API dependency, and public documentation must state the supported Groovy compatibility contract rather than presenting
the dependency as an internal `compileOnly` detail.
