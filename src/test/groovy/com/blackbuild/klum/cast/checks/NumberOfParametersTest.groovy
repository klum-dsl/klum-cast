package com.blackbuild.klum.cast.checks

import com.blackbuild.klum.cast.validation.AstSpec
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class NumberOfParametersTest extends AstSpec {

    def "NumberOfParameterCheck is correct"() {
        given:
        createAnnotation '''
@NumberOfParameters(1)
@interface MyAnnotation {}
'''

        when:
        createClass '''
class MyClass {
    @MyAnnotation
    void myMethod(int value) {}
}
'''

        then:
        noExceptionThrown()
    }

    def "NumberOfParameterCheck is wrong"() {
        given:
        createAnnotation'''
@NumberOfParameters(0)
@interface MyAnnotation {}
'''

        when:
        createClass '''
class MyClass {
    @MyAnnotation
    void myMethod(int value) {}
}
'''

        then:
        thrown(MultipleCompilationErrorsException)
    }


}