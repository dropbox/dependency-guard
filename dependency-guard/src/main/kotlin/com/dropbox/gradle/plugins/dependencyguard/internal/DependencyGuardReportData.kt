package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.models.ArtifactDependency
import com.dropbox.gradle.plugins.dependencyguard.models.Dependency
import com.dropbox.gradle.plugins.dependencyguard.models.ModuleDependency

internal data class DependencyGuardReportData(
    val projectPath: String,
    val configurationName: String,
    val dependencies: List<Dependency>,
    val allowRule: (dependencyName: String) -> Boolean,
    /**
     * Includes dependency in baseline file when "true"
     */
    val baselineFilter: (dependencyName: String) -> Boolean,
) {
    private val artifactDeps = dependencies
        .filterIsInstance<ArtifactDependency>()
        .distinct()
        .filter { baselineFilter(it.name) } // Remove dependencies from the baseline using this filter
        .sortedBy { it.name }

    private val moduleDeps = dependencies
        .filterIsInstance<ModuleDependency>()
        .distinct()
        .filter { baselineFilter(it.name) } // Remove dependencies from the baseline using this filter
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

    @Deprecated("This will be removed as the module and artifact reports were combined")
    val moduleDepsReport: String = moduleDeps.toReportString()

    val artifactDepsReport: String = artifactDeps
        .toReportString()

    val disallowed: List<Dependency> = dependencies
        .filter { !allowRule(it.name) }
        .distinct()
        .sortedBy { it.name }

    private fun List<Dependency>.toReportString(): String {
        val deps = this
        return StringBuilder().apply {
            deps
                .forEach {
                    appendLine(it.name)
                }
        }.toString()
    }

    fun reportForConfig(artifacts: Boolean, modules: Boolean): String {
        return allDepsReport(artifacts, modules)
    }
}
