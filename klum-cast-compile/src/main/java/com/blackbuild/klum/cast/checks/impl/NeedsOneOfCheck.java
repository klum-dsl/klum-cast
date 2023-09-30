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

import com.blackbuild.klum.cast.checks.NeedsOneOf;
import com.blackbuild.klum.cast.validation.AstSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class NeedsOneOfCheck extends KlumCastCheck<NeedsOneOf> {
    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
        Collection<String> matchingMembers = Arrays.stream(validatorAnnotation.value()).filter(m -> annotationToCheck.getMembers().containsKey(m)).collect(Collectors.toList());
        if (validatorAnnotation.exclusive() && matchingMembers.size() != 1)
            throw new RuntimeException("Exactly one of " + Arrays.asList(validatorAnnotation.value()) + " must be set");
        if (!validatorAnnotation.exclusive() && matchingMembers.isEmpty())
            throw new RuntimeException("At least one of " + Arrays.asList(validatorAnnotation.value()) + " must be set");
    }

    @Override
    protected boolean isValidFor(AnnotatedNode target) {
        return AstSupport.matchesOneOf(validatorAnnotation.whenOn(), target);
    }
}
