/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Stephan Pauxberger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.blackbuild.klum.cast.validation;

import com.blackbuild.klum.cast.Filter;
import com.blackbuild.klum.cast.checks.impl.AnnotationHelper;
import org.codehaus.groovy.ast.AnnotatedNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationTargetException;

/** Varios methods for handling filters. */
public class FilterHandler {

    private FilterHandler() {}

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

    private static Filter.Function createFrom(Object memberValue, AnnotatedNode target) {
        if (memberValue instanceof Class) {
            Class<?> filterClass = (Class<?>) memberValue;
            if (Filter.Function.class.isAssignableFrom(filterClass)) {
                return doCreateFromType(filterClass);
            } else {
                throw new IllegalStateException("Filter class " + filterClass.getName() + " does not implement " + Filter.Function.class.getName());
            }
        } else if (memberValue instanceof String) {
            return doCreateFromTypeName((String) memberValue, target);
        } else if (memberValue instanceof ElementType[]) {
            return doCreateFromElementTypes((ElementType[]) memberValue);
        } else {
            throw new IllegalStateException("Filter value must be a class, a string or an array of ElementType");
        }
    }

    private static Filter.Function doCreateFromElementTypes(ElementType[] memberValue) {
        if (memberValue.length == 0) return Filter.All.INSTANCE;
        return new ElementTypeFilter(memberValue);
    }

    private static Filter.Function doCreateFromTypeName(String memberValue, AnnotatedNode target) {
        try {
            if (memberValue.isBlank()) return Filter.All.INSTANCE;
            return doCreateFromType(Class.forName(memberValue, true, AstSupport.getTargetClassLoader(target)));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load filter class " + memberValue, e);
        }
    }

    private static Filter.Function doCreateFromType(Class<?> filterClass) {
        if (!Filter.Function.class.isAssignableFrom(filterClass))
            throw new IllegalStateException("Filter class " + filterClass.getName() + " does not implement " + Filter.Function.class.getName());

        try {
            return (Filter.Function) filterClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static class ElementTypeFilter extends Filter.Function {
        private final ElementType[] elementTypes;

        ElementTypeFilter(ElementType[] elementTypes) {
            this.elementTypes = elementTypes;
        }

        @Override
        public boolean isValidFor(AnnotatedNode target) {
            return AstSupport.matchesOneOf(elementTypes, target);
        }
    }
}
