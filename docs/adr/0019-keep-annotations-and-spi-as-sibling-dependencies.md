# Keep annotations and SPI as sibling dependencies

`klum-cast-annotations` and `klum-cast-spi` do not depend on each other. Annotations owns lightweight name-based binding;
SPI owns typed binding and publicly depends on the Groovy compiler API. `klum-cast-compile` depends on both sibling
artifacts and normalizes their binding forms into SPI-owned invocation data. This avoids both an annotations-to-SPI cycle
and an unnecessary SPI-to-annotations edge while allowing one compiler dependency to supply everything needed for
activation.
