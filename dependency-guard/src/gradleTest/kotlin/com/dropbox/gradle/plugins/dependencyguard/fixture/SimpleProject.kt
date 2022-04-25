package com.dropbox.gradle.plugins.dependencyguard.fixture

import com.dropbox.gradle.plugins.dependencyguard.util.createDirectories
import com.dropbox.gradle.plugins.dependencyguard.util.writeText
import java.nio.file.Path

class SimpleProject : AbstractProject() {

  private val gradlePropertiesFile = projectDir.resolve("gradle.properties")
  private val settingsFile = projectDir.resolve("settings.gradle")
  private val rootBuildFile = projectDir.resolve("build.gradle")

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

    // Generate java library project named 'lib'
    val lib = minimalLibrary("lib")
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
        implementation 'commons-io:commons-io:2.11.0' 
      }
      
      dependencyGuard {
        configuration('compileClasspath') {
          tree = true
        }
      }
      """.trimIndent()
    )

    return lib
  }
}
