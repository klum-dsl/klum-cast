# Check user: validate a consumer annotation

A check user applies reusable validation annotations to a consumer-defined annotation and activates KlumCast for the
Groovy compilation that uses it. This journey turns the aggregator's `@SetterLike` into `@DomainSetter`.

## Prerequisites and dependencies

Use Java 17 and Groovy 3, 4, or 5. Add the artifact containing the validated annotation normally, and put
`klum-cast-compile` on the Groovy compilation classpath:

```groovy
dependencies {
    implementation project(":domain-annotations")
    compileOnly "com.blackbuild.klum.cast:klum-cast-compile:$klumCastVersion"
}
```

If this module declares `@DomainSetter` itself, it also needs
`implementation "com.blackbuild.klum.cast:klum-cast-annotations:$klumCastVersion"` and the artifact containing
`@SetterLike`. Maven uses the default compile scope for annotation artifacts and `provided` for
`klum-cast-compile`. The compiler artifact is the activation switch; annotations and SPI alone do not run validation.
See the [shared dependency table](README.md#dependencies-by-role).

## 1. Declare a validated annotation

The executable
[`@DomainSetter`](../../klum-cast-compile/src/test/java/com/blackbuild/klum/cast/docs/onboarding/DomainSetter.java)
targets methods, has runtime retention, is marked `@KlumCastValidated`, and applies the reusable `@SetterLike` validation
annotation. `@DomainSetter` is the validated annotation; `@SetterLike` is a validation annotation. The Java modules that
contain them are artifacts, not user roles.

## 2. Use it in Groovy source

The valid source in
[`RoleBasedOnboardingDocumentaryTest`](../../klum-cast-compile/src/test/groovy/com/blackbuild/klum/cast/docs/onboarding/RoleBasedOnboardingDocumentaryTest.groovy)
has the required name, return type, and parameter count:

```groovy
class ValidService {
    @DomainSetter
    void setName(String name) {}
}
```

This use violates the custom name constraint while leaving the built-in signature constraints valid:

```groovy
class InvalidService {
    @DomainSetter
    void rename(String name) {}
}
```

Compilation fails with an error like:

```text
InvalidSetterExample.groovy: 7: [example.method-name.invalid-prefix] Method rename must start with 'set'
    @DomainSetter
    ^
```

The test asserts the stable code, actionable message, filename, target line, and annotated source text. The diagnostic is
positioned on the invalid method's annotation because the check supplied that method AST node as its primary location.
A broken check or binding would instead take the technical-failure path with its cause preserved.

## 3. Order dependent transformations later

Compiler activation runs KlumCast during `SEMANTIC_ANALYSIS` and scans annotation uses on classes, constructors, methods,
parameters, and fields declared in each Groovy source unit. If your annotation drives another AST transformation, perform
its main mutation in `CANONICALIZATION` or later so validation finishes first. KlumCast does not guarantee ordering
against a second semantic-analysis transformation.

Annotations and checks may be declared in Java or Groovy, but the annotated target must be Groovy source for this global
Groovy transformation to scan it. Validation ends with compilation: KlumCast does not add runtime model-validation
behavior and `klum-cast-compile` should not be carried onto unrelated runtime or model-compilation classpaths.

## Next steps

Return to the [shared orientation](README.md), browse the
[built-in validation annotations](../../klum-cast-annotations/src/main/java/com/blackbuild/klum/cast/checks), or use the
[0.4 migration guide](../migration/0.4.md) when upgrading a pre-0.4 integration.
