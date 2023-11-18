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

class BooleanOperationsTest extends AstSpec {

    def "all branches fail means the check fails"() {
        given:
        createClass '''import com.blackbuild.klum.cast.KlumCastValidated
import com.blackbuild.klum.cast.validation.DummyFailAnnotation

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Target

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch
@interface NonePass {
    DummyFailAnnotation fail() default @DummyFailAnnotation;
    DummyFailAnnotation fail2() default @DummyFailAnnotation;
    DummyFailAnnotation fail3() default @DummyFailAnnotation;
}
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NonePass
@interface MyAnnotation {}
'''

        when: "one branch passes"
        createClass '''
@MyAnnotation
class MyClass {}
'''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "one branch of an Or check is sufficient to pass"() {
        given:
        createClass '''import com.blackbuild.klum.cast.validation.*

import java.lang.annotation.ElementType
import java.lang.annotation.Target

@Target([ElementType.ANNOTATION_TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch
@interface OnePass {
    DummyFailAnnotation fail() default @DummyFailAnnotation;
    DummyFailAnnotation fail2() default @DummyFailAnnotation;
    DummyPassAnnotation pass() default @DummyPassAnnotation;
}

@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OnePass
@interface MyAnnotation {}
'''

        when: "one branch passes"
        createClass '''
@MyAnnotation
class MyClass {}
'''
        then:
        noExceptionThrown()
    }

    def "multiple branches of an Or check pass"() {
        given:
        createClass '''import com.blackbuild.klum.cast.validation.*

import java.lang.annotation.ElementType
import java.lang.annotation.Target

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@OneCheckMustMatch
@interface TwoPass {
    DummyFailAnnotation fail() default @DummyFailAnnotation;
    DummyPassAnnotation pass2() default @DummyPassAnnotation;
    DummyPassAnnotation pass() default @DummyPassAnnotation;
}
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@TwoPass
@interface MyAnnotation {}

'''

        when: "one branch passes"
        createClass '''
@MyAnnotation
class MyClass {}
'''
        then:
        noExceptionThrown()
    }



}