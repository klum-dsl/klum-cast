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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The given annotation is only valid if the annotated class or the owning class for members is annotated with the
 * given annotation.
 */
@Target({java.lang.annotation.ElementType.ANNOTATION_TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@KlumCastValidator("com.blackbuild.klum.cast.compiler.internal.checks.ClassNeedsAnnotationCheck")
public @interface ClassNeedsAnnotation {
    /**
     * The annotation that needs to be present on the class.
     * @return the annotation that needs to be present on the class.
     */
    Class<? extends Annotation> value();
    String message() default "Annotations annotated with %s are only valid on classes annotated with %s.";
}
