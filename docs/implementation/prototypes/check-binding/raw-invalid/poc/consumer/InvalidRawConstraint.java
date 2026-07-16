package poc.consumer;

import poc.metadata.RawCheckBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@RawCheckBinding(String.class)
public @interface InvalidRawConstraint {
}
