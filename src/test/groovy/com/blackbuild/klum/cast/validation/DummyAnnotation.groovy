package com.blackbuild.klum.cast.validation

import com.blackbuild.klum.cast.KlumCastValidated
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.FIELD, ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass("com.blackbuild.klum.cast.validation.KlumCastTransformation")
@KlumCastValidated
@DummyValidator(DummyValidator.Type.PASS)
@interface DummyPassAnnotation {
}

@Target([ElementType.FIELD, ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass("com.blackbuild.klum.cast.validation.KlumCastTransformation")
@KlumCastValidated
@DummyValidator(DummyValidator.Type.FAIL)
@interface DummyFailAnnotation {
}
