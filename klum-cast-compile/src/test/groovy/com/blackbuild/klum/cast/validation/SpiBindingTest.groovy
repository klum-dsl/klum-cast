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

import com.blackbuild.klum.cast.spi.Check
import com.blackbuild.klum.cast.spi.BindingMetadata
import com.blackbuild.klum.cast.spi.CheckBinding
import com.blackbuild.klum.cast.spi.CheckContext
import com.blackbuild.klum.cast.spi.Diagnostic
import com.blackbuild.klum.cast.spi.DiagnosticDefinition
import com.blackbuild.klum.cast.checks.impl.KlumCastCheck
import com.blackbuild.klum.cast.DiagnosticMessage
import com.blackbuild.klum.cast.DiagnosticMessages
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.control.MultipleCompilationErrorsException

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class SpiBindingTest extends AstSpec {

    def "nested typed checks receive complete immutable context and conjunctive filters run end to end"() {
        given:
        createClass '''
package fixture

import com.blackbuild.klum.cast.spi.*
import org.codehaus.groovy.ast.ClassNode
import java.lang.annotation.*
import java.util.List

@Target([ElementType.ANNOTATION_TYPE, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(value = TypedConstraint.NestedCheck, filters = [TypedConstraint.ClassOnly])
@interface TypedConstraint {
    static class NestedCheck implements Check {
        List<Diagnostic> check(CheckContext context) {
            def holder = com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder
            holder.typedContexts = (holder.typedContexts ?: []) + [[
                validated: context.validatedAnnotation.classNode.name,
                target: context.target.class.name,
                control: context.getControlAnnotation(TypedConstraint).get().annotationType().simpleName,
                member: context.memberName.orElse(null),
                binding: context.binding.declaration.annotationType().simpleName,
                implementation: context.binding.implementationName,
                path: context.compositionPath*.annotationType()*.simpleName
            ]]
            try {
                context.compositionPath.add(context.controlAnnotation.get())
                holder.immutable = false
            } catch (UnsupportedOperationException ignored) {
                holder.immutable = true
            }
            []
        }
    }
    static class ClassOnly implements ApplicabilityFilter {
        boolean appliesTo(CheckContext context) { context.target instanceof ClassNode }
    }
}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(value = SkippedConstraint.SkippedCheck, filters = [SkippedConstraint.Always, SkippedConstraint.Never])
@interface SkippedConstraint {
    static class SkippedCheck implements Check {
        List<Diagnostic> check(CheckContext context) {
            com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.skippedCheckRan = true
            []
        }
    }
    static class Always implements ApplicabilityFilter {
        boolean appliesTo(CheckContext context) {
            com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.filterCalls =
                    (com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.filterCalls ?: []) + 'always'
            true
        }
    }
    static class Never implements ApplicabilityFilter {
        boolean appliesTo(CheckContext context) {
            com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.filterCalls =
                    (com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.filterCalls ?: []) + 'never'
            false
        }
    }
}
'''
        createClass '''
package fixture
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@TypedConstraint
@interface TypedValidated {
    @TypedConstraint
    String configured() default ''
}
'''
        createClass '''
package fixture
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@SkippedConstraint
@interface SkippedValidated {}
'''

        when:
        createClass '''
package fixture
@TypedValidated(configured = 'configured')
class TypedTarget {}
'''
        createClass '''
package fixture
@SkippedValidated
class SkippedTarget {}
'''

        then:
        valueHolder.typedContexts == [
                [validated: 'fixture.TypedValidated', target: 'org.codehaus.groovy.ast.ClassNode', control: 'TypedConstraint',
                 member: null, binding: 'CheckBinding', implementation: 'fixture.TypedConstraint$NestedCheck', path: ['TypedConstraint', 'CheckBinding']],
                [validated: 'fixture.TypedValidated', target: 'org.codehaus.groovy.ast.ClassNode', control: 'TypedConstraint',
                 member: 'configured', binding: 'CheckBinding', implementation: 'fixture.TypedConstraint$NestedCheck', path: ['TypedConstraint', 'CheckBinding']]
        ]
        valueHolder.immutable
        valueHolder.filterCalls == ['always', 'never']
        !valueHolder.skippedCheckRan
    }

    def "name-bound check can be resolved from a separate consumer classloader"() {
        given:
        createClass '''
package splitmeta
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator(value = "splitimpl.NamedCheck", validForType = "splitimpl.NameFilter", validForTargets = ElementType.TYPE)
@interface NamedConstraint {}
'''
        newClassLoader()
        createClass '''
package splitimpl
import com.blackbuild.klum.cast.spi.*
import org.codehaus.groovy.ast.ClassNode
import java.util.List

class NamedCheck implements Check {
    List<Diagnostic> check(CheckContext context) {
        com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.named = context.controlAnnotation.get().annotationType().simpleName
        []
    }
}

class NameFilter implements ApplicabilityFilter {
    boolean appliesTo(CheckContext context) {
        com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.nameFilterRan = true
        context.target instanceof ClassNode
    }
}
'''
        newClassLoader()
        createClass '''
package splituse
import splitmeta.NamedConstraint
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NamedConstraint
@interface SplitValidated {}
'''

        when:
        createClass '''
package splituse
@SplitValidated
class SplitTarget {}
'''

        then:
        valueHolder.named == 'NamedConstraint'
        valueHolder.nameFilterRan
    }

    def "technical failures preserve their causes"() {
        when:
        execute(ConstructionFailure.getAnnotation(CheckBinding))

        then:
        def constructionFailure = thrown(IllegalStateException)
        constructionFailure.message.contains('accessible no-argument constructor')
        constructionFailure.cause instanceof NoSuchMethodException

        when:
        execute(ExecutionFailure.getAnnotation(CheckBinding))

        then:
        def executionFailure = thrown(IllegalStateException)
        executionFailure.message.contains('Technical failure for check')
        executionFailure.message.contains('ExecutionFailureCheck')
        executionFailure.cause instanceof IllegalArgumentException
        executionFailure.cause.message == 'direct check failure'
    }

    def "multiple structured diagnostics use nearest named overrides and preserve supplementary context"() {
        given:
        createClass '''
package fixture

import com.blackbuild.klum.cast.*
import com.blackbuild.klum.cast.spi.*
import org.codehaus.groovy.ast.ASTNode
import java.lang.annotation.*
import java.util.List
import java.util.Map

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DiagnosticMessages([@DiagnosticMessage(code = 'fixture.first', template = 'outer {name}')])
@InnerConstraint
@interface OuterConstraint {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DiagnosticMessages([@DiagnosticMessage(code = 'fixture.first', template = 'inner {name}')])
@CheckBinding(DiagnosticCheck)
@interface InnerConstraint {}

class DiagnosticCheck implements Check {
    List<DiagnosticDefinition> getDiagnosticDefinitions() {
        [new DiagnosticDefinition('fixture.first', ['name'] as Set),
         new DiagnosticDefinition('fixture.second', [] as Set)]
    }

    List<Diagnostic> check(CheckContext context) {
        [new Diagnostic('fixture.first', 'default first', context.validatedAnnotation, [name: 'first'], [context.target]),
         new Diagnostic('fixture.second', 'default second', context.validatedAnnotation)]
    }
}
'''
        createClass '''
package fixture
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OuterConstraint
@interface StructuredValidated {}
'''

        when:
        createClass '''
package fixture
@StructuredValidated
class StructuredTarget {}
'''

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        def output = failure.message
        output.indexOf('[fixture.first] inner first') < output.indexOf('[fixture.second] default second')
        output.contains('Validation binding: fixture.DiagnosticCheck')
        output.contains('Validation path: fixture.OuterConstraint -> fixture.InnerConstraint')
        output.contains('Related locations:')
    }

    def "unknown codes and invalid named templates are technical configuration failures"() {
        when:
        executeControl(UnknownCodeConstraint.getAnnotation(UnknownCodeControl))

        then:
        def unknownCode = thrown(IllegalStateException)
        unknownCode.message.contains('does not declare diagnostic message code missing')

        when:
        executeControl(InvalidTemplateConstraint.getAnnotation(InvalidTemplateControl))

        then:
        def invalidTemplate = thrown(IllegalStateException)
        invalidTemplate.message.contains('has an invalid template for fixture.known')
        invalidTemplate.cause instanceof IllegalArgumentException
    }

    def "deprecated adapter clears mutable invocation fields after every call"() {
        given:
        def adapter = new LegacyAdapterCheck()
        def binding = ConstructionFailure.getAnnotation(CheckBinding)

        when:
        adapter.check(legacyContext('first', binding))
        adapter.check(legacyContext('second', binding))

        then:
        adapter.invocations == [
                ['LegacyControl', 'first', 'CheckBinding', ['LegacyControl']],
                ['LegacyControl', 'second', 'CheckBinding', ['LegacyControl']]
        ]
        adapter.invocationStateIsClear()
    }

    private static void execute(CheckBinding binding) {
        new ValidationHandler(new AnnotationNode(ClassHelper.make(SpiBindingTest)), ClassHelper.make(SpiBindingTest))
                .handleSingleAnnotation(binding)
    }

    private static void executeControl(Annotation control) {
        new ValidationHandler(new AnnotationNode(ClassHelper.make(SpiBindingTest)), ClassHelper.make(SpiBindingTest))
                .handleSingleAnnotation(control)
    }

    private static CheckContext legacyContext(String member, CheckBinding binding) {
        def control = LegacyUse.getAnnotation(LegacyControl)
        new CheckContext(new AnnotationNode(ClassHelper.make(SpiBindingTest)), ClassHelper.make(SpiBindingTest), control,
                member, new BindingMetadata(binding, LegacyAdapterCheck, 'legacy'), [control, binding])
    }

}

