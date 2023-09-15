package com.blackbuild.klum.cast.checks

import com.blackbuild.klum.cast.validation.AstSpec
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Requires

class AllowedMembersTest extends AstSpec {

    def "AllowedMembersCheck is correct"() {
        given:
        createAnnotation '''
@Target([ElementType.METHOD, ElementType.TYPE])
@AllowedMembers.List([ // no repeatable annotations in groovy 2.4
    @AllowedMembers(targets = ElementType.METHOD, members = "onMethod"),
    @AllowedMembers(targets = ElementType.TYPE, members = "onClass")
])
@interface MyAnnotation {
    String onMethod()
    String onClass()
}'''

        when:
        createClass '''
@MyAnnotation(onClass = "onClass")
class MyClassWTA {}
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
class MyClassWMA {
    @MyAnnotation(onMethod = "onMethod")
    void myMethod() {}
}'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
class MyClassWithWrongTypeAnno {
    @MyAnnotation(onClass = "onClass")
    void myMethod() {}
}'''
        then:
        thrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(onMethod = "onMethod")
class MyClassWithWrongTypeAnno {
}'''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    @Requires({!GroovySystem.version.startsWith("2.4")})
    def "AllowedMembersCheck is correct G2.5"() {
        given:
        createAnnotation '''
@Target([ElementType.METHOD, ElementType.TYPE])
@AllowedMembers.List([ // no repeatable annotations in groovy 2.4
    @AllowedMembers(targets = ElementType.METHOD, members = "onMethod"),
    @AllowedMembers(targets = ElementType.TYPE, members = "onClass")
])
@interface MyAnnotation {
    String onMethod()
    String onClass()
}'''

        when:
        createClass '''
@MyAnnotation(onClass = "onClass")
class MyClassWTA {}
'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
class MyClassWMA {
    @MyAnnotation(onMethod = "onMethod")
    void myMethod() {}
}'''
        then:
        notThrown(MultipleCompilationErrorsException)

        when:
        createClass '''
class MyClassWithWrongTypeAnno {
    @MyAnnotation(onClass = "onClass")
    void myMethod() {}
}'''
        then:
        thrown(MultipleCompilationErrorsException)

        when:
        createClass '''
@MyAnnotation(onMethod = "onMethod")
class MyClassWithWrongTypeAnno {
}'''
        then:
        thrown(MultipleCompilationErrorsException)
    }

}