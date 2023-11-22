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
 * Checks that the parameters, return types, the annotated type, and the type of the annotated field (if applicable) have
 * their generics set, i.e. if the respective type has a generic type parameter, it must be set.
 * For methods, checking of return type and parameters can be disabled (for fields and types, this can
 * simply be done using the default mechanisms, i.e. {@link com.blackbuild.klum.cast.Filter} or {@link Target}).
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("com.blackbuild.klum.cast.checks.impl.NeedsGenericsCheck")
@Repeatable(NeedsGenerics.List.class)
public @interface NeedsGenerics {
    /**
     * If set to false, the return type of the annotated method will not be checked. Has no effect on fields and types.
     * @return true if the return type should be checked, false otherwise
     */
    boolean checkReturnType() default true;
    /**
     * If set to false, the parameters of the annotated method will not be checked. Has no effect on fields and types.
     * @return true if the parameters should be checked, false otherwise
     */
    boolean checkParameters() default true;

    /**
     * If set to true, the check will not fail if the type has bounds on its generic type parameters. If
     * false (the default), generic types must be explicit types.
     * @return true if bounds are allowed, false otherwise
     */
    boolean allowBounds() default false;

    @Target({ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        NeedsGenerics[] value();
    }
}
