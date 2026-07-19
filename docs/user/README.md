# Dependencies, activation, and shared concepts

> **Interim location:** `docs/user/` is the repository's preliminary user-facing documentation subtree. Its content is
> intentionally independent of a generator, hosted site, or versioned publication layout so it can move later.

KlumCast is a compile-time annotation-use validation library for Groovy compilation. A check writer supplies reusable
logic, a check aggregator gives checks domain language through composed validation annotations, and a check user applies
those validations to an annotation and activates the compiler integration. These roles do not imply three people, three
modules, or three KlumCast artifacts.

Choose a journey:

- [Check writer](check-writer.md)
- [Check aggregator](check-aggregator.md)
- [Check user](check-user.md)

All examples form one small `@DomainSetter` journey and are compiled by
[`RoleBasedOnboardingDocumentaryTest`](../../klum-cast-compile/src/test/groovy/com/blackbuild/klum/cast/docs/onboarding/RoleBasedOnboardingDocumentaryTest.groovy)
in every supported Groovy lane.

## Example project modules

The three roles can be combined in one module. To make ownership and dependency direction unambiguous, this journey uses
three example project modules:

| Example module | Owns | Direct dependencies |
|---|---|---|
| `:custom-checks` | `@MethodNameStartsWith`, its check, and its filter | `compileOnly` KlumCast SPI and the matching Groovy compiler |
| `:domain-annotations` | composed `@SetterLike` and validated `@DomainSetter` | `api` KlumCast annotations and `api project(":custom-checks")` |
| `:consumer` | Groovy targets that use `@DomainSetter` | `implementation project(":domain-annotations")` and `compileOnly` KlumCast compiler activation |

The `api` scopes assume Gradle's `java-library` plugin. They are intentional because the dependency annotations are
recorded in public, runtime-retained annotation metadata that a downstream Groovy compilation must inspect. Maven's
equivalent is the default transitive compile scope. If you combine modules, collapse the corresponding project edge but
keep direct dependencies on the KlumCast APIs used by that module's own source.

## The compile-time model

A **validated annotation** is a consumer-defined annotation whose uses KlumCast validates. A **validation annotation** is
a reusable declarative annotation that contributes one or more checks to that validated annotation. A validation
annotation may itself be composed from other validation annotations.

When the validated annotation is used in Groovy source, its **target** is the annotated Groovy AST node. Each applicable
**check** receives an immutable **check context** describing the validated annotation, target, validation annotation,
optional member, binding metadata, and composition path. A **filter** decides whether a binding applies to that context;
being filtered out is not the same as passing.

An expected constraint violation is a **diagnostic**: immutable data with a stable code, message, primary source node,
optional named arguments, and optional related nodes. A broken binding, invalid message template, constructor problem, or
exception from a new check is a **technical failure**. Technical failures retain their causes and must not be presented as
ordinary bad annotation usage.

## Dependencies by role

KlumCast 0.4 requires Java 17 and supports Groovy 3, 4, and 5.

| Artifact or dependency | Gradle scope | Maven scope | Add it when |
|---|---|---|---|
| `com.blackbuild.klum.cast:klum-cast-annotations` | `implementation` or `api` | default (`compile`) | Source declares or uses KlumCast metadata or built-in validation annotations. Use `api` when that metadata is exposed to downstream modules. It has no Groovy dependency. |
| `com.blackbuild.klum.cast:klum-cast-spi` | `compileOnly` | `provided` | Source implements a check/filter or uses typed `@CheckBinding`. |
| Matching Groovy compiler | `compileOnly` | `provided` | A check/filter implementation compiles against the AST types exposed by the SPI. |
| `com.blackbuild.klum.cast:klum-cast-compile` | `compileOnly` | `provided` | A Groovy compilation should run validation. This dependency is the activation switch. |

For a module that writes a custom check and also compiles validated Groovy targets:

```groovy
dependencies {
    implementation "com.blackbuild.klum.cast:klum-cast-annotations:$klumCastVersion"
    compileOnly "com.blackbuild.klum.cast:klum-cast-spi:$klumCastVersion"
    compileOnly "org.apache.groovy:groovy:$groovyVersion" // Groovy 4 or 5
    compileOnly "com.blackbuild.klum.cast:klum-cast-compile:$klumCastVersion"
}
```

Use `org.codehaus.groovy:groovy` for Groovy 3 and `org.apache.groovy:groovy` for Groovy 4 or 5. The SPI publishes no
transitive Groovy selector, so a check writer must choose the version matching the consuming compiler. An aggregator that
only composes existing validation annotations normally needs `klum-cast-annotations` plus the artifact containing any
custom validation annotation. A check user needs its validated-annotation artifact and `klum-cast-compile` on the Groovy
compilation classpath.

The Maven equivalent keeps annotations at the default compile scope and uses `provided` for SPI, the matching Groovy
compiler, and compiler activation:

```xml
<dependency>
  <groupId>com.blackbuild.klum.cast</groupId>
  <artifactId>klum-cast-compile</artifactId>
  <version>${klumCastVersion}</version>
  <scope>provided</scope>
</dependency>
```

`klum-cast-compile` supplies the annotations and SPI artifacts transitively to the compiler invocation, but source modules
should still declare the direct artifact that owns the API they compile against.

## Activation, scan scope, and ordering

Putting `klum-cast-compile` on a Groovy compilation classpath activates its global AST transformation through Java
service loading. Removing it disables KlumCast; annotations and SPI alone are inert.

Validation runs during Groovy `SEMANTIC_ANALYSIS`. For every Groovy source unit, KlumCast scans annotation uses on its
declared classes, constructors, methods, parameters, and fields. It follows validation annotations recursively. Groovy
properties are not visited separately because their field or accessor annotations are already in the scan.

A consumer transformation that depends on successful validation must perform its main mutation during
`CANONICALIZATION` or later. Ordering against another `SEMANTIC_ANALYSIS` transformation is not guaranteed; same-phase
coordination needs an explicit mechanism.

Annotations, checks, filters, and consuming transformations may be authored in Java or Groovy. The validated annotation
must be used in Groovy source for KlumCast's Groovy transformation to see its target. This validation adds no runtime
model behavior.

## Continue

Follow the [check writer](check-writer.md), [check aggregator](check-aggregator.md), or
[check user](check-user.md) journey. Existing 0.3 integrations should also read the
[0.4 migration guide](../migration/0.4.md).
