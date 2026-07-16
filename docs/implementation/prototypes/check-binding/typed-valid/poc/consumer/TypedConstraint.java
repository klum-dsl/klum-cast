package poc.consumer;

import poc.spi.CheckContext;
import poc.spi.TypedCheckBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@TypedCheckBinding(TypedConstraint.InnerCheck.class)
public @interface TypedConstraint {
    final class InnerCheck implements poc.spi.Check {
        @Override
        public List<String> check(CheckContext context) {
            return List.of(context.annotation().getClassNode().getName());
        }
    }
}
