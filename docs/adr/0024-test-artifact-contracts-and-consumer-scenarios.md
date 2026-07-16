# Test artifact contracts and consumer scenarios

KlumCast tests each artifact at its responsibility boundary: metadata shape and Groovy-free publication for annotations;
immutability, diagnostics, filters, typed binding, and Groovy compatibility for SPI; and end-to-end activation, ordering,
bindings, custom checks, diagnostics, technical failures, and composition for compile. Packaging fixtures verify published
dependencies, stable module names, service discovery, no split packages, and classpath/module-path use. Consumer fixtures
cover nested typed checks and split name bindings. Coverage is reported per code-bearing artifact, but risk-based scenario
gates rather than one aggregate percentage determine release readiness.
