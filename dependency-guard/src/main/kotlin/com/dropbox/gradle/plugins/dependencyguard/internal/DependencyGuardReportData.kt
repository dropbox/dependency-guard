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
     * Transform or remove (with null) dependencies in the baseline
     */
    val baselineMap: (dependencyName: String) -> String?,
) {

    private val artifactDeps: List<ArtifactDependency> = dependencies
        .filterIsInstance<ArtifactDependency>()
        .distinct()
        .sortedBy { it.name }

    private val moduleDeps: List<ModuleDependency> = dependencies
        .filterIsInstance<ModuleDependency>()
        .distinct()
        .sortedBy { it.name }

    private val allDeps: List<Dependency> = mutableListOf<Dependency>().apply {
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

    val disallowed: List<Dependency> = dependencies
        .filter { !allowRule(it.name) }
        .distinct()
        .sortedBy { it.name }

    private fun List<Dependency>.toReportString(): String {
        val deps = this
        return StringBuilder()
            .apply {
                deps
                    .forEach {
                        val mappedName = baselineMap(it.name)
                        if (mappedName != null) {
                            appendLine(mappedName)
                        }
                    }
            }.toString()
    }

    val artifactDepsReport: String = artifactDeps.toReportString()

    fun reportForConfig(artifacts: Boolean, modules: Boolean): String {
        return allDepsReport(artifacts, modules)
    }
}
