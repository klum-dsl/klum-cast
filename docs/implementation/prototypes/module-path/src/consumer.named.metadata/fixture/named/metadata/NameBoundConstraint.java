package fixture.named.metadata;

import com.blackbuild.klum.cast.KlumCastValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("fixture.named.implementation.NameBoundCheck")
public @interface NameBoundConstraint {}
