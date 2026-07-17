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
package com.blackbuild.klum.cast.validation;

import java.util.Map;
import java.util.Set;

/** Parses the deliberately small named-argument template syntax used by validation annotations. */
final class DiagnosticTemplates {

    private DiagnosticTemplates() {}

    static void validate(String template, Set<String> allowedArguments) {
        render(template, allowedArguments, null);
    }

    static String render(String template, Map<String, Object> arguments) {
        return render(template, arguments.keySet(), arguments);
    }

    private static String render(String template, Set<String> allowedArguments, Map<String, Object> arguments) {
        StringBuilder rendered = new StringBuilder();
        for (int index = 0; index < template.length(); index++) {
            char character = template.charAt(index);
            if (character == '{') {
                if (index + 1 < template.length() && template.charAt(index + 1) == '{') {
                    rendered.append('{');
                    index++;
                    continue;
                }
                int end = template.indexOf('}', index + 1);
                if (end < 0) throw new IllegalArgumentException("Unclosed diagnostic template argument in '" + template + "'");
                String name = template.substring(index + 1, end);
                if (!isName(name) || !allowedArguments.contains(name)) {
                    throw new IllegalArgumentException("Unknown diagnostic template argument '" + name + "' in '" + template + "'");
                }
                if (arguments != null) rendered.append(String.valueOf(arguments.get(name)));
                index = end;
            } else if (character == '}') {
                if (index + 1 < template.length() && template.charAt(index + 1) == '}') {
                    rendered.append('}');
                    index++;
                } else {
                    throw new IllegalArgumentException("Unescaped '}' in diagnostic template '" + template + "'");
                }
            } else {
                rendered.append(character);
            }
        }
        return rendered.toString();
    }

    private static boolean isName(String name) {
        if (name.isEmpty()) return false;
        for (int index = 0; index < name.length(); index++) {
            char character = name.charAt(index);
            if (!Character.isLetterOrDigit(character) && character != '_') return false;
        }
        return true;
    }
}
