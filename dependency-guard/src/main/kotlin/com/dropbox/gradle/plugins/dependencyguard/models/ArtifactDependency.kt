package com.dropbox.gradle.plugins.dependencyguard.models

internal data class ArtifactDependency(
    val group: String,
    val artifact: String,
    val version: String,
) : Dependency {
    override val name: String get() = "$group:$artifact:$version"
}
