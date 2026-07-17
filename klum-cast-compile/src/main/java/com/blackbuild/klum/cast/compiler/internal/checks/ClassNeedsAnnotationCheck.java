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
package com.blackbuild.klum.cast.compiler.internal.checks;

import com.blackbuild.klum.cast.checks.ClassNeedsAnnotation;
import com.blackbuild.klum.cast.spi.Check;
import com.blackbuild.klum.cast.spi.CheckContext;
import com.blackbuild.klum.cast.spi.Diagnostic;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

import java.util.List;

public final class ClassNeedsAnnotationCheck implements Check {
    @Override public List<Diagnostic> check(CheckContext context) {
        ClassNeedsAnnotation control = (ClassNeedsAnnotation) context.getControlAnnotation().orElseThrow(() -> new IllegalStateException("ClassNeedsAnnotation requires a control annotation"));
        ClassNode target = context.getTarget() instanceof ClassNode ? (ClassNode) context.getTarget() : context.getTarget().getDeclaringClass();
        if (target.getAnnotations(ClassHelper.make(control.value())).isEmpty()) return Checks.failure(getClass(), context,
                String.format(control.message(), context.getValidatedAnnotation().getClassNode().getNameWithoutPackage(), control.value().getSimpleName()));
        return List.of();
    }
}
