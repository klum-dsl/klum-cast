# Testing

KlumCast currently supports three Groovy generations on JDK 11. Passing one lane is not evidence that the others pass.
Issue #455 owns redesign of the cross-repository multi-Groovy approach; these commands document the current repository.

| Command | Groovy generation | Use |
|---|---:|---|
| `./gradlew test` | 2.4 | Current default and focused-development lane |
| `./gradlew test -PgroovyVersion=3.0.17` | 3 | Compatibility check |
| `./gradlew test -PgroovyVersion=4.0.12` | 4 | Compatibility check |
| `./gradlew build` with the same properties | corresponding lane | Final compilation, test, license, JAR, and Javadoc evidence |

Run under JDK 11. In a linked Git worktree, the current Nebula Release setup may also require
`-Pgit.root=<path-to-main-checkout>`; treat that as a known build limitation rather than a product contract.

Start with the narrowest relevant default-lane test. Run Groovy 3 and 4 when compiler APIs, AST behavior, dependency
compatibility, classloading, or another version seam may differ, and run all three lanes before final handoff of a code or
build change.

Every ignored, conditionally ignored, or pending test must state an actionable reason and the unsupported contract or
blocker.
