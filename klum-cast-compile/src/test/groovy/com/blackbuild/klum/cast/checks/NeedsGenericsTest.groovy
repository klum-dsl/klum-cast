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

class NeedsGenericsTest extends AstSpec {

    @Override
    def setup() {
        createClass '''
import java.lang.annotation.ElementType
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.FIELD])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NeedsGenerics
@interface MyAnnotation {}
'''
    }

    def "Works if Generics are specified"() {

            when: 'on field'
            createClass '''
class MyClass {
    @MyAnnotation
    List<String> myField
}'''
            then:
            notThrown(MultipleCompilationErrorsException)

            when: 'type is subtype'
            createClass '''
@MyAnnotation
class MyClass extends ArrayList<String> {
}'''
            then:
            notThrown(MultipleCompilationErrorsException)

            when: 'on method'
            createClass '''
class MyClass {
    @MyAnnotation
    List<String> doIt(String value, Closure<String> closure) {}
}'''
            then:
            notThrown(MultipleCompilationErrorsException)
    }

    def "Fails if Generics are not specified"() {

            when: 'on field'
            createClass '''
class MyClass {
    @MyAnnotation
    List myField
}'''
            then:
            thrown(MultipleCompilationErrorsException)

            when: 'type is subtype'
            createClass '''
@MyAnnotation
class MyClass extends ArrayList {
}'''
            then:
            thrown(MultipleCompilationErrorsException)

            when: 'on method'
            createClass '''
class MyClass {
    @MyAnnotation
    List doIt(String value, Closure closure) {}
}'''
            then:
            thrown(MultipleCompilationErrorsException)
    }

    def "Works if Generics are specified with bounds"() {
        given:
        createClass '''
import java.lang.annotation.ElementType
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.FIELD])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NeedsGenerics(allowBounds = true)
@interface MyAnnotationWithBounds {}
'''

        when:
        createClass '''
class MyClass {
    @MyAnnotation
    List<? extends CharSequence> myField
}'''
        then:
        thrown(MultipleCompilationErrorsException)

        when:
        createClass '''
class MyClass {
    @MyAnnotationWithBounds
    List<? extends CharSequence> myField
}'''
        then:
        notThrown(MultipleCompilationErrorsException)
    }

}