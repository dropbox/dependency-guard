package com.dropbox.gradle.plugins.dependencyguard.internal

import org.gradle.configurationcache.extensions.capitalized

internal object DependencyTreeDiffTaskNames {

    fun createDependencyTreeTaskNameForConfiguration(configurationName: String): String {
        return "dependencyTreeDiff${configurationName.capitalized()}"
    }

    fun createDependencyTreeBaselineTaskNameForConfiguration(configurationName: String): String {
        return "dependencyTreeDiffBaseline${configurationName.capitalized()}"
    }
}
