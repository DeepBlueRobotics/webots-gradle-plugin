package org.carlmontrobotics.gradle

import groovy.transform.CompileStatic
import org.gradle.internal.os.OperatingSystem // TODO: Use something else
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.slf4j.LoggerFactory

/**
 * Gradle plugin to support Webots
 */
@CompileStatic
class WebotsPlugin implements Plugin<Project> {
    void apply(Project project) {
        WebotsExtension webots = project.extensions.create("webots", WebotsExtension, project)

        project.plugins.apply("java")

        def ldpath = webots.home + "/lib/controller"
        project.repositories.flatDir([ dirs: ldpath + "/java"])
        def controllerJar = project.files(ldpath + "/java/Controller.jar")
        project.dependencies.add("implementation", controllerJar)
    }
}
