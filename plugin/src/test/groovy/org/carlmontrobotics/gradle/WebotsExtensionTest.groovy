package org.carlmontrobotics.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.apache.commons.io.FileUtils
import spock.lang.Specification
import spock.lang.Unroll


/**
 * Unit test for WebotsExtension
 */
class WebotsExtensionTest extends Specification {
    @Unroll
    def "on #os nativeLibs returns collection of native libs"(String os) {
        given:
        def fakeWebotsHome = new File("build/tmp/extensiontest/fakewebotshome")
        def project = ProjectBuilder.builder().build()

        Sys sys = Stub() { 
            getenv("WEBOTS_HOME") >> { fakeWebotsHome.path }
            pathExists(fakeWebotsHome.path) >> true
            osIsLinux() >> { os == "Linux" }
            osIsMacOsX() >> { os == "MacOS" }
            osIsWindows() >> { os == "Windows" }

        }

        when:
        def webots = new WebotsExtension(project, sys)

        then:
        def dllDir = new File(fakeWebotsHome, "lib/controller")
        def msys64Dir = new File(fakeWebotsHome, "msys64/mingw64/bin")
        def msys64CppDir = new File(fakeWebotsHome, "msys64/mingw64/bin/cpp")
        def String[] includes = [
            "**/*.so", "**/*.so*", "**/*.dll", "**/*.dylib", "**/*.jnilib"
            ]

        webots.nativeLibs.from.stream().filter({ ConfigurableFileTree ft -> 
            ft.dir.path.endsWith(dllDir.path) && ft.includes.containsAll(includes)
        }).count() == 1
        os != "Windows" || webots.nativeLibs.from.stream().filter({ ConfigurableFileTree ft -> 
                ft.dir.path.endsWith(msys64Dir.path) && ft.includes.containsAll(includes)
            }).count() == 1
        os != "Windows" || webots.nativeLibs.from.stream().filter({ ConfigurableFileTree ft -> 
                ft.dir.path.endsWith(msys64CppDir.path) && ft.includes.containsAll(includes)
            }).count() == 1

        where:
            os << ["Linux", "MacOsX", "Windows"]
    }

    def "home uses WEBOTS_HOME if set"() {
        given:
        def project = ProjectBuilder.builder().build()
        Sys sys = Stub() { 
            getenv("WEBOTS_HOME") >> "/path/to/webots"
            pathExists("/path/to/webots") >> true
        }

        when:
        def webots = new WebotsExtension(project, sys)

        then:
        webots.home == "/path/to/webots"
    }

    @Unroll
    def "on #os home uses #path if webots found there"(String os, String path, Map<String, String> env) {
        given:
        def project = ProjectBuilder.builder().build()
        Sys sys = Stub() { 
            getenv(_) >> { String key -> env[key] }
            osIsLinux() >> { os == "Linux" }
            osIsMacOsX() >> { os == "MacOS" }
            osIsWindows() >> { os == "Windows" }
            pathExists(_) >> { String p -> p == path }
        }

        when:
        def webots = new WebotsExtension(project, sys)

        then:
        webots.home == path

        where:
        os        | path                                        | env
        "Linux"   | "/usr/local/webots"                         | [:]
        "MacOS"   | "/Applications/Webots.app"                  | [:]
        "Windows" | "/Users/home/Webots"                        | [USER_HOME: "/Users/home"]
        "Windows" | "/Users/name/AppData/Local/Programs/Webots" | [LOCALAPPDATA: "/Users/name/AppData/Local"]
    }

}
