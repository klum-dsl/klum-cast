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
        if (values.isEmpty())
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
