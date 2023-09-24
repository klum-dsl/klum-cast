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

class UniquePerClassTest extends AstSpec {

    @Override
    def setup() {
        createClass '''
import com.blackbuild.klum.cast.*
import com.blackbuild.klum.cast.checks.*
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD, ElementType.TYPE])
@KlumCastValidated
@UniquePerClass
@interface ShouldBeUnique {}
'''
    }

    def "works on single occurrence"() {
        when:
        createClass '''
class MyClass {
    @ShouldBeUnique
    int myField
}'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@ShouldBeUnique
class MyClass {
    int myField
}'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
class MyClass {
    @ShouldBeUnique
    int myMethod() {}
}'''
        then:
        notThrown(MultipleCompilationErrorsException)
    }

    def "fails on multiple fields"() {
        when:
        createClass '''
class MyClass {
    @ShouldBeUnique
    int myField
    @ShouldBeUnique
    int myField2
}'''

        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "fails on field and method"() {
        when:
        createClass '''
class MyClass {
    @ShouldBeUnique
    int myField
    @ShouldBeUnique
    int myMethod() {}
}'''

        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "fails on class and field"() {
        when:
        createClass '''
@ShouldBeUnique
class MyClass {
    @ShouldBeUnique
    int myField
    int myMethod() {}
}'''

        then:
        thrown(MultipleCompilationErrorsException)
    }
}
