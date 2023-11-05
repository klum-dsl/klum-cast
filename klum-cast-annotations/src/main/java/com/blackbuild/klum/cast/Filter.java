package com.blackbuild.klum.cast;

import org.codehaus.groovy.ast.AnnotatedNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Designates a member as a filter. Filters are used to filter the list of possible targets for a
 * check. The check is only performed if the filter method returns true. Usually, this will be done using
 * a default method.
 * <p>
 *     The filter can be of the following types:
 *     <ul>
 *         <li>{@link ElementType[]}: The check is executed if the annotated element is one of the given types</li>
 *         <li>{@link Filter.Function}: the type of a filter implementation</li>
 *         <li>{@link String}: The fully qualified classname of the filter implementation</li>
 *     </ul>
 * </p>
 * <p>In order for a check to be executed, <b>all</b> Filter checks must pass.</p>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Filter {

    abstract class Function {
        protected Filter annotation;

        public abstract boolean isValidFor(AnnotatedNode target);

        public void setAnnotation(Filter annotation) {
            this.annotation = annotation;
        }
    }

    /** Default filter that always matches */
    class All extends Function {
        @Override
        public boolean isValidFor(AnnotatedNode target) {
            return true;
        }
    }

    /** Filter that matches if the annotated element is a method */
    class Methods extends Function {
        @Override
        public boolean isValidFor(AnnotatedNode target) {
            return target instanceof org.codehaus.groovy.ast.MethodNode;
        }
    }

    /** Filter that matches if the annotated element is a field */
    class Fields extends Function {
        @Override
        public boolean isValidFor(AnnotatedNode target) {
            return target instanceof org.codehaus.groovy.ast.FieldNode;
        }
    }
    /** Filter that matches if the annotated element is a Class */
    class Classes extends Function {
        @Override
        public boolean isValidFor(AnnotatedNode target) {
            return target instanceof org.codehaus.groovy.ast.ClassNode;
        }
    }
}
