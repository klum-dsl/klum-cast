# Keep annotations and SPI as sibling dependencies

`klum-cast-annotations` and `klum-cast-spi` do not depend on each other. Annotations owns lightweight name-based binding;
SPI owns typed binding and its public API uses the Groovy compiler API. `klum-cast-compile` depends on both sibling
artifacts and normalizes their binding forms into SPI-owned invocation data. This avoids both an annotations-to-SPI cycle
and an unnecessary SPI-to-annotations edge while allowing one compiler dependency to supply everything needed for
activation.

ADR 0026 supersedes the original transitive-version-selection clause: SPI publication metadata does not force Groovy;
custom-check authors select the matching Groovy 3, 4, or 5 compiler dependency explicitly.
