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

import com.blackbuild.klum.cast.KlumCastValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Basic or-composition for checks. Due to the nature of the annotation definition, this is somewhat complicated.
 * <ul>
 *     <li>OneCheckMustMatch must be set on an annotation that is designated a {@link com.blackbuild.klum.cast.KlumCastValidated}
 *     (hence called 'aggregation annotation'</li>
 *     <li>The aggregation annotation contains members of the type of other validation annotations</li>
 *     <li>These usually have a default value.</li>
 *     <li>The aggregation annotation must be the ultimate validation annotation, but must be included in another annotation</li>
 *     <li>The Check of any annotation branch has no access to the annotation stack outside of the branch annotation</li>
 * </ul>
 * <p>Example:</p>
 * <pre><code>
 * {literal @}Target(ElementType.ANNOTATION_TYPE)
 * {literal @}Retention(RetentionPolicy.RUNTIME)
 * {literal @}KlumCastValidated
 * {literal @}OneCheckMustMatch
 * {literal @}interface ClosureOrSingleParameter {
 *     NumberOfParameters oneParam() default {literal @}NumberOfParameters(1);
 *     NeedsReturnType returnType() default {literal @}NeedsReturnType(Closure.class);
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
public @interface OneCheckMustMatch {}
