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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.*;

import java.lang.annotation.ElementType;
import java.util.Map;

public class AstSupport {

    private final static Map<ElementType, Class<? extends AnnotatedNode>> elementTypeToNodeType = Map.of(
            ElementType.TYPE, ClassNode.class,
            ElementType.FIELD, FieldNode.class,
            ElementType.METHOD, MethodNode.class,
            ElementType.PARAMETER, Parameter.class,
            ElementType.CONSTRUCTOR, ConstructorNode.class,
            //ElementType.ANNOTATION_TYPE, ClassNode.class,
            ElementType.PACKAGE, PackageNode.class
            // missing elements are not supported by Groovy 2.4
    );

    private AstSupport() {}

    public static ElementType getElementType(Class<? extends AnnotatedNode> nodeType) {
        if (nodeType.isAnnotation())
            return ElementType.ANNOTATION_TYPE;

        return elementTypeToNodeType.entrySet().stream()
                .filter(e -> e.getValue().equals(nodeType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported node type " + nodeType));
    }

    public static ClassNode getClassNode(AnnotatedNode target) {
        return target instanceof ClassNode ? (ClassNode) target : target.getDeclaringClass();
    }

    public static GroovyClassLoader getTargetClassLoader(AnnotatedNode target) {
        return getClassNode(target).getModule().getContext().getClassLoader();
    }

    public static boolean isAssignable(ClassNode type, ClassNode superClass) {
        return ClassHelper.getWrapper(type).isDerivedFrom(ClassHelper.getWrapper(superClass)) || type.implementsInterface(superClass);
    }

    public static boolean isAssignable(ClassNode type, Class<?> superClass) {
        return isAssignable(type, ClassHelper.make(superClass));
    }

    public static boolean isAssignable(Variable parameter, Class<?> requiredType) {
        return isAssignable(parameter.getType(), requiredType);
    }
}
