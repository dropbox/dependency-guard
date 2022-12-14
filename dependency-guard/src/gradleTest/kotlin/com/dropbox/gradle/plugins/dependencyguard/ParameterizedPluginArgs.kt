package com.dropbox.gradle.plugins.dependencyguard

import org.gradle.util.GradleVersion

data class ParameterizedPluginArgs(
    val gradleVersion: GradleVersion,
    val withConfigurationCache: Boolean,
)