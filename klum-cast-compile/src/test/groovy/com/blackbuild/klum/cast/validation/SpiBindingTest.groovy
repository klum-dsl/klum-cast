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
package com.blackbuild.klum.cast.validation

class SpiBindingTest extends AstSpec {

    def "nested typed check and stateless filter run end to end"() {
        given:
        createClass '''
package fixture

import com.blackbuild.klum.cast.spi.*
import org.codehaus.groovy.ast.ClassNode
import java.lang.annotation.*
import java.util.List

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CheckBinding(value = TypedConstraint.NestedCheck, filters = [TypedConstraint.ClassOnly])
@interface TypedConstraint {
    static class NestedCheck implements Check {
        List<Diagnostic> check(CheckContext context) {
            com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.typed = context.controlAnnotation.get().annotationType().simpleName
            []
        }
    }
    static class ClassOnly implements ApplicabilityFilter {
        boolean appliesTo(CheckContext context) { context.target instanceof ClassNode }
    }
}
'''
        createClass '''
package fixture
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@TypedConstraint
@interface TypedValidated {}
'''

        when:
        createClass '''
package fixture
@TypedValidated
class TypedTarget {}
'''

        then:
        valueHolder.typed == 'TypedConstraint'
    }

    def "name-bound check can be resolved from a separate consumer classloader"() {
        given:
        createClass '''
package splitmeta
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidator(value = "splitimpl.NamedCheck", validForType = "splitimpl.NameFilter")
@interface NamedConstraint {}
'''
        newClassLoader()
        createClass '''
package splitimpl
import com.blackbuild.klum.cast.spi.*
import org.codehaus.groovy.ast.ClassNode
import java.util.List

class NamedCheck implements Check {
    List<Diagnostic> check(CheckContext context) {
        com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder.named = context.controlAnnotation.get().annotationType().simpleName
        []
    }
}

class NameFilter implements ApplicabilityFilter {
    boolean appliesTo(CheckContext context) { context.target instanceof ClassNode }
}
'''
        newClassLoader()
        createClass '''
package splituse
import splitmeta.NamedConstraint
import com.blackbuild.klum.cast.*
import java.lang.annotation.*

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NamedConstraint
@interface SplitValidated {}
'''

        when:
        createClass '''
package splituse
@SplitValidated
class SplitTarget {}
'''

        then:
        valueHolder.named == 'NamedConstraint'
    }
}
