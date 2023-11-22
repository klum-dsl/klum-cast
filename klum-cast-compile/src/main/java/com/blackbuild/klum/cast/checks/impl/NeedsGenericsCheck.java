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

import com.blackbuild.klum.cast.checks.NeedsGenerics;
import org.codehaus.groovy.ast.*;

import java.util.Objects;

public class NeedsGenericsCheck extends KlumCastCheck<NeedsGenerics> {
    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) throws ValidationException {
        Objects.requireNonNull(controlAnnotation);
        if (target instanceof ClassNode) {
            ClassNode classNode = (ClassNode) target;
            if (missingGenericsForClassDefinition(classNode))
                throw new ValidationException("Class " + (classNode).getName() + " is missing generics", annotationToCheck);
        } else if (target instanceof FieldNode) {
            if (missesGenerics(((FieldNode) target).getType()))
                throw new ValidationException("Field " + ((FieldNode) target).getName() + " is missing generics", annotationToCheck);
        } else if (target instanceof MethodNode) {
            if (controlAnnotation.checkReturnType() && missesGenerics(((MethodNode) target).getReturnType()))
                throw new ValidationException("Return type of method " + ((MethodNode) target).getName() + " is missing generics", annotationToCheck);

            if (controlAnnotation.checkParameters())
                for (Parameter parameter : ((MethodNode) target).getParameters())
                    if (missesGenerics(parameter.getType()))
                        throw new ValidationException("Parameter " + parameter.getName() + " of method " + ((MethodNode) target).getName() + " is missing generics", annotationToCheck);
        }
    }

    private boolean missesGenerics(ClassNode type) {
        if (!type.redirect().isUsingGenerics())
            return false;

        if (type.getGenericsTypes() == null || type.getGenericsTypes().length == 0)
            return true;

        if (Objects.requireNonNull(controlAnnotation).allowBounds()) return false;

        for (GenericsType genericsType : type.getGenericsTypes())
            if (genericsType.getUpperBounds() != null || genericsType.getLowerBound() != null)
                return true;

        return false;
    }

    private boolean missingGenericsForClassDefinition(ClassNode node) {
        ClassNode superClass = node.getUnresolvedSuperClass(false);
        return missesGenerics(superClass);
    }
}
