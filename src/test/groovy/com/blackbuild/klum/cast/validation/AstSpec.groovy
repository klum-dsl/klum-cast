package com.blackbuild.klum.cast.validation

import org.codehaus.groovy.control.CompilerConfiguration
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Specification

import java.lang.annotation.Annotation

abstract class AstSpec extends Specification {

    static AstSpec currentTest

    @Rule TestName testName = new TestName()
    ClassLoader oldLoader
    GroovyClassLoader loader
    CompilerConfiguration compilerConfiguration

    Map<String, ?> valueHolder = [:]

    def setup() {
        oldLoader = Thread.currentThread().contextClassLoader
        compilerConfiguration = new CompilerConfiguration()
        loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), compilerConfiguration)
        Thread.currentThread().contextClassLoader = loader
        def outputDirectory = new File("build/test-classes/${getClass().simpleName}/$safeFilename")
        outputDirectory.deleteDir()
        outputDirectory.mkdirs()
        compilerConfiguration.targetDirectory = outputDirectory
        currentTest = this
    }

    def getSafeFilename() {
        testName.methodName.replaceAll("\\W+", "_")
    }

    def cleanup() {
        Thread.currentThread().contextClassLoader = oldLoader
        currentTest = null
    }

    Class<?> createClass(@Language("groovy") String code) {
        return loader.parseClass(code)
    }

    Class<? extends Annotation> createAnnotation(@Language("groovy") String code) {
        createClass """
import com.blackbuild.klum.cast.*
import com.blackbuild.klum.cast.checks.*
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass("com.blackbuild.klum.cast.validation.KlumCastTransformation")
@KlumCastValidated
$code
"""
    }

    Class<?> getClass(String classname) {
        loader.loadClass(classname)
    }
}