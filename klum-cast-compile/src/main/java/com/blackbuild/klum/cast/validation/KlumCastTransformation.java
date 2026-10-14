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
import com.blackbuild.klum.cast.compiler.EffectiveAnnotation;
import com.blackbuild.klum.cast.compiler.EffectiveDeclaration;
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContribution;
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContributor;
import com.blackbuild.klum.cast.spi.CheckBinding;
import com.blackbuild.klum.cast.spi.Diagnostic;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.ClassHelper.make;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transformation the performs the actual validation.
 */

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class KlumCastTransformation extends AbstractASTTransformation implements GroovyClassVisitor {

    static final ClassNode KLUM_CAST_VALIDATED = make(KlumCastValidated.class);
    static final ClassNode KLUM_CAST_VALIDATOR = make(KlumCastValidator.class);
    static final ClassNode CHECK_BINDING = make(CheckBinding.class);

    private static final Object EFFECTIVE_DECLARATION_STATE = KlumCastTransformation.class.getName() + ".effectiveDeclarationState";

    private List<EffectiveDeclarationContributor> contributors = List.of();

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        this.sourceUnit = source;
        contributors = ServiceLoader.load(EffectiveDeclarationContributor.class, source.getClassLoader()).stream()
                .map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparing(contributor -> contributor.getClass().getName()))
                .collect(Collectors.toList());
        source.getAST().getClasses().forEach(this::visit);
    }

    public void visit(AnnotatedNode node) {
        if (node instanceof ClassNode)
            visitClass((ClassNode) node);
        else if (node instanceof ConstructorNode)
            visitConstructor((ConstructorNode) node);
        else if (node instanceof MethodNode)
            visitMethod((MethodNode) node);
        else if (node instanceof FieldNode)
            visitField((FieldNode) node);
        else if (node instanceof PropertyNode)
            visitProperty((PropertyNode) node);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (node.isAnnotationDefinition() && hasValidationBinding(node, new HashSet<>()))
            return;
        visitAnnotations(node);
        node.visitContents(this);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node);
    }

    protected void visitAnnotations(AnnotatedNode node) {
        visitAnnotations(node, null);
    }

    private void visitAnnotations(AnnotatedNode node, MethodNode enclosingExecutable) {
        EffectiveDeclarationState state = effectiveState(node);
        if (state != null) {
            if (state.matches(node.getAnnotations())) return;
            throw new IllegalStateException("Annotations on a declaration must not change after KlumCast completes "
                    + "effective-declaration validation.");
        }
        Map<AnnotationNode, EffectiveAnnotation> effectiveAnnotations = new LinkedHashMap<>();
        contributors.forEach(contributor -> applyContribution(contributor, node, effectiveAnnotations));

        for (AnnotationNode annotation : node.getAnnotations())
            if (isKlumCastAnnotation(annotation))
                ValidationHandler.validateEffectiveAnnotation(annotation, node, enclosingExecutable)
                        .forEach(diagnostic -> addError(renderDiagnostic(diagnostic, effectiveAnnotations), diagnostic.getPrimaryNode()));
        node.setNodeMetaData(EFFECTIVE_DECLARATION_STATE, new EffectiveDeclarationState(node.getAnnotations()));
    }

    private void applyContribution(EffectiveDeclarationContributor contributor, AnnotatedNode target,
                                   Map<AnnotationNode, EffectiveAnnotation> effectiveAnnotations) {
        List<AnnotationNode> before = List.copyOf(target.getAnnotations());
        List<Map<String, Expression>> beforeMembers = memberStates(before);
        EffectiveDeclarationContribution contribution = contributor.contribute(new EffectiveDeclaration(target, before));
        if (contribution == null) {
            throw new IllegalStateException("Effective declaration contributor " + contributor.getClass().getName()
                    + " returned null instead of a complete contribution batch.");
        }
        if (!sameAnnotationState(before, beforeMembers, target.getAnnotations())) {
            throw new IllegalStateException("Effective declaration contributor " + contributor.getClass().getName()
                    + " must return additions instead of mutating a declaration.");
        }
        for (EffectiveAnnotation effectiveAnnotation : contribution.getAnnotations()) {
            if (effectiveAnnotation == null) {
                throw new IllegalStateException("Effective declaration contributor " + contributor.getClass().getName()
                        + " returned a null effective annotation.");
            }
            AnnotationNode annotation = effectiveAnnotation.getAnnotation();
            rejectDuplicateContribution(contributor, target.getAnnotations(), annotation);
            target.addAnnotation(annotation);
            effectiveAnnotations.put(annotation, effectiveAnnotation);
        }
    }

    private static EffectiveDeclarationState effectiveState(AnnotatedNode node) {
        return (EffectiveDeclarationState) node.getNodeMetaData(EFFECTIVE_DECLARATION_STATE);
    }

    private static boolean sameAnnotationNodes(List<AnnotationNode> left, List<AnnotationNode> right) {
        if (left.size() != right.size()) return false;
        for (int index = 0; index < left.size(); index++) if (left.get(index) != right.get(index)) return false;
        return true;
    }

    private static boolean sameAnnotationState(List<AnnotationNode> annotations, List<Map<String, Expression>> members,
                                               List<AnnotationNode> current) {
        if (!sameAnnotationNodes(annotations, current)) return false;
        List<Map<String, Expression>> currentMembers = memberStates(current);
        if (members.size() != currentMembers.size()) return false;
        for (int index = 0; index < members.size(); index++) {
            Map<String, Expression> expected = members.get(index);
            Map<String, Expression> actual = currentMembers.get(index);
            if (!expected.keySet().equals(actual.keySet())) return false;
            for (String name : expected.keySet()) if (expected.get(name) != actual.get(name)) return false;
        }
        return true;
    }

    private static void rejectDuplicateContribution(EffectiveDeclarationContributor contributor,
                                                    List<AnnotationNode> annotations, AnnotationNode candidate) {
        boolean duplicate = annotations.stream().anyMatch(annotation -> annotation.getClassNode().getName()
                .equals(candidate.getClassNode().getName()));
        if (duplicate) {
            throw new IllegalStateException("Effective declaration contributor " + contributor.getClass().getName()
                    + " attempted to add duplicate annotation type " + candidate.getClassNode().getName() + ".");
        }
    }

    private static List<Map<String, Expression>> memberStates(List<AnnotationNode> annotations) {
        return annotations.stream().map(annotation -> Map.copyOf(annotation.getMembers())).collect(Collectors.toList());
    }

    private static String renderDiagnostic(Diagnostic diagnostic,
                                           Map<AnnotationNode, EffectiveAnnotation> effectiveAnnotations) {
        StringBuilder rendered = new StringBuilder("[").append(diagnostic.getCode()).append("] ")
                .append(diagnostic.getMessage());
        diagnostic.getBinding().ifPresent(binding -> rendered.append("\nValidation binding: ")
                .append(binding.getImplementationName()));
        if (!diagnostic.getCompositionPath().isEmpty()) {
            rendered.append("\nValidation path: ").append(diagnostic.getCompositionPath().stream()
                    .map(Annotation::annotationType)
                    .map(Class::getName)
                    .collect(Collectors.joining(" -> ")));
        }
        if (!diagnostic.getRelatedNodes().isEmpty()) {
            rendered.append("\nRelated locations: ").append(diagnostic.getRelatedNodes().stream()
                    .map(node -> node.getLineNumber() + ":" + node.getColumnNumber())
                    .collect(Collectors.joining(", ")));
        }
        EffectiveAnnotation effectiveAnnotation = diagnostic.getPrimaryNode() instanceof AnnotationNode
                ? effectiveAnnotations.get(diagnostic.getPrimaryNode()) : null;
        if (effectiveAnnotation != null) {
            rendered.append("\nEffective annotation origin: ").append(renderOrigin(effectiveAnnotation));
        } else if (!effectiveAnnotations.isEmpty()) {
            rendered.append("\nEffective declaration context: ").append(effectiveAnnotations.values().stream()
                    .map(KlumCastTransformation::renderOrigin).collect(Collectors.joining(", ")));
        }
        return rendered.toString();
    }

    private static String renderOrigin(EffectiveAnnotation annotation) {
        return annotation.getOrigin() + " (" + annotation.getOriginNode().getLineNumber() + ":"
                + annotation.getOriginNode().getColumnNumber() + ")";
    }

    private static final class EffectiveDeclarationState {
        private final List<AnnotationNode> annotations;
        private final List<Map<String, Expression>> annotationMembers;

        private EffectiveDeclarationState(List<AnnotationNode> annotations) {
            this.annotations = List.copyOf(annotations);
            this.annotationMembers = memberStates(this.annotations);
        }

        private boolean matches(List<AnnotationNode> current) {
            return sameAnnotationState(annotations, annotationMembers, current);
        }
    }

    private boolean isKlumCastAnnotation(AnnotationNode annotation) {
        if (annotation.isBuiltIn()) return false;
        return hasValidationBinding(annotation.getClassNode(), new HashSet<>());
    }

    private boolean hasValidationBinding(ClassNode annotationType, Set<String> visited) {
        if (!visited.add(annotationType.getName())) return false;
        for (AnnotationNode declaration : annotationType.getAnnotations()) {
            ClassNode type = declaration.getClassNode();
            if (type.equals(KLUM_CAST_VALIDATED) || type.equals(KLUM_CAST_VALIDATOR) || type.equals(CHECK_BINDING)) {
                return true;
            }
            if (hasValidationBinding(type, visited)) return true;
        }
        return false;
    }

    @Override
    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node);
    }

    protected void visitConstructorOrMethod(MethodNode node) {
        visitAnnotations(node);
        for (Parameter param : node.getParameters()) {
            visitAnnotations(param, node);
        }
    }

    @Override
    public void visitField(FieldNode node) {
        visitAnnotations(node);
    }

    @Override
    public void visitProperty(PropertyNode node) {
        // do nothing, annotations will be visited on their methods or fields
        // Handling changed with Groovy 4, where PropertyNodes DID hold the annotations of their fields leading to
        // unexpected behaviour in case of "instanceof FieldNode"
    }
}
