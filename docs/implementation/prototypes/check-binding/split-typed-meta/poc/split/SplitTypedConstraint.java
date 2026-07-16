package poc.split;

import poc.impl.SplitTypedCheck;
import poc.spi.TypedCheckBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@TypedCheckBinding(SplitTypedCheck.class)
public @interface SplitTypedConstraint {
}
