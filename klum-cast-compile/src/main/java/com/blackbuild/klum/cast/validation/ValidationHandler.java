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

import com.blackbuild.klum.cast.KlumCastValidated;
import com.blackbuild.klum.cast.KlumCastValidator;
import com.blackbuild.klum.cast.checks.impl.KlumCastCheck;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValidationHandler {

    private final AnnotationNode annotationToValidate;
    private final AnnotatedNode target;
    private final List<KlumCastCheck.ErrorMessage> errors = new ArrayList<>();
    private String currentMember;

    private List<Annotation> annotationStack = new ArrayList<>();

    public enum Status { VALIDATED }

    public static final String METADATA_KEY = ValidationHandler.class.getName();

    private ValidationHandler(AnnotationNode annotationToValidate, AnnotatedNode target) {
        this.annotationToValidate = annotationToValidate;
        this.target = target;
    }

    public static boolean alreadyValidated(AnnotationNode annotationNode) {
        return annotationNode.getNodeMetaData(METADATA_KEY) != null;
    }

    public static void setStatus(AnnotationNode annotationNode, ValidationHandler.Status status) {
        annotationNode.setNodeMetaData(METADATA_KEY, status);
    }

    public static List<KlumCastCheck.ErrorMessage> validateAnnotation(AnnotationNode annotationToValidate, AnnotatedNode target) {
        if (alreadyValidated(annotationToValidate))
            return Collections.emptyList();
        if (!annotationToValidate.getClassNode().isResolved())
            return Collections.singletonList(new KlumCastCheck.ErrorMessage("Validated annotation must have already been compiled", annotationToValidate));

        return new ValidationHandler(annotationToValidate, target).validate();
    }

    private List<KlumCastCheck.ErrorMessage> validate() {
        doValidate(annotationToValidate.getClassNode().getTypeClass());
        validateAnnotationMembers();
        setStatus(annotationToValidate, Status.VALIDATED);
        return errors;
    }

    private void validateAnnotationMembers() {
        Arrays.stream(annotationToValidate.getClassNode().getTypeClass().getDeclaredMethods())
                .filter(m -> annotationToValidate.getMember(m.getName()) != null)
                .forEach(this::validateAnnotationMember);
    }

    private void validateAnnotationMember(Method method) {
        currentMember = method.getName();
        try {
            doValidate(method);
        } finally {
            currentMember = null;
        }
    }

    private void doValidate(AnnotatedElement annotationOrMethod) {
        RepeatableAnnotationsSupport.getAllAnnotations(annotationOrMethod)
                .forEach(this::handleSingleAnnotation);
    }

    private void handleSingleAnnotation(Annotation annotation) {
        if (!FilterHandler.isValidFor(annotation, target))
            return;
        try {
            annotationStack.add(annotation);
            if (annotation instanceof KlumCastValidator) {
                executeValidator((KlumCastValidator) annotation);
            } else if (isValidated(annotation)) {
                doValidate(annotation.annotationType());
            }
        } finally {
            annotationStack.remove(annotationStack.size() - 1);
        }
    }

    private static boolean isValidated(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(KlumCastValidated.class) || annotation.annotationType().isAnnotationPresent(KlumCastValidator.class);
    }

    private void executeValidator(KlumCastValidator validator) {
        if (validator.value().isEmpty() && validator.type().equals(KlumCastValidator.None.class))
            throw new IllegalStateException("@KlumCastValidator must specify a validator class using value or type.");
        if (!validator.value().isEmpty() && !validator.type().equals(KlumCastValidator.None.class))
            throw new IllegalStateException("@KlumCastValidator specifies both a validator class and a validator name.");
        try {
            Class<?> type = validator.type();
            if (type.equals(KlumCastValidator.None.class))
                type = Class.forName(validator.value(), true, AstSupport.getTargetClassLoader(target));
            if (!KlumCastCheck.class.isAssignableFrom(type))
                throw new IllegalStateException("Class " + validator.value() + " is not a KlumCastCheck.");
            KlumCastCheck<Annotation> check = (KlumCastCheck<Annotation>) InvokerHelper.invokeNoArgumentsConstructorOf(type);
            check.setAnnotationStack(Collections.unmodifiableList(annotationStack));
            check.setMemberName(currentMember);
            check.check(annotationToValidate, target).ifPresent(errors::add);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not create instance of KlumCastCheck " + validator.value(), e);
        }
    }
}
