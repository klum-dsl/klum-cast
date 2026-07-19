# Check aggregator: assemble domain validations

A check aggregator combines existing validation annotations into a reusable, domain-oriented validation annotation. It
does not imply a new artifact, dependency coordinate, person, or `Check` implementation.

This journey combines two built-in validations with the writer journey's custom validation as `@SetterLike`.

## Prerequisites and dependencies

The executable journey uses the typed path in the
[project-module and binding matrix](README.md#example-project-modules-and-binding-choice). This page configures
`:domain-annotations`, which owns both `@SetterLike` and `@DomainSetter`:

```groovy
dependencies {
    api "com.blackbuild.klum.cast:klum-cast-annotations:$klumCastVersion"
    api project(":custom-checks")
}
```

The `api project(":custom-checks")` edge is specific to this typed example: that project owns both
`@MethodNameStartsWith` and its implementation and exports its SPI dependency. Do not add SPI separately here. For a
split name-bound check, replace that edge with `api project(":custom-check-metadata")`; the implementation belongs on the
validated Groovy compilation classpath, not on this annotation-composition module.

These `api` edges make the runtime-retained annotations in `@SetterLike` and `@DomainSetter` available when `:consumer`
compiles against them. Use Maven's default transitive compile scope. Add `klum-cast-compile` only to Groovy compilations
that should run validation. See the [shared dependency table](README.md#dependencies-by-role).

## Compose the setter validation

The executable
[`@SetterLike`](../../klum-cast-compile/src/test/java/com/blackbuild/klum/cast/docs/onboarding/SetterLike.java)
is a validation annotation with runtime retention and `ANNOTATION_TYPE` target. It is marked `@KlumCastValidated` and
contributes:

- built-in `@NumberOfParameters(1)`;
- built-in `@NeedsReturnType(Void.class)`;
- custom `@MethodNameStartsWith("set")`.

Every applicable contributed binding is evaluated. This ordinary aggregation is the way to give a recurring group of
constraints one domain name; it is not a new boolean-composition API and does not require another check class.

Validation annotations may also be placed on members of a validated annotation. In that case the immutable
`CheckContext.memberName` identifies the member, and target filters such as the built-in `@OnlyOn`/`@NotOn` constraints
can limit applicability. Applicability is independent from success: a filtered-out binding contributes neither a pass
nor a failure.

## Delivered boolean composition

`@OneCheckMustMatch` is the only delivered 0.4 boolean strategy. It provides OR over validation-annotation branches:

```java
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch(message = "Use one parameter or return void")
@NumberOfParameters(1)
@NeedsReturnType(Void.class)
public @interface OneParameterOrVoid {}
```

Branches are evaluated eagerly in annotation-type-name order. A filtered branch is not a match. OR passes when any
applicable branch passes, fails when all applicable branches fail, and is not applicable when it has no applicable
branches. Failure retains every branch diagnostic and adds the source-positioned
`klum-cast.composition.or.no-match` summary. A branch technical failure remains a technical failure.

The executable
[`OrCompositionTest`](../../klum-cast-compile/src/test/groovy/com/blackbuild/klum/cast/checks/OrCompositionTest.groovy)
covers passing, all-failing, filtered, nested, zero-branch, diagnostic-ordering, and technical-failure behavior. New
source declares branches directly on the composed annotation. Annotation-valued members on `@OneCheckMustMatch` remain
only as a deprecated binary migration bridge for old declarations; recompile them using direct branch annotations.

There is no public AND, XOR, or conditional composition syntax in 0.4. Do not infer one from the accepted outcome model.

## Java and Groovy declaration shape

Validation annotations and checks may be authored in Java or Groovy. In either language, a composition may nest a branch
annotation type inside its parent when that branch has no independent meaning. The executable
[`NestedOrFixture`](../../klum-cast-compile/src/test/java/com/blackbuild/klum/cast/checks/NestedOrFixture.java) demonstrates
the Java shape, while
[`RoleBasedOnboardingDocumentaryTest`](../../klum-cast-compile/src/test/groovy/com/blackbuild/klum/cast/docs/onboarding/RoleBasedOnboardingDocumentaryTest.groovy)
compiles a Groovy-authored nested branch and verifies both matching and all-failing OR outcomes. Use a top-level annotation
in either language whenever the branch is reused or is independently meaningful.

Nesting is only declaration organization: inner branches are not annotations that check users apply directly, and it
does not change OR outcomes.

## Next steps

Continue as a [check user](check-user.md) to apply `@SetterLike` through a validated annotation. Browse the
[built-in validation annotations](../../klum-cast-annotations/src/main/java/com/blackbuild/klum/cast/checks) for reusable
constraints.
