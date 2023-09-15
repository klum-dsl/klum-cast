package com.blackbuild.klum.cast.validation


import com.blackbuild.klum.cast.KlumCastValidator
import com.blackbuild.klum.cast.checks.KlumCastCheck
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator(Checker)
@interface DummyValidator {

    Type value()

    enum Type { PASS, FAIL }

    static class Checker extends KlumCastCheck<DummyValidator> {

        static List<Tuple2<AnnotatedNode, AnnotationNode>> runs = []

        static void reset() {
            runs.clear()
        }

        @Override
        protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
            runs << new Tuple2<>(target, annotation)
            switch (validatorAnnotation.value()) {
                case Type.PASS:
                    break
                case Type.FAIL:
                    throw new RuntimeException("Type is fail")
                default:
                    throw new IllegalStateException("Unknown validator type: " + validatorAnnotation.value())
            }
        }
    }
}
