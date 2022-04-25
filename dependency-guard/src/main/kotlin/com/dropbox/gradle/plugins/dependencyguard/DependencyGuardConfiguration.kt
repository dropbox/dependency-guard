package com.dropbox.gradle.plugins.dependencyguard

/**
 * Configuration for [DependencyGuardPlugin]
 */
public class DependencyGuardConfiguration(
    /**
     * Name of the build configuration
     *
     * See all possibilities by running Gradle's built in ./gradlew project:dependencies
     */
    public val configurationName: String,

    /** Whether to include artifacts in the dependency list report */
    public var artifacts: Boolean = true,

    /** Whether to include modules in the dependency list report */
    public var modules: Boolean = true,

    /**
     * Whether to create the dependency tree report
     *
     * false by default because while valuable for debugging, get very noisy
     */
    public var tree: Boolean = false,

    /** Whether or not to allow a dependency */
    public var isAllowed: (dependencyName: String) -> Boolean = { true },
)
