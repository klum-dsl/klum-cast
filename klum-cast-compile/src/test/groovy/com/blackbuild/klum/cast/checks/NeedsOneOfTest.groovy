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

class NeedsOneOfTest extends AstSpec {

    def "No exclusive, non restricted"() {
        given:
        createAnnotation '''
@Target(ElementType.TYPE)
@NeedsOneOf(["m1", "m2"])
@interface MyAnnotation {
    String m1() default "";
    String m2() default "";
    String m3() default "";
    String m4() default "";
}'''

        when:
        createClass '''
@MyAnnotation(m1 = "bla")
class MyClass {}
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(m1 = "m1", m2 = "m2")
class MyClass {}
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(m3 = "m3")
class MyClass {}
'''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "exclusive, non restricted"() {
        given:
        createAnnotation '''
@Target(ElementType.TYPE)
@NeedsOneOf(value = ["m1", "m2"], exclusive = true)
@interface MyAnnotation {
    String m1() default "";
    String m2() default "";
    String m3() default "";
    String m4() default "";
}'''

        when:
        createClass '''
@MyAnnotation(m1 = "bla")
class MyClass {}
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(m1 = "m1", m2 = "m2")
class MyClass {}
'''
        then:
        thrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(m3 = "m3")
class MyClass {}
'''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "non exclusive, restricted"() {
        given:
        createAnnotation '''
@Target([ElementType.TYPE, ElementType.METHOD])
@NeedsOneOf.List([ // no repeatable annotations in groovy 2.4
    @NeedsOneOf(whenOn = ElementType.TYPE, value = ["m1", "m2"]),
    @NeedsOneOf(whenOn = ElementType.METHOD, value = ["m3", "m4"])
])
@interface MyAnnotation {
    String m1() default "";
    String m2() default "";
    String m3() default "";
    String m4() default "";
}'''

        when:
        createClass '''
@MyAnnotation(m1 = "bla")
class MyClass {}
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(m1 = "m1", m3 = "m3")
class MyClass {}
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
class MyClass {
    @MyAnnotation(m3 = "m3")
    void method() {}
}
'''
        then:
        notThrown(MultipleCompilationErrorsException)
    }

}