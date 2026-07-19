# Check writer: write a reusable check

A check writer implements reusable constraint logic. This journey creates `@MethodNameStartsWith`, a validation
annotation whose check accepts `setName` and reports an actionable diagnostic for `rename`.

## Prerequisites and dependencies

The [example project-module overview](README.md#example-project-modules) uses the dependency chain `:custom-checks` →
`:domain-annotations` → `:consumer`. This page configures `:custom-checks`. Use Java 17 and select the Groovy 3, 4, or 5
compiler that matches the compilation where the check will run.

```groovy
dependencies {
    compileOnly "com.blackbuild.klum.cast:klum-cast-spi:$klumCastVersion"
    compileOnly "org.apache.groovy:groovy:$groovyVersion" // Groovy 4 or 5; use org.codehaus.groovy for Groovy 3
}
```

Use Maven's `provided` scope for SPI and Groovy. The typed example needs no annotations-artifact API; add
`klum-cast-annotations` at `implementation`/default compile scope when the same module uses built-in annotations or
name-based metadata. Add `klum-cast-compile` as `compileOnly`/`provided` only to a Groovy compilation that should execute
the check. See the [shared dependency table](README.md#dependencies-by-role).

## 1. Declare and bind the validation annotation

The executable example declares
[`@MethodNameStartsWith`](../../klum-cast-compile/src/test/java/com/blackbuild/klum/cast/docs/onboarding/MethodNameStartsWith.java)
with runtime retention and `ANNOTATION_TYPE` target. Its typed `@CheckBinding` names the check class and a method-target
filter.

Prefer typed `@CheckBinding(MyCheck.class)` when the validation annotation can compile against its implementation. The
compiler verifies the type, and nested or co-located implementations are convenient. Use the name binding
`@KlumCastValidator("example.SplitCheck")` when a lightweight metadata module must compile independently of the
implementation module. Name binding defers class resolution until validation runs; a missing or incompatible class is a
technical failure. The deprecated raw `KlumCastValidator.type()` member is only a 0.4 migration bridge.

## 2. Implement a stateless check

[`MethodNameStartsWithCheck`](../../klum-cast-compile/src/test/java/com/blackbuild/klum/cast/docs/onboarding/MethodNameStartsWithCheck.java)
implements `Check` and has an accessible no-argument constructor. It obtains the typed control annotation and target from
the immutable `CheckContext`; it stores no invocation state.

The compiler owns check instances. It may reuse one instance within a compilation, but never across compilation runs.
Treat every `CheckContext` and its binding/composition data as the complete invocation input.

The SPI intentionally exposes Groovy compiler AST types such as `AnnotatedNode`, `AnnotationNode`, and `MethodNode`.
That is why a check implementation must explicitly compile against its chosen Groovy compiler even though
`klum-cast-spi` does not publish a Groovy-version selector.

## 3. Return diagnostics, not exceptions

The example returns an empty list when the method name starts with the requested prefix. Otherwise it returns a
`Diagnostic` with:

- stable code `example.method-name.invalid-prefix`;
- an actionable default message;
- the invalid `MethodNode` as the primary source position;
- named `method` and `prefix` arguments;
- the validated annotation use as a related location.

It also declares the code and argument names through `DiagnosticDefinition`, allowing a composed validation annotation
to provide a checked message override later. Codes and argument names are compatibility contracts; default prose and
compiler rendering are not.

Return diagnostics only for expected annotation-use violations. Let unexpected exceptions escape: KlumCast classifies
constructor failures, broken bindings, invalid message templates, and execution exceptions as technical failures and
preserves their causes.

## 4. Keep applicability separate

[`MethodTargetFilter`](../../klum-cast-compile/src/test/java/com/blackbuild/klum/cast/docs/onboarding/MethodTargetFilter.java)
implements the stateless `ApplicabilityFilter` SPI. A `false` result means the binding is not applicable; it is not a
successful check. All filters declared on one binding are conjunctive. Name-bound declarations can use `@Filter`
members; typed declarations list filter classes in `@CheckBinding(filters = ...)`.

## 5. Verify passing and failing uses

[`RoleBasedOnboardingDocumentaryTest`](../../klum-cast-compile/src/test/groovy/com/blackbuild/klum/cast/docs/onboarding/RoleBasedOnboardingDocumentaryTest.groovy)
compiles a passing use and asserts the failing diagnostic's code, message, filename, line, and source text. The repository
recompiles this same test source independently in Groovy 3, 4, and 5; there are no generation-specific copies.

Checks and validation annotations may be written in Java or Groovy. Their targets must appear in Groovy source for
KlumCast to scan them.

## Next steps

Continue as a [check aggregator](check-aggregator.md) to give this check domain language, or review the public
[`spi` package](../../klum-cast-spi/src/main/java/com/blackbuild/klum/cast/spi).
