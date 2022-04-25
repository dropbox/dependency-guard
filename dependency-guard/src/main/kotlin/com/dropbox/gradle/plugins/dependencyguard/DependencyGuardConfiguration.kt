package com.dropbox.gradle.plugins.dependencyguard

import org.gradle.api.Named
import org.gradle.api.tasks.Input
import javax.inject.Inject

/**
 * Configuration for [DependencyGuardPlugin]
 */
public open class DependencyGuardConfiguration @Inject constructor(
    /**
     * Name of the build configuration
     *
     * See all possibilities by running Gradle's built in ./gradlew project:dependencies
     */
    @get:Input
    public val configurationName: String,
) : Named {
    @Input
    public override fun getName(): String = configurationName

    /** Whether to include artifacts in the dependency list report */
    @get:Input
    public var artifacts: Boolean = true

    /** Whether to include modules in the dependency list report */
    @get:Input
    public var modules: Boolean = true

    /**
     * Whether to create the dependency tree report
     *
     * false by default because while valuable for debugging, get very noisy
     */
    @get:Input
    public var tree: Boolean = false

    /**
     * Whether to allow a dependency.
     *
     * TODO: not sure how to model this as a task input. May not matter since the task that uses it
     *  can never be up-to-date.
     */
    public var isAllowed: (dependencyName: String) -> Boolean = { true }
}
