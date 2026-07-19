# Changes

## 0.4.0 (unreleased)

- Raised the baseline to Java 17 and replaced Groovy 2.4 support with isolated Groovy 3, 4, and 5 verification.
- Compile the production artifacts once against Groovy 3 and verify the same publication with Gradle, Maven, classpath,
  and module-path consumers in all supported generations.
- Added `klum-cast-spi` and the stateless custom-check, typed binding, applicability-filter, and structured-diagnostic
  contracts. Compiler activation remains in `klum-cast-compile`; `klum-cast-annotations` remains Groovy-free.
- Removed forced Groovy version selection from SPI publication metadata. Custom-check authors now declare the matching
  Groovy compiler explicitly (`org.codehaus.groovy` for Groovy 3; `org.apache.groovy` for Groovy 4 and 5).
- Retained the 0.4 migration bridges for the legacy base class, exception/result shape, raw typed binding, and old OR
  declarations. These bridges are scheduled for removal in 1.0; see `docs/migration/0.4.md`.

## 0.3.x

The 0.3 line is the final line for Java 11 and Groovy 2.4. It receives necessary fixes only.
