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
import com.blackbuild.klum.cast.checks.impl.KlumCastCheck;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Set;

/**
 * The {@code AlsoNeeds} annotation is used to specify that a certain annotation member should be used together with
 * one or more specific annotation members. It can only be used on annotation members.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator(".Check")
public @interface AlsoNeeds {
    String[] value();

    class Check extends KlumCastCheck<AlsoNeeds> {

        @Override
        protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
            String[] requiredCoMembers = validatorAnnotation.value();
            Set<String> existingMembers = annotationToCheck.getMembers().keySet();

            if (Arrays.stream(requiredCoMembers).noneMatch(existingMembers::contains))
                throw new IllegalStateException(String.format(
                        "Annotation member %s.%s needs to be used together with one of %s",
                        annotationToCheck.getClassNode().getName(),
                        memberName,
                        Arrays.toString(requiredCoMembers)));
        }
    }
}
