package org.carlmontrobotics.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import spock.lang.Specification
import spock.lang.Unroll


class TaskWithEnv extends DefaultTask {
    public TaskWithEnv() {
    }

    public Map<String, Object> env = [:]

    def environment(String name, String value) {
        env[name] = value
        return this
    }

    Map<String, Object> getEnvironment() {
        return env
    }
}

/**
 * Unit test for the 'org.carlmontrobotics.gradle.webots' plugin.
 */
class WebotsPluginTest extends Specification {
    def project = ProjectBuilder.builder().build()

    def setup() {
    }

    def "plugin adds webots extension named webots"() {
        given:
        project.plugins.apply("org.carlmontrobotics.webots")

        expect:
        project.extensions.findByName("webots") instanceof WebotsExtension
    }

    def "plugin adds flat repository with name ending with /lib/controller/java"() {
        given:
        project.plugins.apply("org.carlmontrobotics.webots")

        when:
        def matches = 0
        def fcDeps = project.repositories
            .withType(FlatDirectoryArtifactRepository)
            .each({ FlatDirectoryArtifactRepository fdar -> 
                fdar.dirs.each({ File f ->
                    if (f.path.endsWith("/lib/controller/java".replace('/', File.separator))) {
                        matches++
                    }
                })
            })

        then:
        matches == 1
    }

    def "plugin adds implementation dependency on Controller.jar"() {
        given:
        project.plugins.apply("org.carlmontrobotics.webots")

        when:
        def matches = 0
        def fcDeps = project.configurations.getByName("implementation").dependencies
            .withType(FileCollectionDependency, { FileCollectionDependency fcd ->
                matches += fcd.files.filter({ File f -> 
                    return f.name == "Controller.jar"
                }).size()
            })

        then:
        matches == 1
    }

    @Unroll
    def "on #os plugin appends to #var with value of #orig"(String os, String var, String orig, String expected) {
        def fakeWebotsHome = new File("wh")

        given:
        project.ext.set(WebotsPlugin.SYS_FOR_TESTING_PROP, Stub(Sys) { 
            getenv("WEBOTS_HOME") >> { fakeWebotsHome.path }
            pathExists(fakeWebotsHome.path) >> true
            osIsLinux() >> { os == "Linux" }
            osIsMacOsX() >> { os == "MacOS" }
            osIsWindows() >> { os == "Windows" }
        })

        def task = project.task('taskWithEnv', type: TaskWithEnv)
        if (orig != null)
            task.env[var] = orig

        when:
        project.plugins.apply("org.carlmontrobotics.webots")

        then:
        task.env[var] == expected

        where:
        os        | var                 | orig        | expected
        "Linux"   | "LD_LIBRARY_PATH"   | null        | "wh/lib/controller:wh/lib/controller/java:wh/lib/webots"
        "Linux"   | "LD_LIBRARY_PATH"   | "/orig/lib" | "/orig/lib:wh/lib/controller:wh/lib/controller/java:wh/lib/webots"
        "MacOS"   | "DYLD_LIBRARY_PATH" | null        | "wh/lib/controller:wh/lib/controller/java:wh/lib/webots"
        "MacOS"   | "DYLD_LIBRARY_PATH" | "/orig/lib" | "/orig/lib:wh/lib/controller:wh/lib/controller/java:wh/lib/webots"
        "Windows" | "PATH"              | null        | "wh/lib/controller;wh/lib/controller/java;wh/lib/webots;wh/msys64/mingw64/bin;wh/msys64/mingw64/bin/cpp"
        "Windows" | "PATH"              | "/orig/lib" | "/orig/lib;wh/lib/controller;wh/lib/controller/java;wh/lib/webots;wh/msys64/mingw64/bin;wh/msys64/mingw64/bin/cpp"
    }
}
