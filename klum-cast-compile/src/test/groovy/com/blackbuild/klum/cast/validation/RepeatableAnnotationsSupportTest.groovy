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
package com.blackbuild.klum.cast.validation


import spock.lang.Requires

class RepeatableAnnotationsSupportTest extends AstSpec {
    Class A
    Class As

    @Override
    def setup() {
        given:
        createClass '''
package an

import java.lang.annotation.Documented
import java.lang.annotation.Repeatable
import java.lang.annotation.RetentionPolicy

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface As {
    A[] value()
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(As)
@Documented
@interface A {
}
'''
        A = getClass("an.A")
        As = getClass("an.As")

        importCustomizer.addImports("an.A", "an.As")
    }

    def "repeatable annotations are unwrapped with wrapper annotation"() {
        when:
        def C = createClass '''
@As([@A, @A])
class C {}
'''
        def anns = RepeatableAnnotationsSupport.unwrapAnnotations(C.getAnnotation(As)).toArray()

        then:
        anns.size() == 2
        anns.every { it.annotationType() == getClass("an.A") }
    }

    def "repeatable annotations are unwrapped with a single annotation"() {
        when:
        def C = createClass '''
@A
class C {}
'''
        def anns = RepeatableAnnotationsSupport.unwrapAnnotations(C.getAnnotation(A)).toArray()

        then:
        anns.size() == 1
        anns.every { it.annotationType() == getClass("an.A") }
    }

    @Requires({ !GroovySystem.version.startsWith("2.4") })
    def "repeatable annotations are unwrapped with a mutiple annotations w/o wrapper"() {
        when:
        def C = createClass '''
@A
@A
class C {}
'''
        def anns = RepeatableAnnotationsSupport.unwrapAnnotations(C.getAnnotation(As)).toArray()

        then:
        anns.size() == 2
        anns.every { it.annotationType() == getClass("an.A") }
    }

}
