package fixture.typed;

import com.blackbuild.klum.cast.KlumCastValidated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@TypedConstraint
public @interface TypedValidated {}
