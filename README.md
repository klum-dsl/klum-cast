KlumCast
========

Check those annotations with style!

## 0.4 custom-check migration

KlumCast 0.4 separates declarative metadata, the custom-check SPI, and compiler activation into three artifacts:

- `com.blackbuild.klum.cast:klum-cast-annotations` contains annotations and name-based bindings. It has no Groovy
  compiler dependency.
- `com.blackbuild.klum.cast:klum-cast-spi` contains `Check`, `CheckContext`, `Diagnostic`,
  `ApplicabilityFilter`, and the typed `@CheckBinding` annotation. Custom-check authors add this dependency explicitly;
  it deliberately exposes Groovy compiler AST types but does not publish a transitive Groovy-version selector.
- `com.blackbuild.klum.cast:klum-cast-compile` activates the service-loaded semantic-analysis transformation and
  supplies both sibling artifacts transitively.

KlumCast 0.4 requires Java 17 and supports Groovy 3, 4, and 5. The production artifacts are compiled once against Groovy
3 and are verified as one artifact set in all three generations. Groovy 2.4 and Java 11 remain supported only on the 0.3
line. Projects that implement custom checks must declare the Groovy compiler matching their own lane in addition to
`klum-cast-spi`:

| Groovy | Compiler coordinate | Automatic module name |
|---:|---|---|
| 3 | `org.codehaus.groovy:groovy` | `org.codehaus.groovy` |
| 4 | `org.apache.groovy:groovy` | `org.apache.groovy` |
| 5 | `org.apache.groovy:groovy` | `org.apache.groovy` |

```groovy
dependencies {
    implementation "com.blackbuild.klum.cast:klum-cast-annotations:$klumCastVersion"
    compileOnly "com.blackbuild.klum.cast:klum-cast-spi:$klumCastVersion"
    compileOnly "com.blackbuild.klum.cast:klum-cast-compile:$klumCastVersion"
    compileOnly "org.apache.groovy:groovy:$groovyVersion" // Groovy 4 or 5; use org.codehaus.groovy for Groovy 3
}
```

The Maven equivalent uses `provided` scope for SPI, compiler activation, and the selected Groovy compiler:

```xml
<dependency>
  <groupId>com.blackbuild.klum.cast</groupId>
  <artifactId>klum-cast-spi</artifactId>
  <version>${klumCastVersion}</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>org.codehaus.groovy</groupId><!-- org.apache.groovy for Groovy 4 or 5 -->
  <artifactId>groovy</artifactId>
  <version>${groovyVersion}</version>
  <scope>provided</scope>
</dependency>
```

New checks implement `Check` and return zero or more `Diagnostic` values from `check(CheckContext)`. A context is
immutable and provides the validated annotation, target, applicable control annotation, member name, binding metadata,
and composition path. Checks and `ApplicabilityFilter` implementations require an accessible no-argument constructor
and must be stateless: the compiler may reuse an instance during one compilation and never retains it across
compilations. A returned diagnostic is an expected annotation-use violation; an exception is a technical failure and
retains its cause.

Use `@CheckBinding(MyAnnotation.NestedCheck.class)` for a strongly typed co-located or nested binding. Use
`@KlumCastValidator("example.SplitCheck")` for a name binding when a metadata module must compile separately from an
implementation module. Typed filters are declared with `@CheckBinding(filters = ...)`; name-bound filters remain
available through `@Filter` annotation members. All filters on one binding are conjunctive, and a filtered-out binding
is not a successful check result.

`com.blackbuild.klum.cast.checks.impl.KlumCastCheck` remains only as a deprecated 0.4 migration adapter in
`klum-cast-spi`. Move new code to `Check`; do not rely on its inherited mutable fields. The legacy
`KlumCastValidator.type()` member is a deprecated raw `Class<?>` bridge and loses source-level type checking when
consumers recompile. Replace legacy `Filter.Function` implementations with `ApplicabilityFilter`. These migration
bridges will be removed for 1.0.

## Structured diagnostic migration

New checks return immutable `Diagnostic` values. Each diagnostic has a stable check-owned code, default message, primary
Groovy AST node, optional named arguments, and optional related nodes. A check that supports message overrides declares
its stable code and argument names through `getDiagnosticDefinitions()`. Codes and argument names are forward-compatible
contracts; default prose and Groovy compiler rendering are not.

