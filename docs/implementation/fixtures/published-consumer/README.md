# Published consumer fixture

This fixture verifies either the artifacts produced by the current checkout or one exact public RC/final through Gradle
and Maven consumers. Each build declares its selected Groovy compiler dependency explicitly, adds `klum-cast-spi` for a
custom check, and places `klum-cast-compile` on the Groovy compiler invocation classpath for activation. Each build
invokes the matching `FileSystemCompiler` from its resolved dependencies. The check writes a marker while the Groovy
target is compiled, proving service discovery and execution rather than dependency resolution alone.

Run all six consumers with `./gradlew verifyPublishedConsumers`. The root build supplies the temporary publication
repository, current project version, and the matching Groovy 3, 4, or 5 coordinate. The fixture never consumes project
class directories.

After a protected publication is publicly visible in Maven Central, run
`./gradlew -PpublicVersion=X.Y.Z-rc.N verifyPublicReleaseCoordinates` (or use the final `X.Y.Z`). That path creates empty,
isolated Gradle and Maven caches, resolves all three exact public coordinates from Maven Central, repeats compiler
activation for both build tools in Groovy 3, 4, and 5, and runs the matching classpath/JPMS fixture from the Gradle
consumer. It has no publication tasks or credentials and does not use a composite build, `mavenLocal`, or a fixture
repository.
