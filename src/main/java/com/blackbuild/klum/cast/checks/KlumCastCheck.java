package com.blackbuild.klum.cast.checks;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.Annotation;
import java.util.Optional;

public abstract class KlumCastCheck<T extends Annotation> {

    protected T validatorAnnotation;

    public KlumCastCheck<T> setValidatorAnnotation(T validatorAnnotation) {
        this.validatorAnnotation = validatorAnnotation;
        return this;
    }

    public Optional<Error> check(AnnotationNode annotationToCheck, AnnotatedNode target) {
        try {
            doCheck(annotationToCheck, target);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(new Error(e.getMessage(), annotationToCheck));
        }
    }

    protected abstract void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target);

    public static class Error {
        public final String message;
        public final ASTNode node;

        public Error(String message, ASTNode node) {
            this.message = message;
            this.node = node;
        }
    }


}
