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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.util.Objects;

/** An annotation added to an effective declaration together with its source provenance. */
public final class EffectiveAnnotation {

    private final AnnotationNode annotation;
    private final String origin;
    private final ASTNode originNode;

    /**
     * @param annotation annotation to attach to the declaration
     * @param origin concise human-readable reason the annotation is effective
     * @param originNode source node that requested or selected the annotation
     */
    public EffectiveAnnotation(AnnotationNode annotation, String origin, ASTNode originNode) {
        this.annotation = Objects.requireNonNull(annotation, "annotation");
        this.origin = requireText(origin, "origin");
        this.originNode = Objects.requireNonNull(originNode, "originNode");
    }

    /** @return annotation to attach; providers should preserve any applicable source position on it */
    public AnnotationNode getAnnotation() { return annotation; }

    /** @return concise human-readable description of why the annotation is effective */
    public String getOrigin() { return origin; }

    /** @return source node that selected or requested this effective annotation */
    public ASTNode getOriginNode() { return originNode; }

    private static String requireText(String value, String name) {
        String result = Objects.requireNonNull(value, name);
        if (result.trim().isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return result;
    }
}
