package com.dropbox.gradle.plugins.dependencyguard.internal.tree

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.getResolvedComponentResult
import com.dropbox.gradle.plugins.dependencyguard.internal.projectConfigurations
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyGuardTreeDiffer
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.Tasks.declareCompatibilities
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

internal abstract class DependencyTreeDiffTask : DefaultTask() {

    private lateinit var dependencyGuardTreeDiffer: DependencyGuardTreeDiffer

    init {
        group = DependencyGuardPlugin.DEPENDENCY_GUARD_TASK_GROUP

        this.doLast {
            dependencyGuardTreeDiffer.performDiff()
        }
    }

    @get:Input
    abstract val resolvedComponentResult: Property<ResolvedComponentResult>

    fun setParams(
        project: Project,
        configurationName: String,
        shouldBaseline: Boolean,
    ) {
        ConfigurationValidators.validateConfigurationsAreAvailable(
            project,
            listOf(configurationName)
        )

        this.dependencyGuardTreeDiffer = DependencyGuardTreeDiffer(
            project = project,
            configurationName = configurationName,
            shouldBaseline = shouldBaseline,
        )

        val resolvedComponentResult = project.projectConfigurations.getResolvedComponentResult(configurationName)
        this.resolvedComponentResult.set(resolvedComponentResult)

        declareCompatibilities()
    }

    @TaskAction
    fun generate() {
        // USES INTERNAL API
        val asciiRenderer = AsciiDependencyReportRenderer2()

        val resolvedComponentResult = resolvedComponentResult.get()
        asciiRenderer.setOutputFile(dependencyGuardTreeDiffer.buildDirOutputFile)
        asciiRenderer.prepareVisit()
        asciiRenderer.render(resolvedComponentResult)
    }
}
