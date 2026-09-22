/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2026 Stephan Pauxberger
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
import com.blackbuild.klum.cast.KlumCastValidator;
import com.blackbuild.klum.cast.spi.ApplicabilityFilter;
import com.blackbuild.klum.cast.spi.BindingMetadata;
import com.blackbuild.klum.cast.spi.Check;
import com.blackbuild.klum.cast.spi.CheckContext;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.MethodNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.List;

/** Resolves built-in, name-bound and typed applicability filters. */
public final class FilterHandler {
    private FilterHandler() {}

    static boolean isValidFor(Annotation annotation, AnnotatedNode target, String memberName, List<Annotation> path,
                              MethodNode enclosingExecutable) {
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Filter.class)) continue;
            try {
                Object value = method.invoke(annotation);
                if (value instanceof ElementType[] && !AstSupport.matchesOneOf((ElementType[]) value, target)) return false;
                if (value instanceof String && !((String) value).isBlank()
                        && !newFilter((String) value, target, enclosingExecutable).appliesTo(
                                context(annotation, target, memberName, path, enclosingExecutable))) return false;
                if (value instanceof Class && !value.equals(KlumCastValidator.None.class)
                        && !newFilter((Class<?>) value).appliesTo(
                                context(annotation, target, memberName, path, enclosingExecutable))) return false;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not resolve applicability filter " + method.getName(), exception);
            }
        }
        return true;
    }

    static boolean areApplicable(Class<?>[] types, CheckContext context) {
        for (Class<?> type : types) if (!newFilter(type).appliesTo(context)) return false;
        return true;
    }

    private static ApplicabilityFilter newFilter(String name, AnnotatedNode target, MethodNode enclosingExecutable) {
        AnnotatedNode classLoaderTarget = enclosingExecutable == null ? target : enclosingExecutable;
        try { return newFilter(Class.forName(name, true, AstSupport.getTargetClassLoader(classLoaderTarget))); }
        catch (ClassNotFoundException exception) { throw new IllegalStateException("Could not load filter " + name, exception); }
    }

    private static ApplicabilityFilter newFilter(Class<?> candidate) {
        if (!ApplicabilityFilter.class.isAssignableFrom(candidate)) {
            throw new IllegalStateException("Configured filter " + candidate.getName() + " does not implement " + ApplicabilityFilter.class.getName());
        }
        try { return candidate.asSubclass(ApplicabilityFilter.class).getDeclaredConstructor().newInstance(); }
        catch (ReflectiveOperationException exception) { throw new IllegalStateException("Could not instantiate filter " + candidate.getName(), exception); }
    }

    private static CheckContext context(Annotation declaration, AnnotatedNode target, String memberName,
                                        List<Annotation> path, MethodNode enclosingExecutable) {
        AnnotationNode placeholder = new AnnotationNode(org.codehaus.groovy.ast.ClassHelper.make(declaration.annotationType()));
        return new CheckContext(placeholder, target, declaration, memberName,
                new BindingMetadata(declaration, NoopCheck.class, NoopCheck.class.getName()), path,
                enclosingExecutable);
    }

    private static final class NoopCheck implements Check {
        @Override public List<com.blackbuild.klum.cast.spi.Diagnostic> check(CheckContext context) { return List.of(); }
    }
}
