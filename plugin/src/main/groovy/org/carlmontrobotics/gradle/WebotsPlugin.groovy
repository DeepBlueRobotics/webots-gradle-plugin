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
        project.tasks.matching({ task -> task.metaClass.respondsTo(task, "environment", String, String) }).all { GroovyObject t -> 
            if (OperatingSystem.current().isLinux()) {
                t.metaClass.pickMethod("environment", String, String).invoke(t, "LD_LIBRARY_PATH", String.join(":",
                    System.getenv("LD_LIBRARY_PATH"),
                    ldpath,
                    ldpath + "/java",
                    webots.home + "/lib/webots"))
            } else if (OperatingSystem.current().isMacOsX()) {
                t.metaClass.pickMethod("environment", String, String).invoke(t, "DYLD_LIBRARY_PATH", String.join(":",
                    System.getenv("DYLD_LIBRARY_PATH"),
                    ldpath,
                    ldpath + "/java",
                    webots.home + "/lib/webots"))
            } else if (OperatingSystem.current().isWindows()) {
                t.metaClass.pickMethod("environment", String, String).invoke(t, "PATH", String.join(";",
                    System.getenv("PATH"),
                    ldpath,
                    ldpath + "/java",
                    webots.home+"/lib/webots",
                    webots.home + "/msys64/mingw64/bin",
                    webots.home + "/msys64/mingw64/bin/cpp"))
            }
        }
    }
}