@CheckBinding(ConstructionFailureCheck)
@Retention(RetentionPolicy.RUNTIME)
@interface ConstructionFailure {}

class ConstructionFailureCheck implements Check {
    ConstructionFailureCheck(String ignored) {}

    @Override
    List<Diagnostic> check(CheckContext context) { [] }
}

@CheckBinding(ExecutionFailureCheck)
@Retention(RetentionPolicy.RUNTIME)
@interface ExecutionFailure {}

class ExecutionFailureCheck implements Check {
    @Override
    List<Diagnostic> check(CheckContext context) {
        throw new IllegalArgumentException('direct check failure')
    }
}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DiagnosticMessages([@DiagnosticMessage(code = 'missing', template = 'nope')])
@CheckBinding(KnownDiagnosticCheck)
@interface UnknownCodeControl {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@UnknownCodeControl
@interface UnknownCodeConstraint {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DiagnosticMessages([@DiagnosticMessage(code = 'fixture.known', template = 'bad {unknown}')])
@CheckBinding(KnownDiagnosticCheck)
@interface InvalidTemplateControl {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@InvalidTemplateControl
@interface InvalidTemplateConstraint {}

class KnownDiagnosticCheck implements Check {
    @Override
    List<Diagnostic> check(CheckContext context) { [] }

    @Override
    List<DiagnosticDefinition> getDiagnosticDefinitions() {
        [new DiagnosticDefinition('fixture.known', ['name'] as Set)]
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface LegacyControl {}

@LegacyControl
class LegacyUse {}

class LegacyAdapterCheck extends KlumCastCheck<LegacyControl> {
    List<List<Object>> invocations = []

    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
        invocations << [
                controlAnnotation.annotationType().simpleName,
                memberName,
                klumCastValidator.annotationType().simpleName,
                annotationStack*.annotationType()*.simpleName
        ]
    }

    boolean invocationStateIsClear() {
        controlAnnotation == null && memberName == null && klumCastValidator == null && annotationStack == null
    }
}
