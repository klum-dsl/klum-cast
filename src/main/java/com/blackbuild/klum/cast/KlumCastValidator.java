package com.blackbuild.klum.cast;

import com.blackbuild.klum.cast.checks.KlumCastCheck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-Annotation that defines the validator to validate the usage of the annotated annotation.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KlumCastValidator {
    Class<? extends KlumCastCheck<?>> value();
}
