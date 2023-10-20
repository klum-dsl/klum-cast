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
package com.blackbuild.klum.cast;

import com.blackbuild.klum.cast.checks.impl.KlumCastCheck;

import java.lang.annotation.*;

/**
 * Meta-Annotation that defines the validator to validate the usage of the annotated annotation.
 * Either value or type must be set, but not both.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KlumCastValidator.List.class)
public @interface KlumCastValidator {
    /**
     * The validator class to use as fully qualified class name. Must be a subtype of {@link KlumCastCheck}.
     * @return the validator class to use.
     */
    String value() default "";

    /**
     * The validator class to use. Must be a subtype of {@link KlumCastCheck}.
     * @return the validator class to use.
     */
    Class<? extends KlumCastCheck> type() default None.class;

    /**
     * Additional parameters to be used for direct validators.
     * @return additional parameters to be used for direct validators.
     */
    String[] parameters() default {};

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        KlumCastValidator[] value();
    }

    /**
     * Marker class to indicate that no validator is defined as type.
     */
    abstract class None extends KlumCastCheck<Annotation> {}
}
