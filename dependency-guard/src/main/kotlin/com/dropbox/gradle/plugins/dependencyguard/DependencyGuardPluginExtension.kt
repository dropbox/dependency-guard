package com.dropbox.gradle.plugins.dependencyguard

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

/**
 * Extension for [DependencyGuardPlugin] which leverages [DependencyGuardConfiguration]
 */
public open class DependencyGuardPluginExtension @Inject constructor(
    private val objects: ObjectFactory
) {

    internal val configurations = objects.domainObjectContainer(DependencyGuardConfiguration::class.java)

    public fun configuration(name: String) {
        configurations.add(newConfiguration(name))
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
        configurations.add(newConfiguration(name, config))
    }

    // We cannot use `configurations.create(name, config)` because the config block is executed
    // after the `all {}` block in the plugin. This is silly, but a reasonable workaround.
    private fun newConfiguration(
        name: String,
        config: Action<DependencyGuardConfiguration>? = null
    ): DependencyGuardConfiguration {
        return objects.newInstance(DependencyGuardConfiguration::class.java, name).apply {
            // execute the config block immediately, then add it to the container
            config?.execute(this)
        }
    }
}
