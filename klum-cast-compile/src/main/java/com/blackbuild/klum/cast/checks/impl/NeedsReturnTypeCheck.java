package com.blackbuild.klum.cast.checks.impl;

import com.blackbuild.klum.cast.checks.KlumCastCheck;
import com.blackbuild.klum.cast.checks.NeedsReturnType;
import com.blackbuild.klum.cast.validation.AstSupport;
import org.codehaus.groovy.ast.*;

import java.util.Arrays;

public class NeedsReturnTypeCheck extends KlumCastCheck<NeedsReturnType> {
    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
        ClassNode actualReturnType = ((MethodNode) target).getReturnType();

        if (Arrays.stream(validatorAnnotation.value()).map(ClassHelper::make).noneMatch(r -> AstSupport.isAssignable(actualReturnType, r)))
            throw new IllegalStateException("Method " + ((MethodNode) target).getName() + " must return one of " + Arrays.toString(validatorAnnotation.value()) + ".");
    }

    @Override
    protected boolean isValidFor(AnnotatedNode target) {
        return target instanceof MethodNode;
    }
}
