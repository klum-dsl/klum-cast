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

import com.blackbuild.klum.cast.checks.impl.KlumCastCheck;
import com.blackbuild.klum.cast.checks.impl.ValidationException;

import com.blackbuild.klum.cast.checks.ForbiddenModifiers;
import com.blackbuild.klum.cast.checks.NeedsModifiers;
import org.codehaus.groovy.ast.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ModifiersCheck extends KlumCastCheck<Annotation> {
    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) throws ValidationException {
        int existingModifiers = getModifiers(target);
        if (existingModifiers == -1) return;

        if (controlAnnotation instanceof NeedsModifiers) {
            int requiredModifiers = Arrays.stream(((NeedsModifiers) controlAnnotation).value()).reduce(0, (a, b) -> a | b);
            requiredModifiers = removeImpossibleModifiers(requiredModifiers, target);
            if ((existingModifiers & requiredModifiers) != requiredModifiers)
                throw new ValidationException(target + " requires modifiers " + Modifier.toString(requiredModifiers), target);
        }
        else if (controlAnnotation instanceof ForbiddenModifiers) {
            int forbbidenModifiers = Arrays.stream(((ForbiddenModifiers) controlAnnotation).value()).reduce(0, (a, b) -> a | b);
            if ((existingModifiers & forbbidenModifiers) != 0)
                throw new ValidationException(target + " must not have modifiers " + Modifier.toString(forbbidenModifiers), target);
        } else {
            throw new IllegalStateException("Annotation " + controlAnnotation.annotationType().getName() + " is not supported by " + getClass().getName());
        }
    }

    private int removeImpossibleModifiers(int requiredModifiers, AnnotatedNode target) {
        if (target instanceof ClassNode) {
            if (!(target instanceof InnerClassNode))
                requiredModifiers &= ~Modifier.STATIC;
            requiredModifiers &= ~Modifier.TRANSIENT;
        }
        if (target instanceof FieldNode)
            requiredModifiers &= ~Modifier.ABSTRACT & ~Modifier.INTERFACE;
        if (target instanceof MethodNode)
            requiredModifiers &= ~Modifier.INTERFACE & ~Modifier.TRANSIENT;
        return requiredModifiers;
    }

    private static int getModifiers(AnnotatedNode target) {
        if (target instanceof ClassNode)
            return ((ClassNode) target).getModifiers();
        else if (target instanceof MethodNode)
            return ((MethodNode) target).getModifiers();
        else if (target instanceof FieldNode)
            return ((FieldNode) target).getModifiers();
        return -1;
    }
}
