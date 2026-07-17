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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Immutable, check-owned declaration of one stable diagnostic code and its named arguments. */
public final class DiagnosticDefinition {

    private final String code;
    private final Set<String> argumentNames;

    public DiagnosticDefinition(String code, Set<String> argumentNames) {
        this.code = requireName(code, "code");
        this.argumentNames = Set.copyOf(new LinkedHashSet<>(Objects.requireNonNull(argumentNames, "argumentNames")));
        for (String argumentName : this.argumentNames) requireName(argumentName, "argumentName");
    }

    public static DiagnosticDefinition of(String code, String... argumentNames) {
        Set<String> names = new LinkedHashSet<>();
        for (String argumentName : argumentNames) names.add(argumentName);
        return new DiagnosticDefinition(code, names);
    }

    public String getCode() { return code; }
    public Set<String> getArgumentNames() { return argumentNames; }

    private static String requireName(String value, String name) {
        String result = Objects.requireNonNull(value, name);
        if (result.trim().isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return result;
    }
}
