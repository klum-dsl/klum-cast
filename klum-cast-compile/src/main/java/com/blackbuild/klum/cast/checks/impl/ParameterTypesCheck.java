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

import com.blackbuild.klum.cast.checks.ParameterTypes;
import com.blackbuild.klum.cast.validation.AstSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.MethodNode;

public class ParameterTypesCheck extends KlumCastCheck<ParameterTypes> {

    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {

        Class<?>[] requiredTypes = controlAnnotation.value();
        MethodNode method = (MethodNode) target;
        if (controlAnnotation.strict() && method.getParameters().length != requiredTypes.length)
            throw new IllegalStateException(String.format(
                    "Method %s has %d parameters, but exactly %d are required",
                    method.getName(),
                    method.getParameters().length,
                    requiredTypes.length
            ));

        for (int i = 0; i < method.getParameters().length; i++) {
            if (i >= requiredTypes.length)
                break;
            if (!AstSupport.isAssignable(method.getParameters()[i], requiredTypes[i]))
                throw new IllegalStateException(String.format(
                        "Parameter %d of method %s is not assignable to %s",
                        i,
                        method.getName(),
                        requiredTypes[i].getName()
                ));
        }
    }

    @Override
    protected boolean isValidFor(AnnotatedNode target) {
        return target instanceof MethodNode;
    }

}
