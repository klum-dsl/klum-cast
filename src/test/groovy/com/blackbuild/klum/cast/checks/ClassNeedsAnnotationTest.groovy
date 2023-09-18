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

class ClassNeedsAnnotationTest extends AstSpec {

    def "ClassNeedsAnnotationCheck is correct"() {
        given:
        createClass '''
import com.blackbuild.klum.cast.*
import com.blackbuild.klum.cast.checks.*
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

@GroovyASTTransformationClass("com.blackbuild.klum.cast.validation.KlumCastTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface ClassAnnotation {}

@GroovyASTTransformationClass("com.blackbuild.klum.cast.validation.KlumCastTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@KlumCastValidated
@ClassNeedsAnnotation(ClassAnnotation)
@interface FieldAnnotation {}
'''
        when:
        createClass '''
@ClassAnnotation
class ClassWithAnnotation {
    @FieldAnnotation
    int myField
}'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''import groovy.transform.Field
class ClassWithoutAnnotation {
    @FieldAnnotation 
    int myField
}
'''
        then:
        thrown(MultipleCompilationErrorsException)
    }

}