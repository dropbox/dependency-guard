package com.dropbox.gradle.plugins.dependencyguard.internal

internal object DependencyTreeDiffTaskNames {

    /**
     * This extension [org.gradle.configurationcache.extensions.capitalized]
     * is not available until 7.4.x, so this is a backport.
     *
     * Fixes: https://github.com/dropbox/dependency-guard/issues/40
     */
    fun String.capitalized(): String {
        return if (this.isEmpty()) {
            ""
        } else {
            val firstChar = get(0)
            if (firstChar.isUpperCase()) {
                return this
            } else {
                firstChar.toUpperCase() + substring(1)
            }
        }
    }

    fun createDependencyTreeTaskNameForConfiguration(configurationName: String): String {
        return "dependencyTreeDiff${configurationName.capitalized()}"
    }

    fun createDependencyTreeBaselineTaskNameForConfiguration(configurationName: String): String {
        return "dependencyTreeDiffBaseline${configurationName.capitalized()}"
    }
}
