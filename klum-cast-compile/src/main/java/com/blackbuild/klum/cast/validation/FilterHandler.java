package com.blackbuild.klum.cast.validation;

import com.blackbuild.klum.cast.Filter;
import com.blackbuild.klum.cast.checks.impl.AnnotationHelper;
import org.codehaus.groovy.ast.AnnotatedNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationTargetException;

public class FilterHandler {

    /**
     * Determines if the given annotation is valid for the given target. This is done by checking all
     * members on the target annotated with {@link Filter}. The annotation is only valid if all filters
     * match.
     *
     * @param annotation the annotation to check
     * @param target the target node to validate against
     * @return {@code true} if the annotation is valid for the target, {@code false} otherwise
     */
    public static boolean isValidFor(Annotation annotation, AnnotatedNode target) {
        return AnnotationHelper.getValuesOfMembersAnnotatedWith(annotation, Filter.class)
                .map(v -> createFrom(v, target))
                .allMatch(f -> f.isValidFor(target));
    }

    public static Filter.Function createFrom(Object memberValue, AnnotatedNode target) {
        if (memberValue instanceof Class) {
            Class<?> filterClass = (Class<?>) memberValue;
            if (Filter.Function.class.isAssignableFrom(filterClass)) {
                return doCreateFrom(filterClass);
            } else {
                throw new IllegalStateException("Filter class " + filterClass.getName() + " does not implement " + Filter.Function.class.getName());
            }
        } else if (memberValue instanceof String) {
            return doCreateFrom((String) memberValue, target);
        } else if (memberValue instanceof ElementType[]) {
            return doCreateFrom((ElementType[]) memberValue);
        } else {
            throw new IllegalStateException("Filter value must be a class, a string or an array of ElementType");
        }
    }

    private static Filter.Function doCreateFrom(ElementType[] memberValue) {
        return new ElementTypeFilter(memberValue);
    }

    private static Filter.Function doCreateFrom(String memberValue, AnnotatedNode target) {
        try {
            return doCreateFrom(Class.forName(memberValue, true, AstSupport.getTargetClassLoader(target)));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load filter class " + memberValue, e);
        }
    }

    private static Filter.Function doCreateFrom(Class<?> filterClass) {
        if (!Filter.Function.class.isAssignableFrom(filterClass))
            throw new IllegalStateException("Filter class " + filterClass.getName() + " does not implement " + Filter.Function.class.getName());

        try {
            return (Filter.Function) filterClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ElementTypeFilter implements Filter.Function {
        private final ElementType[] elementTypes;

        public ElementTypeFilter(ElementType[] elementTypes) {
            this.elementTypes = elementTypes;
        }

        @Override
        public boolean isValidFor(AnnotatedNode target) {
            return AstSupport.matchesOneOf(elementTypes, target);
        }
    }

}
