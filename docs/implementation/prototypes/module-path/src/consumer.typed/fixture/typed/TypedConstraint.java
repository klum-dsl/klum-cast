package fixture.typed;

import com.blackbuild.klum.cast.spi.Check;
import com.blackbuild.klum.cast.spi.CheckBinding;
import com.blackbuild.klum.cast.spi.CheckContext;
import com.blackbuild.klum.cast.spi.Diagnostic;
import org.codehaus.groovy.ast.AnnotatedNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(TypedConstraint.NestedCheck.class)
public @interface TypedConstraint {
    final class NestedCheck implements Check {
        @Override
        public List<Diagnostic> check(CheckContext context) {
            AnnotatedNode target = context.getTarget();
            if (target == null)
                throw new IllegalStateException("The SPI must supply the Groovy AST target");
            System.out.println("TYPED-CHECK-RAN");
            return List.of();
        }
    }
}
