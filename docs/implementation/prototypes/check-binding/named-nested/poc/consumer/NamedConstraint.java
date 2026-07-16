package poc.consumer;

import poc.metadata.NamedCheckBinding;
import poc.spi.CheckContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@NamedCheckBinding("poc.consumer.NamedConstraint$InnerCheck")
public @interface NamedConstraint {
    final class InnerCheck implements poc.spi.Check {
        @Override
        public List<String> check(CheckContext context) {
            return List.of(context.annotation().getClassNode().getName());
        }
    }
}
