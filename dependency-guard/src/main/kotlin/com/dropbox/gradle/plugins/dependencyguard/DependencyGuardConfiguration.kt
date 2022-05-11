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
    public var modules: Boolean = false

    /**
     * Whether to create the dependency tree report
     *
     * false by default because while valuable for debugging, get very noisy
     */
    @get:Input
    public var tree: Boolean = false

    /**
     * Rule to determine if a dependency will be allowed.
     *
     * TODO: not sure how to model this as a task input. May not matter since the task that uses it
     *  can never be up-to-date.
     */
    @get:Input
    public var allowRule: (dependencyName: String) -> Boolean = { true }

    /**
     * Modify a dependency name or remove it (by returning null) from the baseline file
     */
    @get:Input
    public var baselineMap: (dependencyName: String) -> String? = { it }
}
