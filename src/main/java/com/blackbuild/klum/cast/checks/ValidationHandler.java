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
package com.blackbuild.klum.cast.checks;

import com.blackbuild.klum.cast.KlumCastValidator;
import com.blackbuild.klum.cast.helpers.RepeatableAnnotationsSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValidationHandler {
    public enum Status { VALIDATED }

    public static final String METADATA_KEY = ValidationHandler.class.getName();

    public static boolean alreadyValidated(AnnotationNode annotationNode) {
        return annotationNode.getNodeMetaData(METADATA_KEY) != null;
    }

    public static void setStatus(AnnotationNode annotationNode, Status status) {
        annotationNode.setNodeMetaData(METADATA_KEY, status);
    }

    public static List<KlumCastCheck.Error> validateAnnotation(AnnotatedNode target, AnnotationNode validatedAnnotation) {
        if (alreadyValidated(validatedAnnotation))
            return Collections.emptyList();
        List<KlumCastCheck.Error> errors;
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            ClassNode owningClass = target instanceof ClassNode ? (ClassNode) target : target.getDeclaringClass();
            Thread.currentThread().setContextClassLoader(owningClass.getModule().getContext().getClassLoader());
            errors = Arrays.stream(validatedAnnotation.getClassNode().getTypeClass().getAnnotations())
                    .flatMap(a -> RepeatableAnnotationsSupport.unwrapAnnotations(a, c -> c.isAnnotationPresent(KlumCastValidator.class)))
                    .map(ValidationHandler::createFromAnnotation)
                    .map(c -> c.check(validatedAnnotation, target))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        setStatus(validatedAnnotation, Status.VALIDATED);
        return errors;
    }

    public static <T extends Annotation> KlumCastCheck<T> createFromAnnotation(T annotation) {
        KlumCastValidator validator = annotation.annotationType().getAnnotation(KlumCastValidator.class);
        if (validator == null)
            throw new IllegalStateException("Annotation " + annotation.annotationType().getName() + " is not annotated with @KlumCastValidator.");
        try {
            Class<?> type;
            if (validator.value().startsWith(".")) {
                type = Arrays.stream(annotation.annotationType().getDeclaredClasses())
                        .filter(c -> c.getSimpleName().equals(validator.value().substring(1)))
                        .filter(KlumCastCheck.class::isAssignableFrom)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Annotation " + annotation.annotationType().getName() + " does not contain a KlumCastCheck named " + validator.value().substring(1) +  "."));
            } else {
                type = Class.forName(validator.value(), true, Thread.currentThread().getContextClassLoader());
            }
            if (!KlumCastCheck.class.isAssignableFrom(type))
                throw new IllegalStateException("Class " + validator.value() + " is not a KlumCastCheck.");
            KlumCastCheck<T> check = (KlumCastCheck<T>) InvokerHelper.invokeNoArgumentsConstructorOf(type);
            return check.setValidatorAnnotation(annotation);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not create instance of KlumCastCheck " + validator.value(), e);
        }


    }
}
