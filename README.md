# webots-gradle-plugin

![ci](https://github.com/DeepBlueRobotics/webots-gradle-plugin/workflows/ci/badge.svg)

A Gradle plugin to allow projects to build/run using the local Webots installation.

This plugin:

1. Looks for your local Webots installation in `$WEBOTS_HOME` and in the default installation location(s) for your platform.

2. Provides a `webots.home` property that is the path to the installation that was found.

3. Adds an `implementation` dependency on that installation's `Controller.jar`.

4. Provides a `webots.nativeLibs` property that is a `FileCollection` containing the native libraries that need to be loadable by an extern Webots controller. This can be useful if your build copies such libs to a single location referenced by the JVM's `java.library.path` system property.

