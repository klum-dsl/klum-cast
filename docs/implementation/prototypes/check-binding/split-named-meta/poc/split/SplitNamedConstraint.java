package poc.split;

import poc.metadata.NamedCheckBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@NamedCheckBinding("poc.impl.SplitNamedCheck")
public @interface SplitNamedConstraint {
}
