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