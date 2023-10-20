KlumCast
========

Check those annotations with style!

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

- Field.members is only valid on Collections and Maps
- Key and Owner annotations are only valid on classes annotated with @DSL
- @Key is only valid on String fields
- @Owner is only valid on fields or on single argument methods
- @DSL.stripSuffix is only valid on non-final classes


# Usage

## Basic usage: use provided validations

### Dependencies

KlumCast ist split into two modules: klum-cast-annotations and klum-cast-compile. The annotations module contains the preconfigured validations as well as the base class for custom validations. The compile module contains the AST-Transformation that is applied to the schema.

The annotations module must be present at runtime (since the validation transformation needs access to the compiled classes of the annotations). However,
the compile module only needs to be present during the compilation of the schema, but not during the compilation of the model itself, i.e. it should be a compileOnly dependency in Gradle or an optional dependency in Maven. Since the compile module contains a global AST-Transformation, it would have a slight impact on the compilation time of the model, so it should be avoided to have it present during the compilation of the model.

If a project is split into the usual three modules (annotations, ast and runtime), the klumcast-annotations module should be a regular dependency of the annotations module (`compile` for Maven, `api` or `implementation` for Gradle), while the klumcast-compile module should be a compileOnly dependency of the ast module (`optional` for Maven, `compileOnly` for Gradle). See KlumAST for an example.

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

## Nested annotations

Validation annotations can themselves be aggregations of multiple annotations. This is useful if a combination of validations is used multiple times. Or to give 
an annotation a domain specific name.

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

Custom validation can be declared using the `@KlumCastValidator` annotation. This annotation points to the class implementing the validation. The class must extend `KlumCastCheck` and have the actual validator annotation as type parameter. This allows for easy parametrizing of the validator.

So usually, a custom validation consist of two elements, the control annotation (which is eventually placed on the target annotation to mark it as validated) and a validator class. The control annotation needs to be annotated with `@KlumCastValidator` and the validator class needs to extend `KlumCastCheck`.

## The control annotation

The control annotation needs to be of Runtime retention and target only Annotations. It is annotated with `@KlumCastValidator` which points to the classname or type of the validator class. The annotation should have a clear name and contain further members to parametrize the validator. 

```groovy
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("my.NameMustMatchCheck")
@interface NameMustMatch {
    String value()
}
```

## The validator class

The validator class extends `KlumCastCheck` and have the annotation as Type-Parameter. The actual check is usually implemented by the `doCheck` method, which has access to the following information:

- the control annotation (as annotation object, `NameMustMatch` in the example above (if the KlumCastValidatior annotation is placed directly on the annotation to be validated, this can be null)
- the `KlumCastValidator` annotation (which can have an additional String array to further parametrize the validator), in the example above this would be in instance of `@KlumCastValidator("my.NameMustMatch")`
- the annotation target, i.e. the annotated element itself as an `AnnotatedNode` instance
- the annotation to validate, i.e. the annotation that is annotated with the control annotation, as an `AnnotationNode` instance
- if the control annotation is placed on a member of the annotation to validate, that member's name as a String

The last to elements are Groovy-Compiler AST-Nodes.

The `doCheck` should perform necessary validations and eventually either return or throw an exception. If an exception is thrown, it is converted to a compilation error.

```groovy
class NameMustMatchCheck extends KlumCastCheck<NameMustMatch> {
    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
        if (!target.getText().startsWith(controlAnnotation.value())) {
            throw new IllegalStateException("Target element must start with ${controlAnnotation.value()}")
        }
    }
}
```

Additionally, the method `isValidFor(AnnotatedNode target)` can be overridden quickly skip the check if necessary.

## Check as inner class

For convenience, the validator class can be implemented as inner class to the annotation itself. In that case, the alternative syntax using `type` instead of `value` is useful.

```groovy
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator(type = NeedsSomething.Check.class)
@interface NeedsSomething
    String value()

    class Check extends KlumCastCheck<NeedsSomething> {
        @Override
        protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
            if (target.getText().equalsIgnoreCase("foo")) {
                throw new IllegalStateException("must not be placed on Foos")
            }
        }
    }
    
}
```

Note that annotations and KlumCastValidator annotations can be freely mixed, i.e. a control annotation can have multiple KlumCastValidator annotations as well as multiple control annotations (which themselves can have multiple KlumCastValidator annotations or even more control annotations). Just remember that annotations neither having
`KlumCastValidated` nor `KlumCastValidator` are ignored by the AST-Transformation.