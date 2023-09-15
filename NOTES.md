Wording
=======

## annotation under check

The annotation whose placement should be validated, i.e. @Field or @DSL. These annotations are not part of KlumCast.

Target annotation can have the KlumCastTransformation, but this is not required (for instance, @Field annotations only make sense if on a field in a class annotated with @DSL, so it is sufficient to put the validating annotation to DSL
annotation).

## Annotation Target

The annotated node on which annotation under check is placed. This might or might not be relevant for the checks

## The marker annotation 

`KlumCastValidated` marks the annotation under check as annotation that will be validated by the framework.

## The Jury annotations

Annotations that placed on the annotation under check and that will be used to validate the annotation under check. These annotations can be part of KlumCast but can also be externally provided.

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@KlumCastValidated
@ClassNeedsAnnotation(DSL.class)
@AllowedMembers(on = ElementType.FIELD, members = {"id"})
@AllowedMembers(on = ElementType.METHOD, members = {"cast"})
public @interface Field {
    String id();
    Class<?> cast();
}





```
