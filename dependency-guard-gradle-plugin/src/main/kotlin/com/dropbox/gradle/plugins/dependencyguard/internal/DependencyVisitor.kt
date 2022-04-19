package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.models.ArtifactDependency
import com.dropbox.gradle.plugins.dependencyguard.models.Dependency
import com.dropbox.gradle.plugins.dependencyguard.models.ModuleDependency
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal object DependencyVisitor {

    fun ResolvedDependencyResult.toDep(): Dependency? {
        return when (val componentIdentifier = selected.id) {
            is ProjectComponentIdentifier -> ModuleDependency(
                path = componentIdentifier.projectPath,
            )
            is ModuleComponentIdentifier -> {
                ArtifactDependency(
                    group = componentIdentifier.group,
                    artifact = componentIdentifier.module,
                    version = componentIdentifier.version,
                )
            }
            else -> {
                null
            }
        }
    }

    private fun visit(
        reportData: MutableList<Dependency>,
        resolvedDependencyResults: Collection<ResolvedDependencyResult>
    ) {
        for (resolvedDependencyResult: ResolvedDependencyResult in resolvedDependencyResults) {
            resolvedDependencyResult.toDep()
                ?.let { dep: Dependency ->
                    if (!reportData.contains(dep)) {
                        reportData.add(dep)
                        visit(
                            reportData = reportData,
                            resolvedDependencyResults = resolvedDependencyResult
                                .selected
                                .dependencies
                                .filterIsInstance<ResolvedDependencyResult>()
                        )
                    }
                }
        }
    }

    fun traverseDependenciesForConfiguration(config: Configuration): List<Dependency> {
        val incomingResolvableDependencies = config.incoming
        val resolvedComponentResult = incomingResolvableDependencies.resolutionResult.root
        val firstLevelDependencies = resolvedComponentResult
            .dependencies
            .filterIsInstance<ResolvedDependencyResult>()

        val dependencies = mutableListOf<Dependency>()
        visit(dependencies, firstLevelDependencies)
        return dependencies
    }
}
