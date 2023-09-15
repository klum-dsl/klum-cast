package com.blackbuild.klum.cast.validation

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class BasicTest extends AstSpec {

    @Override
    def cleanup() {
        DummyValidator.Checker.reset()
    }

    def "validator is executed"() {
        when:
        createClass '''
import com.blackbuild.klum.cast.validation.*
@DummyPassAnnotation
class MyClass {}
'''

        then:
        DummyValidator.Checker.runs.size() == 1
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
        DummyValidator.Checker.runs.size() == 1

    }
}