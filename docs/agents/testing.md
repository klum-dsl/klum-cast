# Testing

KlumCast supports Groovy 3, 4, and 5 on Java 17. Passing one lane is not evidence that the others pass. Production is
compiled once against Groovy 3; every test lane separately recompiles its Groovy tests and fixtures with the matching
Groovy and Spock generation.

| Command | Groovy generation | Use |
|---|---:|---|
| `./gradlew test` | 3 | Fast baseline and focused-development lane |
| `./gradlew groovy4Tests` | 4 | Isolated compatibility lane |
| `./gradlew groovy5Tests` | 5 | Isolated compatibility lane |
| `./gradlew check` | 3, 4, 5 | Full tests, lane isolation, packaging, JPMS, published metadata, and Gradle/Maven consumers |

Run Gradle with JDK 17. Start with the narrowest relevant Groovy-3 test. Run Groovy 4 and 5 when compiler APIs, AST
behavior, dependency compatibility, class loading, or another version seam may differ, and run `check` before final
handoff of a code or build change.

Never add the compiled Groovy/Spock test or fixture output from one generation to another generation's classpath. Only
explicit Java helpers and resources that are version-neutral may be shared. Do not restore `-PgroovyVersion` or another
whole-build selector: the one production artifact set is always compiled against Groovy 3.

Every ignored, conditionally ignored, or pending test must state an actionable reason and the unsupported contract or
blocker.
