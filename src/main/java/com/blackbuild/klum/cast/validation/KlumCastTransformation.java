package com.blackbuild.klum.cast.validation;

import com.blackbuild.klum.cast.KlumCastValidated;
import com.blackbuild.klum.cast.checks.CheckFactory;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.ClassHelper.make;

/**
 * Transformation the performs the actual validation.
 */

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class KlumCastTransformation extends AbstractASTTransformation implements GroovyClassVisitor {

    static final ClassNode KLUM_CAST_VALIDATED = make(KlumCastValidated.class);

    AnnotatedNode target;

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        target = (AnnotatedNode) nodes[1];
        visit(target);
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
        visitAnnotations(node);
        node.visitContents(this);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node);
    }

    protected void visitAnnotations(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations())
            if (isKlumCastAnnotation(annotation))
                CheckFactory.validateAnnotation(node, annotation)
                        .forEach(e -> addError(e.message, e.node));
    }

    private boolean isKlumCastAnnotation(AnnotationNode annotation) {
        if (annotation.isBuiltIn()) return false;
        return !annotation.getClassNode().getAnnotations(KLUM_CAST_VALIDATED).isEmpty();
    }

    @Override
    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node);
    }

    protected void visitConstructorOrMethod(MethodNode node) {
        visitAnnotations(node);
        for (Parameter param : node.getParameters()) {
            visitAnnotations(param);
        }
    }

    @Override
    public void visitField(FieldNode node) {
        visitAnnotations(node);
    }

    @Override
    public void visitProperty(PropertyNode node) {
        visitAnnotations(node);
    }


}