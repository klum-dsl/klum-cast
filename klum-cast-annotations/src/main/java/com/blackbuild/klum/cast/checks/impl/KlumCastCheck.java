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

import com.blackbuild.klum.cast.KlumCastValidator;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Base class for KlumCastChecks. Subclasses have access to the following elements:
 * <ul>
 *     <li>The control annotation (T), like {@link com.blackbuild.klum.cast.checks.MutuallyExclusive}), this can be null</li>
 *     <li>the {@link KlumCastValidator} annotation, with access to additional parameters</li>
 *     <li>The member name of the annotated element, this is only set when the validation annotation is placed on a member</li>
 * </ul>
 * Usually only doCheck needs to be implemented, isValidFor can be used to restrict the check to certain elements.
 * @param <T>
 */
public abstract class KlumCastCheck<T extends Annotation> {

    @Nullable protected T controlAnnotation;
    @Nullable protected String memberName;
    @NotNull protected KlumCastValidator klumCastValidator;

    public void setControlAnnotation(@Nullable T controlAnnotation) {
        this.controlAnnotation = controlAnnotation;
    }

    public void setKlumCastValidatorAnnotation(@NotNull KlumCastValidator validatorAnnotation) {
        this.klumCastValidator = validatorAnnotation;
    }

    public void setMemberName(@Nullable String memberName) {
        this.memberName = memberName;
    }


    /**
     * Performs the actual check. Checks if the check is applicable for the given target and if so,
     * delegates to the doCheck method, wrapping any exceptions thrown in an Error object.
     * @param annotationToCheck The annotation whose placement is validated
     * @param target the target where the annotation to check is placed
     * @return an Optional Error object if the check failed, empty if the check succeeded
     */
    public Optional<Error> check(AnnotationNode annotationToCheck, AnnotatedNode target) {
        try {
            if (isValidFor(target))
                doCheck(annotationToCheck, target);
            return Optional.empty();
        } catch (ValidationException e) {
            return Optional.of(e.toError(annotationToCheck));
        } catch (RuntimeException e) {
            return Optional.of(new Error(e.getMessage(), annotationToCheck));
        }
    }

    protected boolean isValidFor(AnnotatedNode target) {
        return true;
    }

    protected abstract void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) throws ValidationException;

    public static class Error {
        public final String message;
        public final ASTNode node;

        public Error(String message, ASTNode node) {
            this.message = message;
            this.node = node;
        }
    }
}
