package poc.spi;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

public record CheckContext(AnnotationNode annotation, AnnotatedNode target) {
}
