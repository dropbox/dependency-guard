package com.dropbox.gradle.plugins.dependencyguard.fixture

import java.nio.file.Files
import java.nio.file.Path

class SimpleProject : AbstractProject() {

  companion object {
    // See build.gradle
    private val pluginVersion = System.getProperty("pluginVersion").toString()
  }

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
        plugins {
          id 'com.gradle.enterprise' version '3.10'
        }
      }
      
      plugins {
        id 'com.gradle.enterprise'
      }
      
      dependencyResolutionManagement {
        repositories {
          mavenCentral()
        }
      }
      
      gradleEnterprise {
        buildScan {
          publishAlways()
          termsOfServiceUrl = 'https://gradle.com/terms-of-service'
          termsOfServiceAgree = 'yes'
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
        configuration('compileClasspath')
      }
      """.trimIndent()
    )

    return lib
  }

  private fun Path.writeText(content: String) {
    Files.write(this, content.toByteArray())
  }

  private fun Path.createDirectories(): Path {
    return Files.createDirectories(this)
  }
}
