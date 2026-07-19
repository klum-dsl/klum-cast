# Module-path feasibility fixture

This isolated consumer fixture is the reproducible evidence gate for issue #12. It uses the three JARs produced by the
current checkout, rather than project class directories, so its results describe the publication contract.

`./gradlew :klum-cast-compile:verifyModuleFeasibility` runs the Groovy-3 fixture. The full Java-17 matrix is
`verifyModuleFeasibility`, `verifyGroovy4ModuleFeasibility`, and `verifyGroovy5ModuleFeasibility`. It verifies:

- the three `Automatic-Module-Name` values and the exact published package ownership, including no split package;
- the compiler artifact's global AST-transformation service descriptor;
- a classpath Groovy compilation that discovers the service and executes both a nested typed check and a separately
  compiled name-bound check;
- when the selected Groovy core JAR is a usable Java module, equivalent named-module consumer compilation and Groovy
  compilation on the module path without `--add-reads`.

The consumer source has three named modules: typed annotation/check, name-bound metadata, and name-bound implementation.
The typed module explicitly imports `AnnotatedNode`, proving that the public SPI's Groovy AST dependency is resolvable.

## Current descriptor gate

On Java 17, Groovy 3 derives module `org.codehaus.groovy`; Groovy 4 and 5 derive `org.apache.groovy`. All three lanes run
both classpath and module-path consumer compilation, discover the KlumCast transformation service, and execute typed and
name-bound custom checks.

KlumCast continues to publish stable `Automatic-Module-Name` identities rather than explicit descriptors. Named consumers
must require the generation-matching Groovy module and the KlumCast modules they actually use.
