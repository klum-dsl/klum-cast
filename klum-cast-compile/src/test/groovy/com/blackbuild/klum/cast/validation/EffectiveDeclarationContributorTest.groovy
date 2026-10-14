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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.GroovyBugError

class EffectiveDeclarationContributorTest extends AstSpec {

    def setup() {
        EffectiveDeclarationFixtureState.reset()
    }

    def "ordinary direct validation is unchanged without a contribution"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.DirectValidatedAnnotation

@DirectValidatedAnnotation
class DirectTarget {}
'''

        then:
        EffectiveDeclarationFixtureState.calls == ['direct']
    }

    def "a contributor validates a complete valid effective declaration"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.EffectiveValidRequest

@EffectiveValidRequest
class EffectiveTarget {}
'''

        then:
        EffectiveDeclarationFixtureState.calls == ['effective-valid']
    }

    def "new and affected existing constraints report once with effective provenance"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.EffectiveInvalidRequest
import com.blackbuild.klum.cast.validation.ExistingValidatedAnnotation

@ExistingValidatedAnnotation
@EffectiveInvalidRequest
class InvalidEffectiveTarget {}
'''

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        failure.message.count('[fixture.effective.invalid]') == 1
        failure.message.count('[fixture.effective.co-annotation]') == 1
        failure.message.contains('Effective annotation origin: test fixture request @EffectiveInvalidRequest')
        failure.message.contains('Effective declaration context: test fixture request @EffectiveInvalidRequest')
    }

    def "effective annotations retain normal filter and OR handling"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.EffectiveOrRequest

@EffectiveOrRequest
class EffectiveOrTarget {}
'''

        then:
        EffectiveDeclarationFixtureState.calls == ['or-pass']
    }

    def "contributor additions remain visible to local transforms and unchanged replay is a no-op"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.EffectiveLocalTransformRequest
import com.blackbuild.klum.cast.validation.EffectiveStatefulRequest

@EffectiveLocalTransformRequest
@EffectiveStatefulRequest
class LocalTransformTarget {}
'''

        then:
        EffectiveDeclarationFixtureState.localTransformRuns == 1
        EffectiveDeclarationFixtureState.calls == ['stateful']
        EffectiveDeclarationFixtureState.contributorCalls.LocalTransformTarget == 1
    }

    def "a no-op batch is not requested again for an unchanged declaration"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.NoOpReplay

@NoOpReplay
class NoOpTarget {}
'''

        then:
        EffectiveDeclarationFixtureState.contributorCalls.NoOpTarget == 1
    }

    def "a contributor cannot duplicate an explicit annotation"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.EffectiveInvalidRequest
import com.blackbuild.klum.cast.validation.EffectiveInvalidValidated

@EffectiveInvalidValidated
@EffectiveInvalidRequest
class ExplicitDuplicateTarget {}
'''

        then:
        def failure = thrown(GroovyBugError)
        failure.cause.message.contains('attempted to add duplicate annotation type')
        failure.cause.message.contains(EffectiveInvalidValidated.name)
    }

    def "multiple contributors cannot add the same annotation type"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.EffectiveInvalidRequest
import com.blackbuild.klum.cast.validation.EffectiveSecondInvalidRequest

@EffectiveInvalidRequest
@EffectiveSecondInvalidRequest
class ContributorDuplicateTarget {}
'''

        then:
        def failure = thrown(GroovyBugError)
        failure.cause.message.contains('EffectiveDeclarationSecondFixtureContributor')
        failure.cause.message.contains(EffectiveInvalidValidated.name)
    }

    def "same-type effective annotations with different member values are rejected"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.MemberDuplicateRequest

@MemberDuplicateRequest
class DifferentMembersTarget {}
'''

        then:
        def failure = thrown(GroovyBugError)
        failure.cause.message.contains('duplicate annotation type')
        failure.cause.message.contains(MemberValidated.name)
    }

    def "post-handoff annotation mutation is rejected"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.ExternalMutation

@ExternalMutation
class MutatedTarget {}
'''

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        failure.message.contains('must not change after KlumCast completes effective-declaration validation')
    }

    def "contributor failure does not leak into a later compilation"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.FailingContributorRequest

@FailingContributorRequest
class FailedContributorTarget {}
'''

        then:
        def failure = thrown(GroovyBugError)
        failure.cause.message == 'fixture contributor failed'

        when:
        createClass '''
import com.blackbuild.klum.cast.validation.DirectValidatedAnnotation

@DirectValidatedAnnotation
class LaterTarget {}
'''

        then:
        noExceptionThrown()
        EffectiveDeclarationFixtureState.calls == ['direct']
    }

    def "direct effective diagnostics keep only their own provenance"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.EffectiveInvalidRequest
import com.blackbuild.klum.cast.validation.EffectiveUnrelatedInvalidRequest

@EffectiveInvalidRequest
@EffectiveUnrelatedInvalidRequest
class ProvenanceTarget {}
'''

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        failure.message.count('Effective annotation origin: test fixture request @EffectiveInvalidRequest') == 1
        failure.message.count('Effective annotation origin: test fixture request @EffectiveUnrelatedInvalidRequest') == 1
        !failure.message.contains('Effective declaration context:')
    }
}
