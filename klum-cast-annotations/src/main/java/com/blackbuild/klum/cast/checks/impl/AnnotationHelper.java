package com.blackbuild.klum.cast.checks.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AnnotationHelper {
    private AnnotationHelper() {}


    public static <T extends Annotation> Stream<Object> getValuesOfMembersAnnotatedWith(Annotation annotation, Class<T> memberAnnotationType) {
        return getValuesOfMembersAnnotatedWith(annotation, memberAnnotationType, it -> true);
    }

    public static <T extends Annotation> Stream<Object> getValuesOfMembersAnnotatedWith(Annotation annotation, Class<T> memberAnnotationType, Predicate<T> filter) {
        return Stream.of(annotation.annotationType().getDeclaredMethods())
                .filter(m -> hasMatchingAnnotation(m, memberAnnotationType, filter))
                .map(m -> invokeMemberMethod(annotation, m));
    }

    private static <T extends Annotation> boolean hasMatchingAnnotation(Method member, Class<T> annotationType, Predicate<T> filter) {
        T memberAnnotation = member.getAnnotation(annotationType);
        return memberAnnotation != null && filter.test(memberAnnotation);
    }

    private static Object invokeMemberMethod(Annotation annotation, Method m) {
        try {
            return m.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
