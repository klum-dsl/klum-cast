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
package com.blackbuild.klum.cast.checks;

import com.blackbuild.klum.cast.KlumCastValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OR composition for validation annotations.
 *
 * <p>New declarations put their branch validation annotations directly on the composed validation annotation. The
 * compiler evaluates those branches in deterministic annotation-type-name order through the normal check SPI. A
 * filtered branch is not a match. When no applicable branch passes, the compiler retains every branch diagnostic and
 * adds this annotation's summary diagnostic. {@link #message()} customizes that summary without replacing the branch
 * diagnostics.</p>
 *
 * <p>Annotation-valued branch members are a deprecated source form. They remain supported as a binary migration
 * bridge for declarations compiled before 0.4, but new source must use direct branch annotations.</p>
 * <p>Example:</p>
 * <pre><code>
 * {literal @}Target(ElementType.ANNOTATION_TYPE)
 * {literal @}Retention(RetentionPolicy.RUNTIME)
 * {literal @}KlumCastValidated
 * {literal @}OneCheckMustMatch(message = "A closure needs a return type or one parameter")
 * {literal @}NumberOfParameters(1)
 * {literal @}NeedsReturnType(Closure.class)
 * {literal @}interface ClosureOrSingleParameter {
 * }
 * {literal @}Target([ElementType.METHOD, ElementType.TYPE])
 * {literal @}Retention(RetentionPolicy.RUNTIME)
 * {literal @}KlumCastValidated
 * {literal @}ClosureOrSingleParameter
 * {literal @}interface MyAnnotation {}
 * </code></pre>
 * In that case, it is sufficient for either the {@link com.blackbuild.klum.cast.checks.NeedsReturnType} or the
 * {@link com.blackbuild.klum.cast.checks.NumberOfParameters} check to succeed.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("com.blackbuild.klum.cast.validation.OneCheckMustMatchCheck")
public @interface OneCheckMustMatch {

    /**
     * Optional summary used when no applicable branch passes.
     *
     * <p>The summary is supplementary: branch diagnostics, including their codes, positions, binding identities,
     * composition paths, and related locations, are still emitted.</p>
     *
     * @return summary text, or an empty string for the engine default
     */
    String message() default "";
}
