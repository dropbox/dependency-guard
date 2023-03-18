package com.dropbox.gradle.plugins.dependencyguard.fixture

import com.dropbox.gradle.plugins.dependencyguard.util.writeText

class EmptyProject : AbstractProject() {

    private val gradlePropertiesFile = projectDir.resolve("gradle.properties")
    private val settingsFile = projectDir.resolve("settings.gradle")
    private val rootBuildFile = projectDir.resolve("build.gradle")

    init {
        gradlePropertiesFile.writeText(
            // language=INI
            """
            org.gradle.jvmargs=-Dfile.encoding=UTF-8
            """.trimIndent()
        )

        settingsFile.writeText(
            // language=Groovy
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
            """.trimIndent()
        )
        rootBuildFile.writeText(
            // language=Groovy
            """
            plugins {
                id 'com.dropbox.dependency-guard'
            }
            """.trimIndent()
        )
    }

}
