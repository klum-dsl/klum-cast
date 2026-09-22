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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

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
    private final MethodNode enclosingExecutable;
    private final ClassNode declaringClass;

    /**
     * Creates a context without enclosing-executable navigation.
     *
     * <p>This constructor is retained for compatibility with consumers that construct contexts directly. Because a
     * {@link org.codehaus.groovy.ast.Parameter Parameter} does not identify its owner, contexts created this way report
     * empty {@linkplain #getEnclosingExecutable() enclosing-executable} and
     * {@linkplain #getDeclaringClass() declaring-class} navigation.</p>
     *
     * @param validatedAnnotation use of the validated annotation
     * @param target node on which the validated annotation is used
     * @param controlAnnotation applicable control annotation, or {@code null}
     * @param memberName validated annotation member, or {@code null} for annotation-level validation
     * @param binding binding that selected the check
     * @param compositionPath ordered annotations through which the binding was reached
     */
    public CheckContext(AnnotationNode validatedAnnotation, AnnotatedNode target, Annotation controlAnnotation,
                        String memberName, BindingMetadata binding, List<Annotation> compositionPath) {
        this(validatedAnnotation, target, controlAnnotation, memberName, binding, compositionPath, null);
    }

    /**
     * Creates a context with optional enclosing-executable navigation captured by the compiler traversal.
     *
     * @param validatedAnnotation use of the validated annotation
     * @param target node on which the validated annotation is used
     * @param controlAnnotation applicable control annotation, or {@code null}
     * @param memberName validated annotation member, or {@code null} for annotation-level validation
     * @param binding binding that selected the check
     * @param compositionPath ordered annotations through which the binding was reached
     * @param enclosingExecutable method or constructor enclosing the target, or {@code null} when none was captured
     */
    public CheckContext(AnnotationNode validatedAnnotation, AnnotatedNode target, Annotation controlAnnotation,
                        String memberName, BindingMetadata binding, List<Annotation> compositionPath,
                        MethodNode enclosingExecutable) {
        this.validatedAnnotation = Objects.requireNonNull(validatedAnnotation, "validatedAnnotation");
        this.target = Objects.requireNonNull(target, "target");
        this.controlAnnotation = controlAnnotation;
        this.memberName = memberName;
        this.binding = Objects.requireNonNull(binding, "binding");
        this.compositionPath = List.copyOf(compositionPath);
        this.enclosingExecutable = enclosingExecutable;
        this.declaringClass = enclosingExecutable == null ? null : enclosingExecutable.getDeclaringClass();
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

    /**
     * Returns the method or constructor enclosing a parameter target.
     *
     * <p>A constructor is represented by a {@link org.codehaus.groovy.ast.ConstructorNode ConstructorNode}, which is a
     * {@link MethodNode}. KlumCast populates this value directly while visiting parameters. It is empty for
     * non-parameter targets and contexts created with the compatibility constructor.</p>
     *
     * @return the enclosing method or constructor, when captured
     */
    public Optional<MethodNode> getEnclosingExecutable() { return Optional.ofNullable(enclosingExecutable); }

    /**
     * Returns the class declaring the captured enclosing method or constructor.
     *
     * <p>This value is empty whenever {@link #getEnclosingExecutable()} is empty or the captured executable has no
     * declaring class.</p>
     *
     * @return the declaring class of the enclosing executable, when available
     */
    public Optional<ClassNode> getDeclaringClass() { return Optional.ofNullable(declaringClass); }
}
