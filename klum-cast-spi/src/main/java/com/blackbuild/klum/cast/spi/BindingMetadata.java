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

import java.lang.annotation.Annotation;
import java.util.Objects;

/** Immutable identity of the declarative binding that selected a check. */
public final class BindingMetadata {

    private final Annotation declaration;
    private final Class<? extends Check> checkType;
    private final String implementationName;

    public BindingMetadata(Annotation declaration, Class<? extends Check> checkType, String implementationName) {
        this.declaration = Objects.requireNonNull(declaration, "declaration");
        this.checkType = Objects.requireNonNull(checkType, "checkType");
        this.implementationName = Objects.requireNonNull(implementationName, "implementationName");
    }

    public Annotation getDeclaration() { return declaration; }
    public Class<? extends Check> getCheckType() { return checkType; }
    public String getImplementationName() { return implementationName; }
}
