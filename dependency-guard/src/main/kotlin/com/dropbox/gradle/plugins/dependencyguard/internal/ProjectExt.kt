package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.result.ResolvedComponentResult

internal fun Project.isRootProject(): Boolean = this == rootProject

internal fun getQualifiedBaselineTaskForProjectPath(path: String): String {
    val separator = if (path == ":") "" else ":"
    return "${path}${separator}${DependencyGuardPlugin.DEPENDENCY_GUARD_BASELINE_TASK_NAME}"
}

internal val Project.projectConfigurations: ConfigurationContainer
    get() = if (isRootProject()) {
        buildscript.configurations
    } else {
        configurations
    }

internal fun ConfigurationContainer.getResolvedComponentResult(name: String): ResolvedComponentResult = this
    .getByName(name)
    .incoming
    .resolutionResult
    .root