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

import com.blackbuild.klum.cast.checks.KlumCastCheck;
import com.blackbuild.klum.cast.checks.NotOn;
import com.blackbuild.klum.cast.checks.OnlyOn;
import com.blackbuild.klum.cast.validation.AstSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.EnumSet;

public class MemberTargetFilter extends KlumCastCheck<Annotation> {

    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
        EnumSet<ElementType> allowedTargets;
        if (validatorAnnotation instanceof NotOn)
            allowedTargets = EnumSet.complementOf(EnumSet.copyOf(Arrays.asList(((NotOn) validatorAnnotation).value())));
        else if (validatorAnnotation instanceof OnlyOn)
            allowedTargets = EnumSet.copyOf(Arrays.asList(((OnlyOn) validatorAnnotation).value()));
        else throw new IllegalStateException("Annotation " + validatorAnnotation.annotationType().getName() + " is not supported by " + getClass().getName());

        ElementType targetElementType = AstSupport.getElementType(target.getClass());
        if (!allowedTargets.contains(targetElementType))
            throw new IllegalStateException(String.format(
                    "Annotation %s is not allowed on %s",
                    annotationToCheck.getClassNode().getName(),
                    targetElementType
            ));
        System.out.println("MemberTargetFilter.doCheck");

    }
}
