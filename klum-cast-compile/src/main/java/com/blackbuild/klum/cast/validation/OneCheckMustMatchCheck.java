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

import com.blackbuild.klum.cast.checks.OneCheckMustMatch;
import com.blackbuild.klum.cast.checks.impl.KlumCastCheck;
import com.blackbuild.klum.cast.checks.impl.ValidationException;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OneCheckMustMatchCheck extends KlumCastCheck<OneCheckMustMatch> {

    private Annotation orBranchHolder;

    @Override
    public void setAnnotationStack(List<OneCheckMustMatch> annotationStack) {
        if (annotationStack.size() < 3)
            throw new IllegalStateException("OneMustMatch annotation must be part of a validation annotation");

        orBranchHolder = annotationStack.get(annotationStack.size() - 3);
        super.setAnnotationStack(annotationStack);
    }

    @Override
    public Optional<ErrorMessage> check(AnnotationNode annotationToCheck, AnnotatedNode target) {
        boolean hasNoErrors = Arrays.stream(orBranchHolder.annotationType().getDeclaredMethods())
                .filter(method -> method.getReturnType().isAnnotation())
                .map(member -> executeSubCheck(member, annotationToCheck, target))
                .anyMatch(List::isEmpty);

        if (hasNoErrors)
            return Optional.empty();
        else
            return Optional.of(new ErrorMessage("At least one of the checks of " + orBranchHolder.annotationType().getSimpleName() + " must match.", annotationToCheck));
    }

    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) throws ValidationException {
        // unused
    }

    private List<ErrorMessage> executeSubCheck(Method member, AnnotationNode annotationToCheck, AnnotatedNode target) {
        try {
            return doExecuteSubCheck((Annotation) member.invoke(orBranchHolder), annotationToCheck, target);
        } catch (Exception e) {
            return Collections.singletonList(new ErrorMessage("Could not get member value for " + member.getName() + ": " + e.getMessage(), annotationToCheck));
        }
    }

    private List<ErrorMessage> doExecuteSubCheck(Annotation subAnnotation, AnnotationNode annotationToCheck, AnnotatedNode target) {
        ValidationHandler subCheck = new ValidationHandler(annotationToCheck, target);
        subCheck.handleSingleAnnotation(subAnnotation);
        return subCheck.getErrors();
    }
}
