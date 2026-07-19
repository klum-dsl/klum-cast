# The check SPI exposes Groovy AST types

The supported check SPI intentionally exposes Groovy compiler AST types because KlumCast is Groovy-specific and custom
checks need the compiler's full target model. Public documentation must therefore state the supported Groovy compatibility
contract rather than presenting the dependency as an internal implementation detail.

ADR 0026 supersedes the original publication mechanism: the SPI still has a real Groovy API requirement, but its POM does
not force a Groovy selector across the group/module-name boundary. Custom-check consumers declare the matching compiler
dependency explicitly.
