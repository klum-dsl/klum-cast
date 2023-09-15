package com.blackbuild.klum.cast.checks;

import com.blackbuild.klum.cast.KlumCastValidator;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.ANNOTATION_TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@KlumCastValidator(ClassNeedsAnnotation.Check.class)
public @interface ClassNeedsAnnotation {
    Class<? extends Annotation> value();
    String message() default "Annotations annotated with %s are only valid on classes annotated with %s.";

    class Check extends KlumCastCheck<ClassNeedsAnnotation> {

        @Override
        protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
            ClassNode realTarget = (target instanceof ClassNode) ? (ClassNode) target : target.getDeclaringClass();
            if (realTarget.getAnnotations(ClassHelper.make(validatorAnnotation.value())).isEmpty())
                throw new RuntimeException(String.format(validatorAnnotation.message(), annotationToCheck.getClassNode().getNameWithoutPackage(), validatorAnnotation.value().getSimpleName()));
        }
    }
}
