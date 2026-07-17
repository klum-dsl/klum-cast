/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2026 Stephan Pauxberger
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

import com.blackbuild.klum.cast.spi.BindingMetadata;
import com.blackbuild.klum.cast.spi.Check;
import com.blackbuild.klum.cast.spi.CheckContext;
import com.blackbuild.klum.cast.spi.Diagnostic;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @deprecated since 0.4.0. Implement {@link Check} directly. This adapter exists only for migrating source that relies
 * on the former base class; it is not the stateless SPI contract.
 */
@Deprecated
public abstract class KlumCastCheck<T extends Annotation> implements Check {

    protected T controlAnnotation;
    protected String memberName;
    protected Annotation klumCastValidator;
    protected List<T> annotationStack;

    @Override
    public final List<Diagnostic> check(CheckContext context) {
        try {
            List<Annotation> legacyPath = new ArrayList<>(context.getCompositionPath());
            if (legacyPath.isEmpty() || !legacyPath.get(legacyPath.size() - 1).equals(context.getBinding().getDeclaration())) {
                legacyPath.add(context.getBinding().getDeclaration());
            }
            setAnnotationStack((List<T>) legacyPath);
            setMemberName(context.getMemberName().orElse(null));
            Optional<ErrorMessage> error = check(context.getValidatedAnnotation(), context.getTarget());
            return error.map(it -> Collections.singletonList(new Diagnostic(getClass().getName(), it.message, it.node)))
                    .orElseGet(Collections::emptyList);
        } finally {
            controlAnnotation = null;
            memberName = null;
            klumCastValidator = null;
            annotationStack = null;
        }
    }

    /** @deprecated Use {@link CheckContext} in a {@link Check} implementation. */
    @Deprecated
    public Optional<ErrorMessage> check(AnnotationNode annotationToCheck, AnnotatedNode target) {
        try {
            if (isValidFor(target)) doCheck(annotationToCheck, target);
            return Optional.empty();
        } catch (ValidationException exception) {
            return Optional.of(exception.toError(annotationToCheck));
        } catch (RuntimeException exception) {
            return Optional.of(new ErrorMessage(exception.getMessage(), annotationToCheck));
        }
    }

    /** @deprecated Legacy invocation setup; new checks receive {@link CheckContext}. */
    @Deprecated
    public void setAnnotationStack(List<T> annotations) {
        annotationStack = annotations.subList(0, annotations.size() - 1);
        klumCastValidator = annotations.get(annotations.size() - 1);
        controlAnnotation = annotations.size() > 1 ? annotations.get(annotations.size() - 2) : null;
    }

    /** @deprecated Legacy invocation setup; new checks receive {@link CheckContext}. */
    @Deprecated
    public void setMemberName(String memberName) { this.memberName = memberName; }

    /** @deprecated Use an {@link com.blackbuild.klum.cast.spi.ApplicabilityFilter}. */
    @Deprecated
    protected boolean isValidFor(AnnotatedNode target) { return true; }

    /** @deprecated Implement {@link Check#check(CheckContext)}. */
    @Deprecated
    protected abstract void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) throws ValidationException;

    /** @deprecated Return a {@link Diagnostic} from {@link Check#check(CheckContext)}. */
    @Deprecated
    public static class ErrorMessage {
        public final String message;
        public final ASTNode node;
        public ErrorMessage(String message, ASTNode node) { this.message = message; this.node = node; }
    }
}
