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
//file:noinspection GrPackage
package com.blackbuild.klum.cast.validation

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class BasicTest extends AstSpec {

    def "validator is executed"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.*
@DummyPassAnnotation
class MyClass {}
'''

        then:
        valueHolder.runs.size() == 1
    }

    def "validator fails"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.*
@DummyFailAnnotation
class MyClass {}
'''

        then:
        thrown(MultipleCompilationErrorsException)
        valueHolder.runs.size() == 1
    }

    def "implementation is in different classloader than annotation"() {
        given:
        createClass '''
package annotations 

import java.lang.annotation.*
import com.blackbuild.klum.cast.*
import com.blackbuild.klum.cast.checks.*

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator("checks.MyValidator")
@interface MyAnnotation {
}
'''
        newClassLoader()

        createClass '''
//file:noinspection UnnecessaryQualifiedReference
package checks

import annotations.MyAnnotation
import com.blackbuild.klum.cast.checks.*
import org.codehaus.groovy.ast.*
import com.blackbuild.klum.cast.validation.*

class MyValidator extends KlumCastCheck<MyAnnotation> {
    @Override
    protected void doCheck(AnnotationNode annotationToCheck, AnnotatedNode target) {
        AstSpec.currentTest.valueHolder["executed"] = true
    }
}
'''
        when:
        loader.parent.loadClass("annotations.MyAnnotation")

        then:
        noExceptionThrown()

        when:
        loader.parent.loadClass("checks.MyValidator")

        then:
        thrown(ClassNotFoundException)

        when:
        newClassLoader()
        createClass '''
package schema

import annotations.MyAnnotation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.*
import com.blackbuild.klum.cast.*
import com.blackbuild.klum.cast.checks.*

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@MyAnnotation
@interface MySchemaAnnotation {}
'''
        createClass '''
package schema

@MySchemaAnnotation
class MyClass {}
'''
        then:
        noExceptionThrown()
        valueHolder.executed == true
    }

    def "direct validator passes"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.*

@DummyDirectValidated("PASS")
class MyClass {}
'''

        then:
        noExceptionThrown()
        valueHolder.runs.size() == 1
    }

    def "direct validator fails"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.*

@DummyDirectValidated("FAIL")
class MyClass {}
'''

        then:
        thrown(MultipleCompilationErrorsException)
        valueHolder.runs.size() == 1
    }
}