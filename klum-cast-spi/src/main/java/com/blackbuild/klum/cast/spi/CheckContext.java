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

/** Immutable description of one check invocation. Groovy AST types are intentional public SPI. */
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

    public AnnotationNode getValidatedAnnotation() { return validatedAnnotation; }
    public AnnotatedNode getTarget() { return target; }
    public Optional<Annotation> getControlAnnotation() { return Optional.ofNullable(controlAnnotation); }
    public Optional<String> getMemberName() { return Optional.ofNullable(memberName); }
    public BindingMetadata getBinding() { return binding; }
    public List<Annotation> getCompositionPath() { return compositionPath; }
}
