package com.dropbox.gradle.plugins.dependencyguard.fixture

import com.dropbox.gradle.plugins.dependencyguard.util.createDirectories
import com.dropbox.gradle.plugins.dependencyguard.util.writeText
import java.nio.file.Path

class ConfiguredProject(
    private val configurations: List<String>,
) : AbstractProject() {

    private val gradlePropertiesFile = projectDir.resolve("gradle.properties")
    private val settingsFile = projectDir.resolve("settings.gradle")
    private val rootBuildFile = projectDir.resolve("build.gradle")

    // Generate java library project named 'lib'
    val lib = minimalLibrary("lib")

    init {
        gradlePropertiesFile.writeText(
            """
        org.gradle.jvmargs=-Dfile.encoding=UTF-8
      """.trimIndent()
        )

        settingsFile.writeText(
            """
      pluginManagement {
        repositories {
          gradlePluginPortal()
          mavenCentral()
        }
      }
      
      dependencyResolutionManagement {
        repositories {
          mavenCentral()
        }
      }
      
      rootProject.name = 'project-under-test'
      
      // Don't forget to add all your modules here!
      include ':lib'
      """.trimIndent()
        )
    }

    private fun minimalLibrary(name: String): Path {
        val lib = projectDir.resolve(name).createDirectories()

        lib.resolve("build.gradle").writeText(
            """
      plugins {
        id 'java-library'
        id 'com.dropbox.dependency-guard'
      }
      
      dependencies {
        implementation 'io.reactivex.rxjava3:rxjava:3.1.4'
      }
      
      dependencyGuard {
         ${
                configurations.map {
                    """
                 configuration('$it') {
                   tree = true
                 }
                 """.trimIndent()
                }
            }
      }
      """.trimIndent()
        )

        return lib
    }
}
