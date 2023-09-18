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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks that an annotation only has certain members when placed on a certain targets.
 * Checks for not matching targets are ignored (considered valid), this annotation can
 * be used multiple times to check for different targets. Note that Groovy 2.4 does not
 * support repeating annotations, so you have to use the {@link AllowedMembers.List} annotation,
 * with 2.5+ this optional
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllowedMembers.List.class)
@KlumCastValidator(AllowedMembers.Check.class)
public @interface AllowedMembers {
    ElementType[] targets();

    String[] members();

    boolean invert() default false;

    class Check extends KlumCastCheck<AllowedMembers> {

        EnumSet<ElementType> targets;

        @Override
        public KlumCastCheck<AllowedMembers> setValidatorAnnotation(AllowedMembers validatorAnnotation) {
            super.setValidatorAnnotation(validatorAnnotation);
            targets = EnumSet.copyOf(Arrays.asList(validatorAnnotation.targets()));
            return this;
        }

        @Override
        protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
            if (!isValidFor(target)) return;
            if (validatorAnnotation.invert())
                assertAnnotationHasNoMembersFrom(annotationToCheck, target);
            else
                assertAnnotationOnlyHasMembersFrom(annotationToCheck, target);
        }

        private void assertAnnotationOnlyHasMembersFrom(AnnotationNode annotationToCheck, AnnotatedNode target) {
            Set<String> allowedMembers = Arrays.stream(validatorAnnotation.members()).collect(Collectors.toSet());
            Set<String> existingMembers = annotationToCheck.getMembers().keySet();

            if (allowedMembers.containsAll(existingMembers)) return;

            HashSet<String> forbiddenMembers = new HashSet<>(existingMembers);
            forbiddenMembers.removeAll(allowedMembers);

            throw new RuntimeException(String.format(
                    "Annotation %s has members which are not allowed when placed on a %s (%s)",
                    annotationToCheck.getClassNode().getNameWithoutPackage(),
                    target.getClass().getSimpleName(),
                    forbiddenMembers
            ));
        }

        private void assertAnnotationHasNoMembersFrom(AnnotationNode annotationToCheck, AnnotatedNode target) {
            Set<String> forbiddenMembers = Arrays.stream(validatorAnnotation.members()).collect(Collectors.toSet());
            Set<String> existingMembers = annotationToCheck.getMembers().keySet();

            forbiddenMembers.retainAll(existingMembers);
            if (forbiddenMembers.isEmpty()) return;

            throw new RuntimeException(String.format(
                    "Annotation %s has members which are not allowed when placed on a %s (%s)",
                    annotationToCheck.getClassNode().getNameWithoutPackage(),
                    target.getClass().getSimpleName(),
                    forbiddenMembers
            ));
        }

        private boolean isValidFor(AnnotatedNode target) {
            if (target instanceof ClassNode)
                return targets.contains(ElementType.TYPE);
            if (target instanceof org.codehaus.groovy.ast.ConstructorNode)
                return targets.contains(ElementType.CONSTRUCTOR);
            if (target instanceof org.codehaus.groovy.ast.MethodNode)
                return targets.contains(ElementType.METHOD);
            if (target instanceof org.codehaus.groovy.ast.FieldNode)
                return targets.contains(ElementType.FIELD);
            if (target instanceof org.codehaus.groovy.ast.PropertyNode)
                return targets.contains(ElementType.FIELD);
            if (target instanceof org.codehaus.groovy.ast.Parameter)
                return targets.contains(ElementType.PARAMETER);
            if (target instanceof org.codehaus.groovy.ast.Variable)
                return targets.contains(ElementType.LOCAL_VARIABLE);
            return false;
        }
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        AllowedMembers[] value();
    }
}
