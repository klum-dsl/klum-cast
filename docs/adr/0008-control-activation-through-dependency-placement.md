# Control activation through dependency placement

Dependency placement is KlumCast's supported activation control: adding the compiler artifact to the Groovy compilation
classpath enables its service-loaded transformation, and removing that artifact disables it while metadata and check-SPI
artifacts remain inert. KlumCast does not initially provide a runtime flag, system property, or source-level opt-out,
because hidden environmental state would make validation inconsistent. A more granular opt-out may be added only for a
concrete tooling or embedding use case that cannot control its compilation classpath.
