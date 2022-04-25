package com.dropbox.gradle.plugins.dependencyguard

import org.gradle.api.Action

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
     *     artifactReport = true
     *     moduleReport = false
     *   }
     * }
     */
    public fun configuration(name: String, config: Action<DependencyGuardConfiguration>) {
        val configuration = DependencyGuardConfiguration(name)
        config.execute(configuration)
        configurations.add(configuration)
    }
}
