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
    }
}
