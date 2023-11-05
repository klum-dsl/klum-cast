/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Stephan Pauxberger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
