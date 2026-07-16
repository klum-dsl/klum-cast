# Use annotations, SPI, and compile artifacts

KlumCast preserves `klum-cast-annotations` as the lightweight declarative metadata and built-in-validation-annotation
artifact, introduces `klum-cast-spi` for the Groovy-dependent check, filter, context, and diagnostic contracts, and keeps
`klum-cast-compile` for the engine, built-in implementations, and service-loaded activation. The compile artifact depends
on annotations and SPI. There is no aggregator artifact or rename of `compile` without a demonstrated need. Custom-check
authors must add the explicit SPI dependency, and moving the existing base-class API requires a documented breaking
migration and release plan.
