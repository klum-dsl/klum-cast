package com.blackbuild.klum.cast.checks;

import com.blackbuild.klum.cast.KlumCastValidator;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Allows only one member of a given group to be set. For example, if you can designate the strategy either as a class
 * or a type name, both members are mutually exclusive. This annotation can be used multiple times with different
 * combinations of members.
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MutuallyExclusive.List.class)
@KlumCastValidator(".Check")
@Documented
public @interface MutuallyExclusive {
    String[] value();

    class Check extends KlumCastCheck<MutuallyExclusive> {
        @Override
        protected void doCheck(final AnnotationNode annotationToCheck, final AnnotatedNode target) {
            Collection<String> matchingMembers = Arrays.stream(validatorAnnotation.value()).filter(m -> annotationToCheck.getMembers().containsKey(m)).collect(Collectors.toList());
            if (matchingMembers.size() > 1)
                throw new RuntimeException("Only one of " + matchingMembers + " may be set");
        }
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        MutuallyExclusive[] value();
    }

}
