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

import com.blackbuild.klum.cast.KlumCastValidated;
import com.blackbuild.klum.cast.KlumCastValidator;
import com.blackbuild.klum.cast.spi.BindingMetadata;
import com.blackbuild.klum.cast.spi.Check;
import com.blackbuild.klum.cast.spi.CheckBinding;
import com.blackbuild.klum.cast.spi.CheckContext;
import com.blackbuild.klum.cast.spi.Diagnostic;
import com.blackbuild.klum.cast.checks.impl.KlumCastCheck;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Internal compiler orchestration for the public check SPI. */
public class ValidationHandler {

    private final AnnotationNode annotationToValidate;
    private final AnnotatedNode target;
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final List<Annotation> compositionPath = new ArrayList<>();
    private String currentMember;

    public enum Status { VALIDATED }
    public static final String METADATA_KEY = ValidationHandler.class.getName();

    ValidationHandler(AnnotationNode annotationToValidate, AnnotatedNode target) {
        this.annotationToValidate = annotationToValidate;
        this.target = target;
    }

    public static boolean alreadyValidated(AnnotationNode annotationNode) {
        return annotationNode.getNodeMetaData(METADATA_KEY) != null;
    }

    public static void setStatus(AnnotationNode annotationNode, Status status) {
        annotationNode.setNodeMetaData(METADATA_KEY, status);
    }

    public static List<Diagnostic> validateAnnotation(AnnotationNode annotationToValidate, AnnotatedNode target) {
        if (alreadyValidated(annotationToValidate)) return Collections.emptyList();
        if (!annotationToValidate.getClassNode().isResolved()) {
            return Collections.singletonList(new Diagnostic("klum-cast.unresolved-annotation",
                    "Validated annotation must have already been compiled", annotationToValidate));
        }
        return new ValidationHandler(annotationToValidate, target).validate();
    }

    private List<Diagnostic> validate() {
        validate(annotationToValidate.getClassNode().getTypeClass());
        Arrays.stream(annotationToValidate.getClassNode().getTypeClass().getDeclaredMethods())
                .filter(method -> annotationToValidate.getMember(method.getName()) != null)
                .forEach(this::validateMember);
        setStatus(annotationToValidate, Status.VALIDATED);
        return diagnostics;
    }

    private void validateMember(Method method) {
        currentMember = method.getName();
        try { validate(method); } finally { currentMember = null; }
    }

    private void validate(AnnotatedElement element) {
        RepeatableAnnotationsSupport.getAllAnnotations(element).forEach(this::handleSingleAnnotation);
    }

    void handleSingleAnnotation(Annotation annotation) {
        if (!FilterHandler.isValidFor(annotation, target, currentMember, compositionPath)) return;
        compositionPath.add(annotation);
        try {
            if (annotation instanceof KlumCastValidator) {
                executeLegacyBinding((KlumCastValidator) annotation);
            } else if (annotation instanceof CheckBinding) {
                executeTypedBinding((CheckBinding) annotation);
            } else if (isValidated(annotation)) {
                validate(annotation.annotationType());
            }
        } finally {
            compositionPath.remove(compositionPath.size() - 1);
        }
    }

    private static boolean isValidated(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(KlumCastValidated.class)
                || annotation.annotationType().isAnnotationPresent(KlumCastValidator.class)
                || annotation.annotationType().isAnnotationPresent(CheckBinding.class);
    }

    private void executeLegacyBinding(KlumCastValidator binding) {
        boolean hasName = !binding.value().isEmpty();
        boolean hasType = !binding.type().equals(KlumCastValidator.None.class);
        if (hasName == hasType) throw new IllegalStateException("@KlumCastValidator must select exactly one check binding.");
        Class<?> candidate = hasType ? binding.type() : load(binding.value());
        execute(binding, candidate, hasName ? binding.value() : candidate.getName(), new Class[0]);
    }

    private void executeTypedBinding(CheckBinding binding) {
        execute(binding, binding.value(), binding.value().getName(), binding.filters());
    }

    private void execute(Annotation declaration, Class<?> candidate, String implementationName,
                         Class<?>[] filterTypes) {
        if (!Check.class.isAssignableFrom(candidate)) {
            throw new IllegalStateException("Configured check " + implementationName + " does not implement " + Check.class.getName());
        }
        Class<? extends Check> checkType = candidate.asSubclass(Check.class);
        BindingMetadata metadata = new BindingMetadata(declaration, checkType, implementationName);
        Annotation control = findControlAnnotation();
        CheckContext context = new CheckContext(annotationToValidate, target, control, currentMember, metadata, compositionPath);
        if (!FilterHandler.areApplicable(filterTypes, context)) return;
        try {
            diagnostics.addAll(checkType.getDeclaredConstructor().newInstance().check(context));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not instantiate check " + implementationName + " with an accessible no-argument constructor", exception);
        }
    }

    private Class<?> load(String name) {
        try { return Class.forName(name, true, AstSupport.getTargetClassLoader(target)); }
        catch (ClassNotFoundException exception) { throw new IllegalStateException("Could not load check " + name, exception); }
    }

    private Annotation findControlAnnotation() {
        for (int index = compositionPath.size() - 1; index >= 0; index--) {
            Annotation candidate = compositionPath.get(index);
            if (!(candidate instanceof KlumCastValidator) && !(candidate instanceof CheckBinding)) return candidate;
        }
        return null;
    }

    @Deprecated
    List<KlumCastCheck.ErrorMessage> getErrors() {
        List<KlumCastCheck.ErrorMessage> errors = new ArrayList<>();
        for (Diagnostic diagnostic : diagnostics) {
            errors.add(new KlumCastCheck.ErrorMessage(diagnostic.getMessage(), diagnostic.getPrimaryNode()));
        }
        return errors;
    }
}
