package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.models.ArtifactDependency
import com.dropbox.gradle.plugins.dependencyguard.models.Dependency
import com.dropbox.gradle.plugins.dependencyguard.models.ModuleDependency

internal data class DependencyGuardReportData(
    val projectPath: String,
    val configurationName: String,
    val dependencies: List<Dependency>,
    val isAllowed: (dependencyName: String) -> Boolean,
) {
    private val artifactDeps = dependencies
        .filterIsInstance<ArtifactDependency>()
        .distinct()
        .sortedBy { it.name }

    private val moduleDeps = dependencies
        .filterIsInstance<ModuleDependency>()
        .distinct()
        .sortedBy { it.name }

    private val allDeps = mutableListOf<Dependency>().apply {
        addAll(moduleDeps)
        addAll(artifactDeps)
    }

    private fun allDepsReport(artifacts: Boolean, modules: Boolean): String = StringBuilder().apply {
        if (modules) {
            moduleDeps.toReportString().apply {
                if (this.isNotBlank()) {
                    append(this)
                }
            }
        }
        if (artifacts) {
            artifactDeps.toReportString().apply {
                if (this.isNotBlank()) {
                    append(this)
                }
            }
        }
    }.toString()

    val moduleDepsReport: String = moduleDeps.toReportString()

    val artifactDepsReport: String = artifactDeps.toReportString()

    val disallowed: List<Dependency> = dependencies
        .filter { !isAllowed(it.name) }
        .distinct()
        .sortedBy { it.name }

    private fun List<Dependency>.toReportString(): String {
        val deps = this
        return StringBuilder().apply {
            deps.forEach {
                appendLine(it.name)
            }
        }.toString()
    }

    fun reportForConfig(artifacts: Boolean, modules: Boolean): String {
        return allDepsReport(artifacts, modules)
    }
}
