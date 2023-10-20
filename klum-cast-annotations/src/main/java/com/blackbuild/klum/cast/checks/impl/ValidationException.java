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
package com.blackbuild.klum.cast.checks.impl;

import org.codehaus.groovy.ast.ASTNode;

/**
 * Exception that can be thrown to signal a validation error. Currently the only difference to a normal exception is
 * the option to provide an ASTNode that is used as the position of the error (which defaults to the validated annotation).
 */
public class ValidationException extends Exception {

    private final ASTNode position;
    public ValidationException(String message) {
        this(message, null, null);
    }

    public ValidationException(String message, ASTNode position) {
        this(message, null, position);
    }

    public ValidationException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public ValidationException(String message, Throwable cause, ASTNode position) {
        super(message, cause);
        this.position = position;
    }

    public ValidationException(Throwable cause) {
        this(cause.getMessage(), cause, null);
    }

    public KlumCastCheck.ErrorMessage toError(ASTNode position) {
        return new KlumCastCheck.ErrorMessage(getMessage(), this.position != null ? this.position : position);
    }
}
