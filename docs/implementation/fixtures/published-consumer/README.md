# Published consumer fixture

This fixture verifies the artifacts produced by the current checkout through both Gradle and Maven consumers. Each build
declares its selected Groovy compiler dependency explicitly, adds `klum-cast-spi` for a custom check, and places
`klum-cast-compile` on the Groovy compiler invocation classpath for activation. Each build invokes the matching
`FileSystemCompiler` from its resolved dependencies. The check writes a marker while the Groovy target is compiled,
proving service discovery and execution rather than dependency resolution alone.

Run all six consumers with `./gradlew verifyPublishedConsumers`. The root build supplies the temporary publication
repository, current project version, and the matching Groovy 3, 4, or 5 coordinate. The fixture never consumes project
class directories.
