# Module-path feasibility fixture

This isolated consumer fixture is the reproducible evidence gate for issue #12. It uses the three JARs produced by the
current checkout, rather than project class directories, so its results describe the publication contract.

`./gradlew :klum-cast-compile:verifyModuleFeasibility` runs the fixture under the selected Groovy lane. It verifies:

- the three `Automatic-Module-Name` values and the exact published package ownership, including no split package;
- the compiler artifact's global AST-transformation service descriptor;
- a classpath Groovy compilation that discovers the service and executes both a nested typed check and a separately
  compiled name-bound check;
- when the selected Groovy core JAR is a usable Java module, equivalent named-module consumer compilation and Groovy
  compilation on the module path without `--add-reads`.

The consumer source has three named modules: typed annotation/check, name-bound metadata, and name-bound implementation.
The typed module explicitly imports `AnnotatedNode`, proving that the public SPI's Groovy AST dependency is resolvable.

## Current descriptor gate

JDK 11 cannot derive a module descriptor for Groovy 2.4.21: its service metadata names a provider that is absent from the
JAR. The fixture records that tool output and skips the impossible module-path consumer invocation for that lane, while
still proving classpath activation. Groovy 3.0.17 and 4.0.12 derive `org.codehaus.groovy` and `org.apache.groovy`
respectively and run the module-path scenario.

Consequently, this repository currently publishes stable automatic module identities but does **not** add explicit
`module-info.java` descriptors. Doing so would not meet the issue #12 all-supported-lanes gate. This is evidence for
maintainer review, not a decision to alter the Groovy support matrix; that remains with #13 and KlumAST #455.
