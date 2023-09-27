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

class ParameterTypesTest extends AstSpec {

    @Override
    def setup() {
        createClass '''
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@ParameterTypes([String, Number])
@interface MyAnnotation {
}'''
    }

    def "method with correct parameter types works"() {
        when:
        createClass '''
class MyClass {
    @MyAnnotation
    void myMethod(String s, Integer i) {}
}'''
        then:
        notThrown(MultipleCompilationErrorsException)
    }

    def "method with wrong parameter types fails"() {
        when:
        createClass '''
class MyClass {
    @MyAnnotation
    void myMethod(Integer i, String s) {}
}'''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "additional parameter types are allowed"() {
        when:
        createClass '''
class MyClass {
    @MyAnnotation
    void myMethod(String s, Integer i, Boolean b) {}
}
'''
        then:
        notThrown(MultipleCompilationErrorsException)
    }

    def "missing parameter types are allowed"() {
        when:
        createClass '''
class MyClass {
    @MyAnnotation
    void myMethod(String s) {}
}
'''
        then:
        notThrown(MultipleCompilationErrorsException)
    }

}