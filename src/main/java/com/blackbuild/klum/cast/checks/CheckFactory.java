package com.blackbuild.klum.cast.checks;

import com.blackbuild.klum.cast.KlumCastValidator;
import com.blackbuild.klum.cast.helpers.RepeatableAnnotationsSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class CheckFactory {

    public static Stream<KlumCastCheck.Error> validateAnnotation(AnnotatedNode target, AnnotationNode validatedAnnotation) {
        return Arrays.stream(validatedAnnotation.getClassNode().getTypeClass().getAnnotations())
                .flatMap(a -> RepeatableAnnotationsSupport.unwrapAnnotations(a, c -> c.isAnnotationPresent(KlumCastValidator.class)))
                .map(CheckFactory::createFromAnnotation)
                .map(c -> c.check(validatedAnnotation, target))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public static <T extends Annotation> KlumCastCheck<T> createFromAnnotation(T annotation) {
        KlumCastValidator validator = annotation.annotationType().getAnnotation(KlumCastValidator.class);
        if (validator == null)
            throw new IllegalStateException("Annotation " + annotation.annotationType().getName() + " is not annotated with @KlumCastValidator.");
        KlumCastCheck<T> check = (KlumCastCheck<T>) InvokerHelper.invokeNoArgumentsConstructorOf(validator.value());
        return check.setValidatorAnnotation(annotation);
    }
}
