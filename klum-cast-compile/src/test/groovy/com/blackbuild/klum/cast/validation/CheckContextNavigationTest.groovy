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

import com.blackbuild.klum.cast.spi.BindingMetadata
import com.blackbuild.klum.cast.spi.Check
import com.blackbuild.klum.cast.spi.CheckBinding
import com.blackbuild.klum.cast.spi.CheckContext
import com.blackbuild.klum.cast.spi.Diagnostic
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class CheckContextNavigationTest extends AstSpec {

    def "parameter checks and filters receive the same enclosing declaration navigation"() {
        given:
        createClass '''
package fixture

import com.blackbuild.klum.cast.spi.*
import java.lang.annotation.*
import java.util.List

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@com.blackbuild.klum.cast.KlumCastValidator(
    value = 'fixture.NavigationCheck',
    validForType = 'fixture.NavigationFilter'
)
@interface NavigationConstraint {}

class NavigationRecorder {
    static void record(String recipient, CheckContext context) {
        def executable = context.enclosingExecutable.orElse(null)
        def holder = com.blackbuild.klum.cast.validation.AstSpec.currentTest.valueHolder
        holder.navigation = (holder.navigation ?: []) + [[
            recipient: recipient,
            target: context.target.name,
            executableType: executable?.class?.simpleName,
            executableName: executable?.name,
            declaringClass: context.declaringClass.map { it.name }.orElse(null)
        ]]
    }
}

class NavigationFilter implements ApplicabilityFilter {
    boolean appliesTo(CheckContext context) {
        NavigationRecorder.record('filter', context)
        true
    }
}

class NavigationCheck implements Check {
    List<Diagnostic> check(CheckContext context) {
        NavigationRecorder.record('check', context)
        []
    }
}

@Target([ElementType.PARAMETER, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@com.blackbuild.klum.cast.KlumCastValidated
@NavigationConstraint
@interface Navigated {}
'''

        when:
        createClass '''
package fixture

@Navigated
class NonParameterTarget {}

class ParameterTarget {
    ParameterTarget(@Navigated String constructorValue) {}

    void work(@Navigated String methodValue) {}
}
'''

        then:
        valueHolder.navigation.findAll { it.target == 'constructorValue' } == [
                [recipient: 'filter', target: 'constructorValue', executableType: 'ConstructorNode',
                 executableName: '<init>', declaringClass: 'fixture.ParameterTarget'],
                [recipient: 'check', target: 'constructorValue', executableType: 'ConstructorNode',
                 executableName: '<init>', declaringClass: 'fixture.ParameterTarget']
        ]
        valueHolder.navigation.findAll { it.target == 'methodValue' } == [
                [recipient: 'filter', target: 'methodValue', executableType: 'MethodNode',
                 executableName: 'work', declaringClass: 'fixture.ParameterTarget'],
                [recipient: 'check', target: 'methodValue', executableType: 'MethodNode',
                 executableName: 'work', declaringClass: 'fixture.ParameterTarget']
        ]
        valueHolder.navigation.findAll { it.target == 'fixture.NonParameterTarget' } == [
                [recipient: 'filter', target: 'fixture.NonParameterTarget', executableType: null,
                 executableName: null, declaringClass: null],
                [recipient: 'check', target: 'fixture.NonParameterTarget', executableType: null,
                 executableName: null, declaringClass: null]
        ]
    }

    def "manual context construction without an owner leaves declaration navigation empty"() {
        given:
        def declaration = LegacyNavigationBinding.getAnnotation(CheckBinding)
        def context = new CheckContext(
                new AnnotationNode(ClassHelper.make(CheckContextNavigationTest)),
                new Parameter(ClassHelper.STRING_TYPE, 'value'),
                null,
                null,
                new BindingMetadata(declaration, LegacyNavigationCheck, LegacyNavigationCheck.name),
                [],
                null
        )

        expect:
        context.enclosingExecutable.empty
        context.declaringClass.empty
    }
}

@CheckBinding(LegacyNavigationCheck)
@Retention(RetentionPolicy.RUNTIME)
@interface LegacyNavigationBinding {}

class LegacyNavigationCheck implements Check {
    @Override
    List<Diagnostic> check(CheckContext context) { [] }
}
