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
package com.blackbuild.klum.cast.validation

import com.blackbuild.klum.cast.KlumCastValidated
import com.blackbuild.klum.cast.checks.OneCheckMustMatch
import com.blackbuild.klum.cast.compiler.EffectiveAnnotation
import com.blackbuild.klum.cast.compiler.EffectiveDeclaration
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContribution
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContributor
import com.blackbuild.klum.cast.spi.ApplicabilityFilter
import com.blackbuild.klum.cast.spi.Check
import com.blackbuild.klum.cast.spi.CheckBinding
import com.blackbuild.klum.cast.spi.CheckContext
import com.blackbuild.klum.cast.spi.Diagnostic
import com.blackbuild.klum.cast.spi.DiagnosticDefinition
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

final class EffectiveDeclarationFixtureState {
    static final List<String> calls = []
    static final Map<String, Integer> contributorCalls = [:].withDefault { 0 }
    static int localTransformRuns

    static void reset() {
        calls.clear()
        contributorCalls.clear()
        localTransformRuns = 0
    }
}

class EffectiveDeclarationFixtureContributor implements EffectiveDeclarationContributor {
    @Override
    EffectiveDeclarationContribution contribute(EffectiveDeclaration declaration) {
        if (declaration.target instanceof ClassNode)
            EffectiveDeclarationFixtureState.contributorCalls[declaration.target.name]++
        def additions = []
        declaration.annotations.each { AnnotationNode request ->
            switch (request.classNode.name) {
                case EffectiveValidRequest.name:
                    additions << effective(EffectiveValidValidated, request)
                    break
                case EffectiveInvalidRequest.name:
                    additions << effective(EffectiveInvalidValidated, request)
                    break
                case EffectiveUnrelatedInvalidRequest.name:
                    additions << effective(EffectiveUnrelatedInvalidValidated, request)
                    break
                case EffectiveOrRequest.name:
                    additions << effective(EffectiveOrValidated, request)
                    break
                case EffectiveStatefulRequest.name:
                    additions << effective(EffectiveStatefulValidated, request)
                    break
                case EffectiveLocalTransformRequest.name:
                    additions << effective(EffectiveLocalTransform, request)
                    break
                case MemberDuplicateRequest.name:
                    additions << effectiveWithValue(MemberValidated, request, 'first')
                    additions << effectiveWithValue(MemberValidated, request, 'second')
                    break
                case FailingContributorRequest.name:
                    throw new IllegalStateException('fixture contributor failed')
            }
        }
        additions ? new EffectiveDeclarationContribution(additions) : EffectiveDeclarationContribution.none()
    }

    static EffectiveAnnotation effective(Class<?> type, AnnotationNode request) {
        def annotation = new AnnotationNode(ClassHelper.make(type))
        annotation.setSourcePosition(request)
        new EffectiveAnnotation(annotation, "test fixture request @${request.classNode.nameWithoutPackage}", request)
    }

    private static EffectiveAnnotation effectiveWithValue(Class<?> type, AnnotationNode request, String value) {
        def annotation = new AnnotationNode(ClassHelper.make(type))
        annotation.addMember('value', new ConstantExpression(value))
        annotation.setSourcePosition(request)
        new EffectiveAnnotation(annotation, "test fixture request @${request.classNode.nameWithoutPackage}", request)
    }
}