Validation annotations can override a check's default prose with `@DiagnosticMessages`, for example
`@DiagnosticMessage(code = "example.invalid-name", template = "Name {name} is reserved")`. Placeholders are named,
must be declared by that code, and use `{{` and `}}` for literal braces. In a composed validation annotation, the nearest
applicable override wins. Unknown codes, unknown placeholder names, malformed templates, and missing arguments are
technical configuration failures, not invalid annotation uses.

## Boolean composition migration

`@OneCheckMustMatch` is the delivered 0.4 OR strategy. Declare its branch validation annotations directly on the
composed validation annotation; no annotation-valued branch members or branch reflection are used by new source.
One composed annotation type declares one composition node. Applying a validated annotation to a target starts at its
outermost node; nested branch annotations are declaration details and are never annotations that users apply to targets.

```groovy
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch(message = 'A closure needs a return type or one parameter')
@NumberOfParameters(1)
@NeedsReturnType(Closure)
@interface ClosureOrSingleParameter {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@ClosureOrSingleParameter
@interface CheckedClosure {}
```

When a branch has no value outside its parent composition, a Java-authored composition may declare that branch annotation
type inside the parent. This keeps the nested composition visibly together without changing its outcome or making its
inner branches user-facing:

```java
@KlumCastValidated
@OneCheckMustMatch(message = "Choose the outer check or one inner check")
@OuterOr.InnerOr
@OtherBranch
public @interface OuterOr {
    @KlumCastValidated
    @OneCheckMustMatch
    @FirstInnerBranch
    @SecondInnerBranch
    public @interface InnerOr {}
}
```

Use a top-level annotation type when a branch is reused by another composition or as an independently named validation
annotation.

The engine evaluates branches eagerly in ascending annotation-type-name order. A filtered branch is not a match; OR
passes when any applicable branch passes, fails when all applicable branches fail, and is not applicable when it has no
applicable branches (including zero branches). On failure, every failing branch diagnostic is retained in order and an
engine-owned `klum-cast.composition.or.no-match` summary is added. `message` changes only that summary, so it cannot
hide branch positions, codes, causes, or provenance. Unexpected branch exceptions remain technical failures.

Existing `@OneCheckMustMatch` declarations with annotation-valued branch members continue to work as a deprecated binary
migration bridge. Recompile them using direct branch annotations. AND and XOR have documented semantics but no public
0.4 syntax yet; conditional composition is outside this release slice.

KlumCast emits one native Groovy compiler error per returned diagnostic, including its code and primary-node position.
Related locations plus binding and composition-path information are rendered as supplementary context. Returned
diagnostics are emitted deterministically in source and binding order.

`ValidationException` and `KlumCastCheck.ErrorMessage` remain deprecated adapter-only migration types. A legacy
`ValidationException` becomes one diagnostic using the adapter's check-class code. For source compatibility, the
deprecated adapter retains its historical runtime-exception-to-message behavior; new `Check` implementations must return
expected failures as diagnostics and let unexpected exceptions follow the technical-failure path with their causes
preserved. Boolean branch aggregation and outcome semantics remain deferred to issue #22.

## Java modules and classpaths

The 0.4 artifacts retain classpath support and publish stable automatic module names:

- `com.blackbuild.klum.cast.annotations`
- `com.blackbuild.klum.cast.spi`
- `com.blackbuild.klum.cast.compiler`

The compiler artifact continues to discover its global Groovy AST transformation through its service descriptor when it
is on the compilation classpath. Its packages are intentionally separate from annotations and SPI; consumers must not
rely on compiler implementation packages as API.

KlumCast does not currently publish explicit `module-info.java` descriptors. The repository-owned Java 17 fixture proves
classpath and module-path compiler activation for Groovy 3, 4, and 5. Named consumers must require
`org.codehaus.groovy` for Groovy 3 or `org.apache.groovy` for Groovy 4 and 5, alongside the KlumCast automatic modules they
use.

# Overview

KlumCast is validator for annotation placement for Groovy based schemas. It allows to conveniently validate AST driving annotations before the actual transformation is performed and thus helps keep the transformation code clean.

## For whom is KlumCast?

KlumCast is for you if you are writing a Groovy AST transformation that is driven by annotations.

