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
package com.blackbuild.klum.cast.checks

import com.blackbuild.klum.cast.validation.AstSpec
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class OrCompositionTest extends AstSpec {

    def "direct OR branches retain ordered diagnostics and a custom summary"() {
        given:
        createClass compositionSource()

        when:
        createClass '''
package fixture
@AllFailValidated
class FailedTarget {}
'''

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        def output = failure.message
        output.indexOf('[fixture.alpha.first] alpha first') < output.indexOf('[fixture.alpha.second] alpha second')
        output.indexOf('[fixture.alpha.second] alpha second') < output.indexOf('[fixture.zeta] zeta')
        output.contains('[klum-cast.composition.or.no-match] Select alpha or zeta')
        output.contains('Validation binding: fixture.AlphaCheck')
        output.contains('Validation binding: com.blackbuild.klum.cast.validation.OneCheckMustMatchCheck')
        output.contains('Validation path: fixture.AllFail -> com.blackbuild.klum.cast.checks.OneCheckMustMatch')
        output.contains('Related locations:')
    }

    def "OR evaluates every direct branch but discards failures when one branch passes"() {
        given:
        createClass compositionSource()

        when:
        createClass '''
package fixture
@OnePassValidated
class PassingTarget {}
'''

        then:
        noExceptionThrown()
        valueHolder.calls == ['alpha', 'pass', 'zeta']
    }

    def "filtered branches are not matches and nested OR composition preserves the outcome"() {
        given:
        createClass compositionSource()

        when:
        createClass '''
package fixture
@NestedAllFailValidated
class FailedTarget {}
'''

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        def output = failure.message
        output.contains('[fixture.alpha.first] alpha first')
        output.contains('[fixture.zeta] zeta')
        output.contains('[klum-cast.composition.or.no-match] Inner did not match')
        output.contains('[klum-cast.composition.or.no-match] Outer did not match')
        !output.contains('filtered branch ran')
        valueHolder.calls == ['alpha', 'zeta']
    }

    def "zero and fully filtered OR branch sets are not applicable"() {
        given:
        createClass compositionSource()

        when:
        createClass '''
package fixture
@NoBranchesValidated
class EmptyTarget {}
@OnlyFilteredValidated
class FilteredTarget {}
'''

        then:
        noExceptionThrown()
        !valueHolder.calls
    }

    def "a Java nested annotation keeps a non-reusable inner composition together"() {
        when:
        createClass '''
package fixture
import com.blackbuild.klum.cast.checks.NestedOrFixture
@NestedOrFixture.Validated
class NestedTarget {}
'''

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        failure.message.contains('[com.blackbuild.klum.cast.checks.NestedOrFixture$FailingCheck] inner failure')
        failure.message.contains('[klum-cast.composition.or.no-match] Inner composition did not match')
        failure.message.contains('[klum-cast.composition.or.no-match] Outer composition did not match')
    }

    def "a technical branch failure propagates with its cause"() {
        given:
        createClass compositionSource()

        when:
        createClass '''
package fixture
@TechnicalFailureValidated
class BrokenTarget {}
'''

        then:
        def failure = thrown(GroovyBugError)
        def technicalFailure = failure.cause
        technicalFailure instanceof IllegalStateException
        technicalFailure.message.contains('Technical failure for check fixture.ExplodingCheck')
        technicalFailure.cause instanceof IllegalArgumentException
        technicalFailure.cause.message == 'branch exploded'
    }

    private static String compositionSource() {
        '''
package fixture

import com.blackbuild.klum.cast.*
import com.blackbuild.klum.cast.checks.OneCheckMustMatch
import com.blackbuild.klum.cast.spi.*
import java.lang.annotation.*
import java.util.List

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(AlphaCheck)
@interface AlphaFailure {}

class AlphaCheck implements Check {
    List<DiagnosticDefinition> getDiagnosticDefinitions() {
        [DiagnosticDefinition.of('fixture.alpha.first'), DiagnosticDefinition.of('fixture.alpha.second')]
    }

    List<Diagnostic> check(CheckContext context) {
        def holder = com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder
        holder.calls = (holder.calls ?: []) + 'alpha'
        [new Diagnostic('fixture.alpha.first', 'alpha first', context.validatedAnnotation),
         new Diagnostic('fixture.alpha.second', 'alpha second', context.validatedAnnotation)]
    }
}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(PassingCheck)
@interface PassingBranch {}

class PassingCheck implements Check {
    List<Diagnostic> check(CheckContext context) {
        def holder = com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder
        holder.calls = (holder.calls ?: []) + 'pass'
        []
    }
}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(ZetaCheck)
@interface ZetaFailure {}

class ZetaCheck implements Check {
    List<DiagnosticDefinition> getDiagnosticDefinitions() { [DiagnosticDefinition.of('fixture.zeta')] }

    List<Diagnostic> check(CheckContext context) {
        def holder = com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder
        holder.calls = (holder.calls ?: []) + 'zeta'
        [new Diagnostic('fixture.zeta', 'zeta', context.validatedAnnotation)]
    }
}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(value = FilteredCheck, filters = Never)
@interface FilteredBranch {}

class Never implements ApplicabilityFilter {
    boolean appliesTo(CheckContext context) { false }
}

class FilteredCheck implements Check {
    List<Diagnostic> check(CheckContext context) {
        throw new AssertionError('filtered branch ran')
    }
}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(ExplodingCheck)
@interface ExplodingBranch {}

class ExplodingCheck implements Check {
    List<Diagnostic> check(CheckContext context) { throw new IllegalArgumentException('branch exploded') }
}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch(message = 'Select alpha or zeta')
@ZetaFailure
@AlphaFailure
@interface AllFail {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@AllFail
@interface AllFailValidated {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch
@ZetaFailure
@PassingBranch
@AlphaFailure
@interface OnePass {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OnePass
@interface OnePassValidated {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch(message = 'Inner did not match')
@FilteredBranch
@ZetaFailure
@AlphaFailure
@interface InnerAllFail {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch(message = 'Outer did not match')
@InnerAllFail
@FilteredBranch
@interface NestedAllFail {
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NestedAllFail
@interface NestedAllFailValidated {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch
@interface NoBranches {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NoBranches
@interface NoBranchesValidated {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch
@FilteredBranch
@interface OnlyFiltered {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OnlyFiltered
@interface OnlyFilteredValidated {}

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch
@ExplodingBranch
@interface TechnicalFailure {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@TechnicalFailure
@interface TechnicalFailureValidated {}
'''
    }
}
