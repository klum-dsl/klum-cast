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
package com.blackbuild.klum.cast.compiler;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.util.List;
import java.util.Objects;

/**
 * Read-only compiler view of the declaration currently in KlumCast's validation window.
 *
 * <p>The returned Groovy AST node is supplied for inspection only. A contributor must return annotations through
 * {@link EffectiveDeclarationContribution} instead of changing this node. KlumCast detects annotation-list mutation
 * during the call and rejects it.</p>
 */
public final class EffectiveDeclaration {

    private final AnnotatedNode target;
    private final List<AnnotationNode> annotations;

    /**
     * Creates a declaration snapshot. KlumCast creates these snapshots for contributor calls.
     *
     * @param target declaration being inspected
     * @param annotations annotations present at the snapshot boundary
     */
    public EffectiveDeclaration(AnnotatedNode target, List<? extends AnnotationNode> annotations) {
        this.target = Objects.requireNonNull(target, "target");
        this.annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations"));
    }

    /** @return the declaration being inspected; contributors must not mutate it */
    public AnnotatedNode getTarget() { return target; }

    /** @return the declaration's annotations at the start of this contributor call */
    public List<AnnotationNode> getAnnotations() { return annotations; }
}
