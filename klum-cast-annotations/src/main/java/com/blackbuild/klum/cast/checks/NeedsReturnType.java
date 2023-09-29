package com.blackbuild.klum.cast.checks;

import com.blackbuild.klum.cast.KlumCastValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the validated annotation is placed on a method, the return type of that method
 * must match value() (can be {@link Void}). Has no effect, if the annotation is placed on any other element.
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("com.blackbuild.klum.cast.checks.impl.NeedsReturnTypeCheck")
public @interface NeedsReturnType {
    Class<?>[] value();
}
