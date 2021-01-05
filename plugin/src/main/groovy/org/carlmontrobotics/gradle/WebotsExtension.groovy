package org.carlmontrobotics.gradle

import groovy.transform.CompileStatic
import org.gradle.internal.os.OperatingSystem // TODO: Use something else
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.file.ConfigurableFileCollection

import org.slf4j.LoggerFactory

import javax.inject.Inject

@CompileStatic
trait Sys {
    String getenv(String key) { System.getenv(key) }
    boolean osIsLinux() { OperatingSystem.current().isLinux() }
    boolean osIsMacOsX() { OperatingSystem.current().isMacOsX() }
    boolean osIsWindows() { OperatingSystem.current().isWindows() }
    boolean pathExists(String p) { (new File(p)).exists() }
}

class DefaultSys implements Sys {}

/**
 * Gradle project extension for Webots
 */
@CompileStatic
class WebotsExtension {

    String home
    ConfigurableFileCollection nativeLibs

    final Project project

    @Inject
    WebotsExtension(Project project) {
        this.project = project
        Sys sys = new DefaultSys()
        if (project.hasProperty(WebotsPlugin.SYS_FOR_TESTING_PROP)) {
            sys = (Sys)project.findProperty(WebotsPlugin.SYS_FOR_TESTING_PROP)
        }

        def log = LoggerFactory.getLogger('webots-plugin-logger')
        String envDelim = sys.osIsWindows() ? ";" : ":"
        home = ""
        List<String> dirs_to_check = new ArrayList<String>()

        def webots_home_env = sys.getenv("WEBOTS_HOME")
        if (webots_home_env) {
            dirs_to_check.add(webots_home_env)
        }

        if (dirs_to_check.size() == 0) {
            log.info "WEBOTS_HOME environment variable is not set, so looking for Webots installation."
            if (sys.osIsLinux()) {
                dirs_to_check.add "/usr/local/webots"
                dirs_to_check.add "/snap/webots/current/usr/share/webots"
            } else if (sys.osIsMacOsX()) {
                dirs_to_check.add sys.getenv("HOME") + "/Applications/Webots.app"
                dirs_to_check.add "/Applications/Webots.app"
            } else if (sys.osIsWindows()) {
                dirs_to_check.add sys.getenv("USER_HOME") + "/Webots"
                dirs_to_check.add sys.getenv("LOCALAPPDATA") + "/Programs/Webots"
            }
        }
        for (d in dirs_to_check) {
            log.info "Checking '" + d + "'"

            if (sys.pathExists(d)) {
                log.info "Found it!"
                home = d
                break
            }
        }

        if (home == "") {
            log.warn "Can't find Webots installation. To build and run a Webots extern controller, install Webots and, if necessary, set WEBOTS_HOME environment variable ."
            return
        }

        def ldpath = home + "/lib/controller"
        nativeLibs = project.files()

        addNativeLibsIn(ldpath)
        if (sys.osIsWindows()) {
            addNativeLibsIn(home + "/msys64/mingw64/bin")
            addNativeLibsIn(home + "/msys64/mingw64/bin/cpp")
        }
    }
    
    void addNativeLibsIn(String libDir) {
        def dllPatterns = ["**/*.so*", "**/*.so", "**/*.dll", "**/*.dylib", "**/*.jnilib"]
        nativeLibs.from project.fileTree([ dir: libDir, includes: dllPatterns])
    }
}
