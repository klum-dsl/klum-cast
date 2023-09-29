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
package com.blackbuild.klum.cast.checks

import com.blackbuild.klum.cast.validation.AstSpec
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class AlsoNeedsTest extends AstSpec {

    @Override
    def setup() {
        createClass '''

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@interface MyAnnotation {
    @AlsoNeeds(['aux1', 'aux2']) String main1()
    @AlsoNeeds(['aux2', 'aux3']) String main2()
    String aux1()
    String aux2()
    String aux3()
}'''
    }

    def "Has one necessary co member"() {
        when:
        createClass '''
@MyAnnotation(main1 = "main1", aux1 = "aux1")
class MyClass {}   
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(main1 = "main1", aux2 = "aux2")
class MyClass {}   
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(main1 = "main1", aux1 = "aux1", aux2 = "aux2")
class MyClass {}   
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(main1 = "main1", main2 = "main2", aux2 = "aux2")
class MyClass {}   
'''
        then:
        notThrown(MultipleCompilationErrorsException)
    }

    def "misses necessary co member"() {
        when:
        createClass '''
@MyAnnotation(main1 = "main1")
class MyClass {}   
'''
        then:
        thrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(main1 = "main1", aux3 = "aux3")
class MyClass {}   
'''
        then:
        thrown(MultipleCompilationErrorsException)
    }
}