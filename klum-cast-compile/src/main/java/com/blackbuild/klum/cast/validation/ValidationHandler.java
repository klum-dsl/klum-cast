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
package com.blackbuild.klum.cast.validation;

import com.blackbuild.klum.cast.KlumCastValidator;
import com.blackbuild.klum.cast.checks.impl.KlumCastCheck;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidationHandler {
    private static Stream<? extends Annotation> getValidatorAnnotations(Annotation a) {
        return RepeatableAnnotationsSupport.unwrapAnnotations(a, c -> c.isAnnotationPresent(KlumCastValidator.class) || KlumCastValidator.class.isAssignableFrom(c));
    }

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
            Thread.currentThread().setContextClassLoader(AstSupport.getTargetClassLoader(target));
            errors = Stream.concat(
                    validateAnnotationItself(target, validatedAnnotation),
                    validateAnnotationMembers(target, validatedAnnotation)
                    ).collect(Collectors.toList());
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        setStatus(validatedAnnotation, Status.VALIDATED);
        return errors;
    }

    private static Stream<KlumCastCheck.Error> validateAnnotationMembers(AnnotatedNode target, AnnotationNode validatedAnnotation) {
        return Arrays.stream(validatedAnnotation.getClassNode().getTypeClass().getDeclaredMethods())
                .filter(m -> validatedAnnotation.getMember(m.getName()) != null)
                .flatMap(m -> validateAnnotationMember(target, validatedAnnotation, m));
    }

    private static Stream<KlumCastCheck.Error> validateAnnotationMember(AnnotatedNode target, AnnotationNode validatedAnnotation, Method method) {
        return Arrays.stream(method.getAnnotations())
                .filter(a -> a.annotationType().isAnnotationPresent(KlumCastValidator.class))
                .map(ValidationHandler::createFromAnnotation)
                .peek(c -> c.setMemberName(method.getName()))
                .map(c -> c.check(validatedAnnotation, target))
                .flatMap(Optional::stream);
    }

    @NotNull
    private static Stream<KlumCastCheck.Error> validateAnnotationItself(AnnotatedNode target, AnnotationNode validatedAnnotation) {
        return Arrays.stream(validatedAnnotation.getClassNode().getTypeClass().getAnnotations())
                .flatMap(ValidationHandler::getValidatorAnnotations)
                .map(ValidationHandler::createFromAnnotation)
                .map(c -> c.check(validatedAnnotation, target))
                .flatMap(Optional::stream);
    }

    public static <T extends Annotation> KlumCastCheck<T> createFromAnnotation(T annotation) {
        KlumCastValidator validator;
        if (annotation instanceof KlumCastValidator) {
            validator = (KlumCastValidator) annotation;
        } else {
            validator = annotation.annotationType().getAnnotation(KlumCastValidator.class);
            if (validator == null)
                throw new IllegalStateException("Annotation " + annotation.annotationType().getName() + " is not annotated with @KlumCastValidator.");
            if (validator.parameters().length != 0)
                throw new IllegalStateException("KlumCastValidator.parameters() may only be set on direct validators.");
        }
        if (validator.value().isEmpty() && validator.type().equals(KlumCastValidator.None.class))
            throw new IllegalStateException("@KlumCastValidator must not specify a validator class using value or type.");
        if (!validator.value().isEmpty() && !validator.type().equals(KlumCastValidator.None.class))
            throw new IllegalStateException("@KlumCastValidator specifies both a validator class and a validator name.");
        try {
            Class<?> type = validator.type();
            if (type.equals(KlumCastValidator.None.class))
                type = Class.forName(validator.value(), true, Thread.currentThread().getContextClassLoader());
            if (!KlumCastCheck.class.isAssignableFrom(type))
                throw new IllegalStateException("Class " + validator.value() + " is not a KlumCastCheck.");
            KlumCastCheck<T> check = (KlumCastCheck<T>) InvokerHelper.invokeNoArgumentsConstructorOf(type);
            check.setValidatorAnnotation(annotation);
            return check;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not create instance of KlumCastCheck " + validator.value(), e);
        }
    }
}
