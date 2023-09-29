package com.blackbuild.klum.cast.checks

import com.blackbuild.klum.cast.validation.AstSpec
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class NeedsReturnTypeTest extends AstSpec {

    def "Simple return type works"() {
        given:
        createClass '''
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NeedsReturnType(List)
@interface MyAnnotation {}
'''

            when: 'return type matches'
            createClass '''
class MyClass {
    @MyAnnotation
    List<String> myMethod() { null }
}'''
            then:
            notThrown(MultipleCompilationErrorsException)

            when: 'return type is subtype'
            createClass '''
class MyClass {
    @MyAnnotation
    ArrayList<String> myMethod() { null }
}'''
            then:
            notThrown(MultipleCompilationErrorsException)

            when: 'return type does not match'
            createClass '''
class MyClass {
    @MyAnnotation
    String myMethod() { null }
}'''
            then:
            thrown(MultipleCompilationErrorsException)
    }

    def "Void return type works"() {
        given:
        createClass '''
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NeedsReturnType(Void)
@interface MyAnnotation {}
'''

            when: 'return type matches'
            createClass '''
class MyClass {
    @MyAnnotation
    void myMethod() {}
}'''
            then:
            notThrown(MultipleCompilationErrorsException)

            when: 'return type is subtype'
            createClass '''
class MyClass {
    @MyAnnotation
    String myMethod() { null }
}'''
            then:
            thrown(MultipleCompilationErrorsException)
    }

    def "primitive return type works"() {
        given:
        createClass '''
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@KlumCastValidated
@NeedsReturnType(Integer)
@interface MyAnnotation {}
'''

            when: 'return type matches'
            createClass '''
class MyClass {
    @MyAnnotation
    int myMethod() { 0 }
}'''
            then:
            notThrown(MultipleCompilationErrorsException)
    }

}