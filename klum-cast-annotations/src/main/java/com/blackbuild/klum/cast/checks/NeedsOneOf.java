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

import java.lang.annotation.*;

/**
 * Designates that the annotated element needs at least or exactly one of the given members.
 * This can be further restricted by specifying the {@link #whenOn()} member.
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("com.blackbuild.klum.cast.checks.impl.NeedsOneOfCheck")
@Repeatable(NeedsOneOf.List.class)
public @interface NeedsOneOf {
    /**
     * The members that are required.
     */
    String[] value();

    /**
     * If set, the annotation is only checked when placed on one of the given types.
     */
    ElementType[] whenOn() default {};

    /**
     * If set to true, exactly one of the given members must be set. If set to false, more than one member can be set.
     */
    boolean exclusive() default false;

    @Target({ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        NeedsOneOf[] value();
    }
}
