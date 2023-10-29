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
 *         <li>{@link Class<Filter.Function>>}: a filter implementation</li>
 *         <li>{@link String}: The fully qualified classname of the filter implementation</li>
 *     </ul>
 * </p>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Filter {

    @FunctionalInterface
    interface Function {
        boolean isValidFor(AnnotatedNode target);
    }
}
