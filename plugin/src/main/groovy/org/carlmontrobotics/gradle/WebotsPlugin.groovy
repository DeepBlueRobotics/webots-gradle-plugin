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

    static final String SYS_FOR_TESTING_PROP = "_sysForTesting"
    Sys sys
    
    void apply(Project project) {
        sys = new DefaultSys()
        if (project.hasProperty(SYS_FOR_TESTING_PROP)) {
            sys = (Sys)project.findProperty(SYS_FOR_TESTING_PROP)
        }

        WebotsExtension webots = project.extensions.create("webots", WebotsExtension, project)

        project.plugins.apply("java")

        def ldpath = webots.home + "/lib/controller"
        project.repositories.flatDir([ dirs: ldpath + "/java"])
        def controllerJar = project.files(ldpath + "/java/Controller.jar")
        project.dependencies.add("implementation", controllerJar)
        project.tasks.matching({ task -> 
            (task.metaClass.respondsTo(task, "environment", String, String) 
            && task.metaClass.respondsTo(task, "getEnvironment"))
        }).all { GroovyObject t -> 
            if (sys.osIsLinux()) {
                addToDllPath(t, "LD_LIBRARY_PATH", ":", [
                    ldpath,
                    ldpath + "/java",
                    webots.home + "/lib/webots"])
            } else if (sys.osIsMacOsX()) {
                addToDllPath(t, "DYLD_LIBRARY_PATH", ":", [
                    ldpath,
                    ldpath + "/java",
                    webots.home + "/lib/webots"])
            } else if (sys.osIsWindows()) {
                addToDllPath(t, "PATH", ";", [
                    ldpath,
                    ldpath + "/java",
                    webots.home+"/lib/webots",
                    webots.home + "/msys64/mingw64/bin",
                    webots.home + "/msys64/mingw64/bin/cpp"])
            }
        }
    }

    void addToDllPath(GroovyObject t, String dllPathVarName, String sep, List<String> paths) {
        List<String> newPaths = []
        def orig = t.metaClass.pickMethod("getEnvironment").invoke(t)[dllPathVarName];
        if (orig != null)
            newPaths.add(0, (String)orig)
        newPaths.addAll(paths)
        t.metaClass.pickMethod("environment", String, String).invoke(t, dllPathVarName, String.join(sep, newPaths))
    }
}
