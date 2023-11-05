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
import java.util.List;
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
    @NotNull protected List<T> annotationStack;

    public void setAnnotationStack(List<T> annotationStack) {
        this.annotationStack = annotationStack.subList(0, annotationStack.size() - 1);
        klumCastValidator = (KlumCastValidator) annotationStack.get(annotationStack.size() - 1);
        controlAnnotation = annotationStack.size() > 1 ? annotationStack.get(annotationStack.size() - 2) : null;
    }

    public void setMemberName(@Nullable String memberName) {
        this.memberName = memberName;
    }


    /**
     * Performs the actual check. Checks if the check is applicable for the given target and if so,
     * delegates to the doCheck method, wrapping any exceptions thrown in an ErrorMessage object.
     * <p>
     * Usually it is more convenient to override doCheck instead of this method.
     *
     * @param annotationToCheck The annotation whose placement is validated
     * @param target the target where the annotation to check is placed
     * @return an Optional ErrorMessage object if the check failed, empty if the check succeeded
     */
    public Optional<ErrorMessage> check(AnnotationNode annotationToCheck, AnnotatedNode target) {
        try {
            if (isValidFor(target))
                doCheck(annotationToCheck, target);
            return Optional.empty();
        } catch (ValidationException e) {
            return Optional.of(e.toError(annotationToCheck));
        } catch (RuntimeException e) {
            return Optional.of(new ErrorMessage(e.getMessage(), annotationToCheck));
        }
    }

    /**
     * Checks if the check is applicable for the given target. By default, this is always true, but can be overidden for
     * quick testing.
     * @param target The target where the annotation to validate is placed.
     * @return true if the check is applicable for the given target.
     */
    protected boolean isValidFor(AnnotatedNode target) {
        return true;
    }

    /**
     * Performs the actual check. If the check fails, a {@link ValidationException} or any Runtime exception should be thrown.
     * @param annotationToCheck The annotation whose placement is validated
     * @param target the target where the annotation to check is placed
     * @throws ValidationException if the check fails
     */
    protected abstract void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) throws ValidationException;

    /**
     * Encapsulates an error message and the node where the error occurred. Usually needs not to be used directly
     */
    public static class ErrorMessage {
        public final String message;
        public final ASTNode node;

        public ErrorMessage(String message, ASTNode node) {
            this.message = message;
            this.node = node;
        }
    }
}
