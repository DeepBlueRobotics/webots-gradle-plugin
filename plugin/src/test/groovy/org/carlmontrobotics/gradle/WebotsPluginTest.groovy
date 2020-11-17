package org.carlmontrobotics.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import spock.lang.Specification

/**
 * Unit test for the 'org.carlmontrobotics.gradle.webots' plugin.
 */
class WebotsPluginTest extends Specification {
    def project = ProjectBuilder.builder().build()

    def setup() {
        project.plugins.apply("org.carlmontrobotics.webots")
    }

    def "plugin adds webots extension named webots"() {
        expect:
        project.extensions.findByName("webots") instanceof WebotsExtension
    }

    def "plugin adds flat repository with name ending with /lib/controller/java"() {
        when:
        def matches = 0
        def fcDeps = project.repositories
            .withType(FlatDirectoryArtifactRepository)
            .each({ FlatDirectoryArtifactRepository fdar -> 
                fdar.dirs.each({ File f ->
                    if (f.path.endsWith("/lib/controller/java")) {
                        matches++
                    }
                })
            })

        then:
        matches == 1
    }

    def "plugin adds implementation dependency on Controller.jar"() {
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
}