class EffectiveDeclarationSecondFixtureContributor implements EffectiveDeclarationContributor {
    @Override
    EffectiveDeclarationContribution contribute(EffectiveDeclaration declaration) {
        def request = declaration.annotations.find { it.classNode.name == EffectiveSecondInvalidRequest.name }
        request ? new EffectiveDeclarationContribution([EffectiveDeclarationFixtureContributor.effective(EffectiveInvalidValidated, request)])
                : EffectiveDeclarationContribution.none()
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EffectiveValidRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EffectiveInvalidRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EffectiveUnrelatedInvalidRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EffectiveSecondInvalidRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EffectiveOrRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EffectiveStatefulRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EffectiveLocalTransformRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface MemberDuplicateRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface FailingContributorRequest {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@KlumCastValidated
@CheckBinding(DirectCheck)
@interface DirectValidatedAnnotation {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@KlumCastValidated
@CheckBinding(ExistingCheck)
@interface ExistingValidatedAnnotation {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@KlumCastValidated
@CheckBinding(EffectiveValidCheck)
@interface EffectiveValidValidated {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@KlumCastValidated
@CheckBinding(EffectiveInvalidCheck)
@interface EffectiveInvalidValidated {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@KlumCastValidated
@CheckBinding(EffectiveUnrelatedInvalidCheck)
@interface EffectiveUnrelatedInvalidValidated {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface MemberValidated {
    String value()
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@CheckBinding(value = FilteredBranchCheck, filters = NeverApplicable)
@interface FilteredBranch {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@CheckBinding(PassingBranchCheck)
@interface PassingBranch {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@KlumCastValidated
@OneCheckMustMatch(message = 'effective OR did not match')
@FilteredBranch
@PassingBranch
@interface EffectiveOr {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@KlumCastValidated
@EffectiveOr
@interface EffectiveOrValidated {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@KlumCastValidated
@CheckBinding(StatefulCheck)
@interface EffectiveStatefulValidated {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass('com.blackbuild.klum.cast.validation.EffectiveLocalTransformProbe')
@interface EffectiveLocalTransform {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass('com.blackbuild.klum.cast.validation.NoOpReplayProbe')
@interface NoOpReplay {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass('com.blackbuild.klum.cast.validation.ExternalMutationProbe')
@interface ExternalMutation {}

class DirectCheck implements Check {
    @Override List<Diagnostic> check(CheckContext context) {
        EffectiveDeclarationFixtureState.calls << 'direct'
        []
    }
}

class ExistingCheck implements Check {
    @Override List<DiagnosticDefinition> getDiagnosticDefinitions() {
        [DiagnosticDefinition.of('fixture.effective.co-annotation')]
    }

    @Override List<Diagnostic> check(CheckContext context) {
        if (context.target.annotations*.classNode*.name.contains(EffectiveInvalidValidated.name)) {
            return [new Diagnostic('fixture.effective.co-annotation', 'existing annotation conflicts with effective annotation',
                    context.validatedAnnotation)]
        }
        []
    }
}

class EffectiveValidCheck implements Check {
    @Override List<Diagnostic> check(CheckContext context) {
        EffectiveDeclarationFixtureState.calls << 'effective-valid'
        []
    }
}

class EffectiveInvalidCheck implements Check {
    @Override List<DiagnosticDefinition> getDiagnosticDefinitions() {
        [DiagnosticDefinition.of('fixture.effective.invalid')]
    }

    @Override List<Diagnostic> check(CheckContext context) {
        [new Diagnostic('fixture.effective.invalid', 'effective annotation is invalid', context.validatedAnnotation)]
    }
}

class EffectiveUnrelatedInvalidCheck implements Check {
    @Override List<DiagnosticDefinition> getDiagnosticDefinitions() {
        [DiagnosticDefinition.of('fixture.effective.unrelated')]
    }

    @Override List<Diagnostic> check(CheckContext context) {
        [new Diagnostic('fixture.effective.unrelated', 'unrelated effective annotation is invalid', context.validatedAnnotation)]
    }
}

class FilteredBranchCheck implements Check {
    @Override List<Diagnostic> check(CheckContext context) {
        throw new AssertionError('filtered branch ran')
    }
}

class NeverApplicable implements ApplicabilityFilter {
    @Override boolean appliesTo(CheckContext context) { false }
}

class PassingBranchCheck implements Check {
    @Override List<Diagnostic> check(CheckContext context) {
        EffectiveDeclarationFixtureState.calls << 'or-pass'
        []
    }
}

class StatefulCheck implements Check {
    @Override List<Diagnostic> check(CheckContext context) {
        EffectiveDeclarationFixtureState.calls << 'stateful'
        []
    }
}

@GroovyASTTransformation
class EffectiveLocalTransformProbe implements ASTTransformation {
    @Override void visit(ASTNode[] nodes, SourceUnit source) {
        EffectiveDeclarationFixtureState.localTransformRuns++
        new KlumCastTransformation().visit(nodes, source)
    }
}

@GroovyASTTransformation
class NoOpReplayProbe implements ASTTransformation {
    @Override void visit(ASTNode[] nodes, SourceUnit source) {
        new KlumCastTransformation().visit(nodes, source)
    }
}

@GroovyASTTransformation
class ExternalMutationProbe implements ASTTransformation {
    @Override void visit(ASTNode[] nodes, SourceUnit source) {
        nodes[1].addAnnotation(new AnnotationNode(ClassHelper.make(EffectiveValidValidated)))
        new KlumCastTransformation().visit(nodes, source)
    }
}
