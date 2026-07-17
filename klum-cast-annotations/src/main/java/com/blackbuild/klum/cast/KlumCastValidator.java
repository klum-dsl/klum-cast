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
package com.blackbuild.klum.cast;

import java.lang.annotation.*;

/**
 * Meta-Annotation that defines the validator to validate the usage of the annotated annotation.
 * Either value or type must be set, but not both.
 * <p>
 *     The validFor parameter can be used to restrict the usage of the validator to certain elements.
 * </p>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KlumCastValidator.List.class)
public @interface KlumCastValidator {
    /**
     * The check implementation as a fully qualified class name. It must implement the SPI {@code Check} interface.
     * @return the validator class to use.
     */
    String value() default "";

    /**
     * Deprecated raw compatibility bridge for a typed check binding. New declarations should use
     * {@code com.blackbuild.klum.cast.spi.CheckBinding}.
     * @return the validator class to use.
     */
    @Deprecated
    Class<?> type() default None.class;

    /**
     * Additional parameters to be used for direct validators.
     * @return additional parameters to be used for direct validators.
     */
    String[] parameters() default {};

    /**
     * The elements the validator is valid for as a filter implementation. Default means no filter.
     * @return the elements the validator is valid for.
     */
    @Deprecated
    @Filter Class<?> validFor() default None.class;

    /**
     * The elements the validator is valid for as a filter implementation type name. Empty string means no filter.
     * @return the elements the validator is valid for.
     */
    @Filter String validForType() default "";

    /**
     * The elements the validator is valid for. Empty array (default) means all elements.
     * @return the elements the validator is valid for.
     */
    @Filter ElementType[] validForTargets() default {};

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        KlumCastValidator[] value();
    }

    /**
     * Marker class to indicate that no validator is defined as type.
     */
    final class None {}
}
