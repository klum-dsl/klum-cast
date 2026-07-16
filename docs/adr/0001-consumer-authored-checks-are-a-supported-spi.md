# Consumer-authored checks are a supported SPI

KlumCast supports consumer-authored checks as a core extension capability, not as an incidental implementation hook.
Consumers must be able to bind their own declarative validation annotations to check implementations without owning a
parallel transformation; the current `checks.impl` package, mutable base-class fields, annotation-stack representation,
and reflective construction mechanism are not thereby accepted as the permanent SPI shape.