It is also relevant for extenders of an existing framework, for example a Layer3 approach with KlumDSL can hugely benefit from the usage of KlumCast.

## Basic example

Lets consider a an annotation that creates convenience methods to fill add entries to collection fields:

```groovy
class MyClass {
    @AutoAdd
    List<String> names
}
```

Based on this setup, the following code would be generated:

```groovy
void addName(String name) {
    if (names == null) names = []
    names.add(name)
}

void addNames(Collection<String> names) {
    if (this.names == null) this.names = []
    this.names.addAll(names)
}

void addNames(String... names) {
    if (this.names == null) this.names = []
    this.names.addAll(names)
}
```

A Groovy AST-Transformation generating the above code would be easy to implement. 
However, before actually creating the methods, it must be assured, that the field to be transformed is actually a collection. Otherwise, compilation errors or worse, runtime errors would occur.

Making this check is easy to implement, but it would clutter the transformation code as well as the javadoc of the annotation. KlumCast allows to move this check to a separate class, which is then called by the AST-Transformation. It also includes a couple of preconfigured validations that can be used out of the box.

Note that annotation, checks and transformation can be written in Java as well as in Groovy, only the actual model schema - and of course the model itself - needs to be written in Groovy.

```java
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NeedsType(Collection)
@interface AutoAdd {}
```

This is a lot cleaner and easier to understand, as well as less effort in implementing the transformation.


# Quick explainer: models, schemas and AST transformations

Since KlumCast derived from Klum-AST, it is a fitting example to explain the key concepts of KlumCast.

KlumDSL ist a framework to define statically typed and checked static models, i.e. glorified Pojos, by using a short, elegant DSL language, something like:

```groovy
Home.Create.With {
    entry("main")
    entry("back")
    
    baseStations {
        hue(ip: "100.1.2.3", tokenEnv: "HUE_TOKEN")
        homematic("100.1.2.3", authEnv: "HOMEMATIC_AUTH")
    }
    
    livingRoom {
        light("ceiling", hueId: "1")
        light("table", hueId: "2")
        light("floor", hueId: "3")
        rollershutter("window", type: HmIP.BROLL, hmId: "abcdef")
    }
}
```

This model code returns an instance of our example Home, which can be consumed and used by Java and Groovy code using convenient getters or GPath notations:

`myHome.livingRoom.lights.ceiling`

