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
package com.blackbuild.klum.cast.docs.onboarding;

import com.blackbuild.klum.cast.spi.Check;
import com.blackbuild.klum.cast.spi.CheckContext;
import com.blackbuild.klum.cast.spi.Diagnostic;
import com.blackbuild.klum.cast.spi.DiagnosticDefinition;
import org.codehaus.groovy.ast.MethodNode;

import java.util.List;
import java.util.Map;

/** Stateless check used by the role-based onboarding journey. */
public final class MethodNameStartsWithCheck implements Check {

    public static final String INVALID_PREFIX = "example.method-name.invalid-prefix";

    @Override
    public List<DiagnosticDefinition> getDiagnosticDefinitions() {
        return List.of(DiagnosticDefinition.of(INVALID_PREFIX, "method", "prefix"));
    }

    @Override
    public List<Diagnostic> check(CheckContext context) {
        MethodNameStartsWith control = context.getControlAnnotation(MethodNameStartsWith.class)
                .orElseThrow(() -> new IllegalStateException("MethodNameStartsWith control annotation is required"));
        MethodNode method = (MethodNode) context.getTarget();
        if (method.getName().startsWith(control.value())) {
            return List.of();
        }
        return List.of(new Diagnostic(
                INVALID_PREFIX,
                "Method " + method.getName() + " must start with '" + control.value() + "'",
                method,
                Map.of("method", method.getName(), "prefix", control.value()),
                List.of(context.getValidatedAnnotation())
        ));
    }
}
