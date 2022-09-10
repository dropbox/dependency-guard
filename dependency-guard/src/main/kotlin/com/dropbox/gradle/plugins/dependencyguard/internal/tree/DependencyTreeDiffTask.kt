package com.dropbox.gradle.plugins.dependencyguard.internal.tree

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyGuardTreeDiffer
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.Tasks.declareCompatibilities
import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.DependencyReportTask

internal open class DependencyTreeDiffTask : DependencyReportTask(), TreeDiffTask {

    private lateinit var dependencyGuardTreeDiffer: DependencyGuardTreeDiffer

    init {
        group = DependencyGuardPlugin.DEPENDENCY_GUARD_TASK_GROUP

        this.doLast {
            dependencyGuardTreeDiffer.performDiff()
        }
    }

    override fun setParams(
        project: Project,
        configurationName: String,
        shouldBaseline: Boolean,
    ) {
        ConfigurationValidators.validateConfigurationsAreAvailable(
            project,
            listOf(configurationName)
        )

        super.setConfiguration(configurationName)

        this.dependencyGuardTreeDiffer = DependencyGuardTreeDiffer(
            project = project,
            configurationName = configurationName,
            shouldBaseline = shouldBaseline,
        )

        this.outputFile = dependencyGuardTreeDiffer.buildDirOutputFile

        declareCompatibilities()
    }
}
