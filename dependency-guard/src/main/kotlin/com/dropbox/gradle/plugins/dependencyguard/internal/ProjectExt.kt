package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import org.gradle.api.Project

internal fun Project.isRootProject(): Boolean = this == rootProject

internal fun getQualifiedBaselineTaskForProjectPath(path: String): String {
    val separator = if (path == ":") "" else ":"
    return "${path}${separator}${DependencyGuardPlugin.DEPENDENCY_GUARD_BASELINE_TASK_NAME}"
}