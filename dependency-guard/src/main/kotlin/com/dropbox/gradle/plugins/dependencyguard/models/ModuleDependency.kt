package com.dropbox.gradle.plugins.dependencyguard.models

internal data class ModuleDependency(
    val path: String,
) : Dependency {
    override val name: String get() = path
}