While this barely scratches the surface of KlumAST (see [Klum-AST](https://github.com/klum-dsl/klum-ast) for more details), it is sufficient to explain the key concepts of KlumCast. The most important information to take is that KlumAST makes heavy use of annotations to generate type safe methods to create the model. The schema (i.e. the defining classes, think XML and XML-Schema) of the above model could be something like:

```groovy
@DSL
class Home {
    Map<String, BaseStation> baseStations
    Map<String, Room> rooms
    @Field(members = "entry")
    Map<String, Entry> entries
}

@DSL(stripSuffix = "Base")
abstract class BaseStation {
    @Key String key
    @Owner Home
}

@DSL
class HueBase extends BaseStation {
    String ip
    String tokenEnv
}
```

The Annotations in the above example all have to follow additional placement rules that need to be checked by the AST-Transformation consuming them:

- `@Field.members` is only valid on Collections and Maps
- `@Key` and `@Owner` annotations are only valid on classes annotated with `@DSL`
- `@Key` is only valid on String fields
- `@Owner` is only valid on fields or on single argument methods
- `@DSL.stripSuffix` is only valid on non-final classes


# Usage

## Basic usage: use provided validations

### Dependencies

KlumCast is split into three artifacts. `klum-cast-annotations` contains lightweight metadata and built-in validation
annotations, `klum-cast-spi` contains custom-check contracts, and `klum-cast-compile` contains and activates the global
AST transformation.

The annotations module must be present at runtime (since the validation transformation needs access to the compiled classes of the annotations). However,
the compile module only needs to be present during the compilation of the schema, but not during the compilation of the model itself, i.e. it should be a compileOnly dependency in Gradle or an optional dependency in Maven. Since the compile module contains a global AST-Transformation, it would have a slight impact on the compilation time of the model, so it should be avoided to have it present during the compilation of the model.

If a project is split into the usual annotations, AST, and runtime modules, `klum-cast-annotations` should be a regular
dependency of the annotations module, while `klum-cast-compile` should be a compile-only/optional dependency of the AST
module. Custom-check implementations additionally declare `klum-cast-spi` and the matching Groovy compiler coordinate;
neither dependency is selected transitively by the SPI artifact.

### Declaring validations

In order to use KlumCast on an annotation, the validation to check needs to be annotated with `@KlumCastValidated` as well as the actual validation annotations as in the example above. The `@KlumCastValidated` annotation is only used to mark the annotation as validation target and is not used by the AST-Transformation itself. It allows the validation transformation to easily detect the annotations to be processed.

```java
@Target([ElementType.FIELD, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NumberOfParameters(1)
@ClassNeedsAnnotation(DSL)
public @interface Field {
    String[] members() default {};
}
```

Note that the validations to be annotated can be implemented in Java or Groovy, but the place where the annotation is used needs to be Groovy, since the AST transformation is only applied to Groovy code.

### Member annotations

KlumCast also supports validation annotations placed on members of the validated annotation, which makes syntax more concise.

For example, the `@Validate` annotation of KlumAST can be placed on classes as well as on fields and methods. Depending on the placement, only specific members of the annotation are valid:

```java
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NumberOfParameters(0)
@Documented
public @interface Validate {

    @NotOn({ ElementType.METHOD , ElementType.TYPE })
    Class<? extends Closure> value() default GroovyTruth.class;

    @NotOn({ ElementType.METHOD , ElementType.TYPE })
    String message() default "";
}
```
### Included validations

KlumCast includes a couple of validations that can be used out of the box:

#### @ClassNeedsAnnotation

Checks if the class the annotated element is part of is annotated with the given annotation.

#### @NumberOfParameters

Checks that the annotated methods has exactly the given number of parameters. Note that if the annotated element is no method, the validation is ignored.

#### @MustBeStatic

If the validated annotation is placed on a method, the method must be static. Has no effect if the annotation is placed on any other element.

#### @MutuallyExclusive

Designates that the annotated members are mutually exclusive, i.e. only one of them can be set at the same time.

#### @NeedsReturnType

Checks that the annotated method has the given return type. Note that if the annotated element is no method, the validation is ignored.

#### @ParameterTypes

Forces the parameters of an annotated method to be of the given type. Note that if the annotated element is no method, the validation is ignored.

#### @UniquePerClass

The annotation must only be used once per class.

#### @AlsoNeeds 

The AlsoNeeds annotation is used to specify that a certain annotation member should be used together with one or more specific annotation members. It can only be used on annotation members.

#### @NotTogetherWith

The NotTogetherWith annotation is used to specify that a certain annotation member should not be used together with one or more specific annotation members. It can only be used on annotation members.

#### @NotOn

The NotOn annotation is used to specify that a certain annotation member should not be used on a specific element type. It can only be used on annotation members.

#### @OnlyOn

The OnlyOn annotation is used to specify that a certain annotation member should only be used on a specific element type. It can only be used on annotation members.

#### @NeedsModifiers and @ForbiddenModifiers

Can be use to designate that an annotation can only be placed on targets with or without certain modifiers.

## Nested annotations

Validation annotations can themselves be aggregations of multiple annotations. This is useful if a combination of validations is used multiple times. Or to give 
an annotation a domain-specific name.

Note that Target and Retention annotations are omitted in the examples below for brevity.

```groovy
@KlumCastValidated
@ClassNeedsAnnotation(DSL)
@interface NeedsDslClass {}

@KlumCastValidated
@NumberOfParameters(1)
@NeedsReturnType(Void)
@interface SetterLike {}
```
## Custom validations

New custom validations implement the stateless `Check` interface. A check receives one immutable `CheckContext` and
returns zero or more `Diagnostic` values; it does not inherit mutable invocation fields. The preferred co-located or
nested form uses the type-safe `@CheckBinding`. Use name-based `@KlumCastValidator("fully.qualified.CheckName")` only
when an annotations artifact must compile separately from its check implementation.

Typically a custom validation has a control annotation, placed on the annotation to validate, and a check class. The
custom-check compilation classpath must contain `klum-cast-spi` and the matching Groovy compiler dependency explicitly.

## The control annotation

The control annotation has runtime retention and targets annotation definitions. Bind the check with `@CheckBinding` and
use its members to parameterize the constraint.

```groovy
import com.blackbuild.klum.cast.spi.CheckBinding

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(NameMustMatchCheck)
@interface NameMustMatch {
    String value()
}
```

## The validator class

The check is a concrete class with an accessible no-argument constructor. It receives all invocation data from
`CheckContext`:

- `context.getControlAnnotation(NameMustMatch)` obtains the typed control annotation.
- `context.getTarget()` and `context.getValidatedAnnotation()` are the Groovy AST target and annotation use.
- `context.getMemberName()`, `context.getBinding()`, and `context.getCompositionPath()` provide member, binding, and
  composition information without mutable state.

The check declares stable codes and named arguments for any diagnostic it allows validation annotations to override.
It returns expected annotation-use failures as diagnostics. Unexpected exceptions are technical failures and retain their
causes; do not use them for normal validation failures.

```groovy
import com.blackbuild.klum.cast.spi.Check
import com.blackbuild.klum.cast.spi.CheckContext
import com.blackbuild.klum.cast.spi.Diagnostic
import com.blackbuild.klum.cast.spi.DiagnosticDefinition

class NameMustMatchCheck implements Check {
    static final String INVALID_NAME = 'example.name-must-match.invalid-name'

    @Override
    List<DiagnosticDefinition> getDiagnosticDefinitions() {
        [DiagnosticDefinition.of(INVALID_NAME, 'prefix', 'targetText')]
    }

    @Override
    List<Diagnostic> check(CheckContext context) {
        NameMustMatch control = context.getControlAnnotation(NameMustMatch)
                .orElseThrow { new IllegalStateException('NameMustMatch control annotation is required') }
        String targetText = context.target.text
        if (targetText.startsWith(control.value())) return []

        [new Diagnostic(
                INVALID_NAME,
                "Target element must start with ${control.value()}",
                context.validatedAnnotation,
                [prefix: control.value(), targetText: targetText],
                [context.target]
        )]
    }
}
```

For example, a composed validation annotation can override this default message with
`@DiagnosticMessages([@DiagnosticMessage(code = NameMustMatchCheck.INVALID_NAME, template = "{targetText} must start with {prefix}")])`.

### Filtering Checks

Filters determine whether a binding applies; a filtered-out binding is not a successful check result. Attach typed
filters through `@CheckBinding(filters = [...])`; each implements `ApplicabilityFilter` and receives the same immutable
context. Name-bound filters remain available through `@KlumCastValidator` and `@Filter` members.

```groovy
import com.blackbuild.klum.cast.spi.ApplicabilityFilter
import com.blackbuild.klum.cast.spi.CheckContext
import org.codehaus.groovy.ast.ClassNode

class ClassesOnly implements ApplicabilityFilter {
    @Override
    boolean appliesTo(CheckContext context) {
        context.target instanceof ClassNode
    }
}
```

Built-in target-kind filtering through `KlumCastValidator.validForTargets()` remains available for name-bound
declarations. All filters on one binding are conjunctive.

## Check as inner class

For convenience, a type-safe check can be nested in its control annotation.

```groovy
import com.blackbuild.klum.cast.spi.Check
import com.blackbuild.klum.cast.spi.CheckBinding
import com.blackbuild.klum.cast.spi.CheckContext
import com.blackbuild.klum.cast.spi.Diagnostic
import com.blackbuild.klum.cast.spi.DiagnosticDefinition

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(NeedsSomething.ValidationCheck)
@interface NeedsSomething {
    String value()

    class ValidationCheck implements Check {
        static final String NOT_ON_FOO = 'example.needs-something.not-on-foo'

        @Override
        List<DiagnosticDefinition> getDiagnosticDefinitions() {
            [DiagnosticDefinition.of(NOT_ON_FOO)]
        }

        @Override
        List<Diagnostic> check(CheckContext context) {
            if (!context.target.text.equalsIgnoreCase('foo')) return []
            [new Diagnostic(
                    NOT_ON_FOO,
                    'Must not be placed on Foos',
                    context.validatedAnnotation
            )]
        }
    }
}
```

Validation annotations can be composed and may contain multiple typed or name-based bindings. Annotations without a
KlumCast validation marker or binding are ignored by the transformation. `KlumCastCheck`, its mutable fields, and the
raw `KlumCastValidator.type()` member are deprecated 0.4 migration bridges; do not use them in new examples.
