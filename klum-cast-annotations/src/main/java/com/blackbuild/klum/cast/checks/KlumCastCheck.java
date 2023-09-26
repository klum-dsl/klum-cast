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
package com.blackbuild.klum.cast.checks;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.Annotation;
import java.util.Optional;

public abstract class KlumCastCheck<T extends Annotation> {

    protected T validatorAnnotation;
    protected String memberName;

    public void setValidatorAnnotation(T validatorAnnotation) {
        this.validatorAnnotation = validatorAnnotation;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Optional<Error> check(AnnotationNode annotationToCheck, AnnotatedNode target) {
        try {
            if (isValidFor(target))
                doCheck(annotationToCheck, target);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(new Error(e.getMessage(), annotationToCheck));
        }
    }

    protected boolean isValidFor(AnnotatedNode target) {
        return true;
    }

    protected abstract void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target);

    public static class Error {
        public final String message;
        public final ASTNode node;

        public Error(String message, ASTNode node) {
            this.message = message;
            this.node = node;
        }
    }
}
