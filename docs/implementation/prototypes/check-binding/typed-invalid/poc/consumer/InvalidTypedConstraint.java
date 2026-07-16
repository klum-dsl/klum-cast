package poc.consumer;

import poc.spi.TypedCheckBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@TypedCheckBinding(String.class)
public @interface InvalidTypedConstraint {
}
