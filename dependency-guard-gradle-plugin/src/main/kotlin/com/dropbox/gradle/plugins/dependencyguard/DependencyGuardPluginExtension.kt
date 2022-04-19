package com.dropbox.gradle.plugins.dependencyguard

/**
 * Extension for [DependencyGuardPlugin] which leverages [DependencyGuardConfiguration]
 */
public open class DependencyGuardPluginExtension {
    internal val configurations = mutableListOf<DependencyGuardConfiguration>()

    public fun configuration(name: String) {
        val configuration = DependencyGuardConfiguration(name)
        configurations.add(configuration)
    }

    /**
     * Supports configuration in build files.
     *
     * dependencyGuard {
     *   configuration("normalReleaseRuntimeClasspath") {
     *     it.artifactReport = true
     *     it.moduleReport = false
     *   }
     * }
     */
    public fun configuration(name: String, config: DependencyGuardConfiguration.() -> Unit) {
        val configuration = DependencyGuardConfiguration(name).apply(config)
        configurations.add(configuration)
    }
}
