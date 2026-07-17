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
package com.blackbuild.klum.cast.spi;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable description of one check invocation.
 *
 * <p>The validated annotation and target intentionally expose Groovy compiler AST types. Check authors therefore need
 * the Groovy compiler API supplied by {@code klum-cast-spi}. The context contains the complete invocation state instead
 * of relying on mutable fields in a check implementation.</p>
 */
public final class CheckContext {

    private final AnnotationNode validatedAnnotation;
    private final AnnotatedNode target;
    private final Annotation controlAnnotation;
    private final String memberName;
    private final BindingMetadata binding;
    private final List<Annotation> compositionPath;

    public CheckContext(AnnotationNode validatedAnnotation, AnnotatedNode target, Annotation controlAnnotation,
                        String memberName, BindingMetadata binding, List<Annotation> compositionPath) {
        this.validatedAnnotation = Objects.requireNonNull(validatedAnnotation, "validatedAnnotation");
        this.target = Objects.requireNonNull(target, "target");
        this.controlAnnotation = controlAnnotation;
        this.memberName = memberName;
        this.binding = Objects.requireNonNull(binding, "binding");
        this.compositionPath = List.copyOf(compositionPath);
    }

    /** @return the use of the validated annotation being checked */
    public AnnotationNode getValidatedAnnotation() { return validatedAnnotation; }

    /** @return the Groovy AST node on which the validated annotation is used */
    public AnnotatedNode getTarget() { return target; }

    /**
     * @return the applicable control annotation, when the binding was reached through one
     */
    public Optional<Annotation> getControlAnnotation() { return Optional.ofNullable(controlAnnotation); }

    /**
     * Returns the applicable control annotation when it has the requested type.
     *
     * <p>This is the type-safe counterpart to {@link #getControlAnnotation()} for checks that know their control
     * annotation type. It does not make {@code CheckContext} generic because one context can describe a binding reached
     * through differently composed validation annotations.</p>
     *
     * @param annotationType expected control annotation type
     * @param <T> expected control annotation type
     * @return the matching control annotation, or empty when no control annotation is present or it has another type
     */
    public <T extends Annotation> Optional<T> getControlAnnotation(Class<T> annotationType) {
        Objects.requireNonNull(annotationType, "annotationType");
        return annotationType.isInstance(controlAnnotation)
                ? Optional.of(annotationType.cast(controlAnnotation))
                : Optional.empty();
    }

    /** @return the validated annotation member being checked, or empty for annotation-level validation */
    public Optional<String> getMemberName() { return Optional.ofNullable(memberName); }

    /** @return immutable identity of the binding that selected this check */
    public BindingMetadata getBinding() { return binding; }

    /** @return immutable ordered annotations through which the binding was reached */
    public List<Annotation> getCompositionPath() { return compositionPath; }
}
