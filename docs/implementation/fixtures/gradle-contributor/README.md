# Ordinary Gradle contributor fixture

This fixture uses Gradle's `GroovyCompile`, with a separately compiled Java provider JAR and a separate foreign local
transform JAR. It exercises the actual source/transform loader boundary, unlike a `FileSystemCompiler` JavaExec.

`verifyGradleContributorGroovy3`, `verifyGradleContributorGroovy4`, and `verifyGradleContributorGroovy5` copy and run it
against the same local three-artifact publication. Each lane compiles its own fixtures with the matching Groovy version.
They are part of root `check`.

The no-service and no-op modes contain the same provider classes; only service registration changes. The effective mode
adds a foreign annotation through the supported contributor seam. A check on an explicit existing annotation must see
the contribution. The contributed annotation's typed check, named check, and named filter must
all run once before normal Groovy local-transform discovery executes the foreign transform once. The resulting method
is asserted by running the compiled target. No consumer loader adjustment, manual validation, or transform invocation
is used.

KlumAST-specific annotation evidence is recorded separately under `docs/implementation/evidence/issue-60/`.
