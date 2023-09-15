package com.blackbuild.klum.cast.helpers;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RepeatableAnnotationsSupport {

    private RepeatableAnnotationsSupport() {}

    /**
     * Returns a stream if his annotation. If this annotation is the container of a Repeatable annotation, the stream
     * contains all annotations of the repeatable type instead.
     * @param annotation the annotation to unwrap
     * @param filter
     * @return
     */
    public static Stream<Annotation> unwrapAnnotations(Annotation annotation, Predicate<Class<? extends Annotation>> filter) {
        if (filter.test(annotation.annotationType()))
            return Stream.of(annotation); // if it is a validator, it would handle inner annotations itself

        Optional<Method> values = getSingleValuesMemberMethod(annotation);
        if (!values.isPresent())
            return Stream.empty();

        Class<?> returnType = values.get().getReturnType();

        if (!returnType.isArray())
            return Stream.empty();
        if (!returnType.getComponentType().isAnnotation())
            return Stream.empty();


        if (!returnType.getComponentType().isAnnotationPresent(Repeatable.class))
            return Stream.empty();


        if (!returnType.getComponentType().getAnnotation(Repeatable.class).value().equals(annotation.annotationType()))
            return Stream.empty();

        //noinspection unchecked
        if (!filter.test((Class<? extends Annotation>) returnType.getComponentType()))
            return Stream.empty();

        return Arrays.stream((Annotation[]) InvokerHelper.invokeMethod(annotation, "value", new Object[0]));
    }

    @NotNull
    private static Optional<Method> getSingleValuesMemberMethod(Annotation annotation) {
        Method[] methods = annotation.annotationType().getDeclaredMethods();
        if (methods.length != 1 || !methods[0].getName().equals("value"))
            return Optional.empty();
        return Optional.of(methods[0]);
    }


}
