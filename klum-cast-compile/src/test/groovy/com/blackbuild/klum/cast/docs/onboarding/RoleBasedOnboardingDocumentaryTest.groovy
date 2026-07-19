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
package com.blackbuild.klum.cast.docs.onboarding

import com.blackbuild.klum.cast.validation.AstSpec
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Issue
import spock.lang.See
import spock.lang.Tag

@Issue('28')
@Tag('documentary')
@See('https://github.com/klum-dsl/klum-cast/blob/main/docs/user/check-user.md#2-use-it-in-groovy-source')
class RoleBasedOnboardingDocumentaryTest extends AstSpec {

    def "a valid setter-shaped use compiles"() {
        when:
        loader.parseClass '''
package fixture

import com.blackbuild.klum.cast.docs.onboarding.DomainSetter

class ValidService {
    @DomainSetter
    void setName(String name) {}
}
''', 'ValidSetterExample.groovy'

        then:
        noExceptionThrown()
    }

    def "an invalid use reports the custom diagnostic at the target method"() {
        when:
        loader.parseClass '''
package fixture

import com.blackbuild.klum.cast.docs.onboarding.DomainSetter

class InvalidService {
    @DomainSetter
    void rename(String name) {}
}
''', 'InvalidSetterExample.groovy'

        then:
        def failure = thrown(MultipleCompilationErrorsException)
        failure.message.contains("[example.method-name.invalid-prefix] Method rename must start with 'set'")
        failure.message.contains('InvalidSetterExample.groovy: 7:')
        failure.message.contains('@DomainSetter')
    }
}
