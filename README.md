KlumCast
========

Check those annotations with style!

# Overview

KlumCast is validator for annotation placement for Groovy based schemas. It allows to conveniently validate AST driving annotations before the actual transformation is performed and thus helps keep the transformation code clean.

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

In order to use KlumCast on an annotation, the validation to check needs to be annotated with `@KlumCastValidated` as well as the actual validation annotations as in the example above

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

Note that the validations to be annotated can be implemented in Java or Groovy, but place where the annotation is used needs to be Groovy, since the AST transformation is only applied to Groovy code.

In order for the validation to be applied, the KlumCast library needs to be present during compile time of schema, but it does not need to be present during the compilation/loading of the model (i.e. the compileOnly in case of Gradle or optional in case of Maven).

# Validation annotations

The following validation annotations are available:

## @ClassNeedsAnnotation

Checks if the class the annotated element is part of is annotated with the given annotation.

## @NumberOfParameters

Checks that the annotated methods has exactly the given number of parameters. Note that if the annotated element is no method, the validation is ignored.

## @AllowedMembers

Can be used multiple times for various targets. Checks for the matching target that only the given members of the annotation are set or that some annotations are forbidden.

# Custom validations

Custom validations consist of two elements, the annotation and a validator class. The annotation needs to be annotated with `@KlumCastValidated` and the validator class needs to extends `KlumCastCheck`.  

## The annotation

The annotation needs to be of Runtime retention and target only Annotations. It is annotated with `@KlumCastValidator` which points to the classname of the validator class.

```groovy
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("my.MyValidator")
@interface MyAnnotation {
    String value()
}
```

## the validator class

The validator class extends `KlumCastCheck` and have the annotation as Type-Parameter. The `doCheck` method receives the AST node of the annotated element and the annotation itself. Any exception thrown inside this method is converted to a Compilation Error.

```groovy
class MyValidator extends KlumCastCheck<MyAnnotation> {
    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
        if (target.getText().equalsIgnoreCase("foo")) {
            throw new IllegalStateException("must not be placed on Foos")
        }
    }
}
```

## Check as inner class

For convenience, the validator class can be implemented as inner class to the annotation itself. In that case, the alternative syntax using `type` instead of `value` is useful.

```groovy
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator(type = MyAnnotation.Check.class)
@interface MyAnnotation {
    String value()

    class Check extends KlumCastCheck<MyAnnotation> {
        @Override
        protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
            if (target.getText().equalsIgnoreCase("foo")) {
                throw new IllegalStateException("must not be placed on Foos")
            }
        }
    }
    
}
```