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

import org.codehaus.groovy.ast.ASTNode;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable expected constraint violation.
 *
 * <p>A check supplies the code, default rendered message, primary source position, named arguments and related nodes.
 * The compiler attaches binding and composition provenance before presenting the diagnostic. Codes and argument names
 * are the compatibility surface; default prose and Groovy-specific rendering are not.</p>
 */
public final class Diagnostic {

    private final String code;
    private final String message;
    private final ASTNode primaryNode;
    private final Map<String, Object> arguments;
    private final List<ASTNode> relatedNodes;
    private final BindingMetadata binding;
    private final List<Annotation> compositionPath;

    public Diagnostic(String code, String message, ASTNode primaryNode) {
        this(code, message, primaryNode, Map.of(), List.of());
    }

    public Diagnostic(String code, String message, ASTNode primaryNode, Map<String, ?> arguments,
                      List<? extends ASTNode> relatedNodes) {
        this(code, message, primaryNode, arguments, relatedNodes, null, List.of());
    }

    private Diagnostic(String code, String message, ASTNode primaryNode, Map<String, ?> arguments,
                       List<? extends ASTNode> relatedNodes, BindingMetadata binding,
                       List<? extends Annotation> compositionPath) {
        this.code = requireText(code, "code");
        this.message = Objects.requireNonNull(message, "message");
        this.primaryNode = Objects.requireNonNull(primaryNode, "primaryNode");
        this.arguments = Map.copyOf(Objects.requireNonNull(arguments, "arguments"));
        for (String name : this.arguments.keySet()) requireText(name, "argument name");
        this.relatedNodes = List.copyOf(Objects.requireNonNull(relatedNodes, "relatedNodes"));
        this.binding = binding;
        this.compositionPath = List.copyOf(Objects.requireNonNull(compositionPath, "compositionPath"));
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public ASTNode getPrimaryNode() { return primaryNode; }
    public Map<String, Object> getArguments() { return arguments; }
    public List<ASTNode> getRelatedNodes() { return relatedNodes; }
    public Optional<BindingMetadata> getBinding() { return Optional.ofNullable(binding); }
    public List<Annotation> getCompositionPath() { return compositionPath; }

    /**
     * Returns this diagnostic with engine-owned binding and composition provenance.
     *
     * @param binding binding that selected the check
     * @param compositionPath ordered validation annotations that reached the binding
     * @return the diagnostic carrying the supplied provenance
     */
    public Diagnostic withProvenance(BindingMetadata binding, List<? extends Annotation> compositionPath) {
        return new Diagnostic(code, message, primaryNode, arguments, relatedNodes,
                Objects.requireNonNull(binding, "binding"), compositionPath);
    }

    /**
     * Returns this diagnostic after an engine-selected message template has been rendered.
     *
     * @param renderedMessage selected message text
     * @return the diagnostic carrying the rendered message
     */
    public Diagnostic withMessage(String renderedMessage) {
        return new Diagnostic(code, renderedMessage, primaryNode, arguments, relatedNodes, binding, compositionPath);
    }

    private static String requireText(String value, String name) {
        String result = Objects.requireNonNull(value, name);
        if (result.trim().isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return result;
    }
}
