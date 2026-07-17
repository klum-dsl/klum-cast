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

import java.util.Objects;

/**
 * An immutable expected constraint violation.
 *
 * <p>This initial SPI seam carries the stable check-scoped code, rendered message and primary source position. Message
 * templates, arguments, related nodes and provenance presentation remain owned by issue #17.</p>
 */
public final class Diagnostic {

    private final String code;
    private final String message;
    private final ASTNode primaryNode;

    public Diagnostic(String code, String message, ASTNode primaryNode) {
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
        this.primaryNode = Objects.requireNonNull(primaryNode, "primaryNode");
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public ASTNode getPrimaryNode() { return primaryNode; }
}
