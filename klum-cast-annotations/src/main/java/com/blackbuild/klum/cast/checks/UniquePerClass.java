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
import org.codehaus.groovy.ast.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Checks that the given annotation is only used on one element in a class.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator(type = UniquePerClass.Check.class)
public @interface UniquePerClass {
    class Check extends KlumCastCheck<UniquePerClass> {

        private static final String METADATA_KEY = UniquePerClass.class.getName();

        @Override
        protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
            ClassNode classNode = getClassNode(target);
            AnnotatedNode existingEntry = classNode.getNodeMetaData(METADATA_KEY);
            if (existingEntry != null)
                throw new IllegalStateException("Annotation " + annotationToCheck.getClassNode().getName() + " is used multiple times in class " + classNode.getName());

            classNode.setNodeMetaData(METADATA_KEY, target);
        }

        private ClassNode getClassNode(AnnotatedNode target) {
            return target instanceof ClassNode ? (ClassNode) target : target.getDeclaringClass();
        }
    }
}
