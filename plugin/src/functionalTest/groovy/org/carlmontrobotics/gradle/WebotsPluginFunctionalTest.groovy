/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package org.carlmontrobotics.gradle

import spock.lang.Specification
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner

/**
 * Functional tests for the 'org.carlmontrobotics.webots' plugin.
 */
class WebotsPluginFunctionalTest extends Specification {
    def "finds dlls in Webots installation"() { 
        given:
        def projectDir = new File("build/functionalTest")
        FileUtils.deleteDirectory(projectDir)
        projectDir.mkdirs()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('org.carlmontrobotics.webots')
            }

            task('printWebotsNativeLibs') {
                webots.nativeLibs.each { File f ->
                    println f.name
                }
            }
        """
        def fakeWebotsHome = new File(projectDir, "fakewebotshome")
        def dllDir = new File(fakeWebotsHome, "lib/controller")
        dllDir.mkdirs()
        new File(dllDir, "fakelib1.dll") << ""
        new File(dllDir, "fakelib1.so") << ""
        new File(dllDir, "fakelib1.dylib") << ""
        new File(dllDir, "fakelib2.dll") << ""
        new File(dllDir, "fakelib2.sp") << ""
        new File(dllDir, "fakelib2.dylib") << ""

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withEnvironment([WEBOTS_HOME: fakeWebotsHome.absolutePath])
        runner.withArguments("clean", "printWebotsNativeLibs")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("fakelib1")
        result.output.contains("fakelib2")
    }

    def "warns about missing Webots installation"() { 
        given:
        def projectDir = new File("build/functionalTest")
        FileUtils.deleteDirectory(projectDir)
        projectDir.mkdirs()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('org.carlmontrobotics.webots')
            }

            task('printWebotsHome') {
                println webots.home
            }
        """

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withEnvironment([WEBOTS_HOME: "build/functionalTest/missingwebotshome"])
        runner.withArguments("printWebotsHome")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("Can't find Webots installation")
    }
}
