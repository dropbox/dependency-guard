package com.dropbox.gradle.plugins.dependencyguard.internal

import org.gradle.configurationcache.extensions.capitalized

internal object BuildEnvironmentDependencyTreeDiffTaskNames {

    fun createBuildEnvironmentDependencyTreeTaskNameForConfiguration(configurationName: String): String {
        return "dependencyTreeDiff${configurationName.capitalized()}"
    }

    fun createBuildEnvironmentDependencyTreeBaselineTaskNameForConfiguration(configurationName: String): String {
        return "dependencyTreeDiffBaseline${configurationName.capitalized()}"
    }
}
