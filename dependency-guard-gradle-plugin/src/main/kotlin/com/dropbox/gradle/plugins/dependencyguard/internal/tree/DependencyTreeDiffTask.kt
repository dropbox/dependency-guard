package com.dropbox.gradle.plugins.dependencyguard.internal.tree

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyGuardTreeDiffer
import org.gradle.api.tasks.diagnostics.DependencyReportTask

internal open class DependencyTreeDiffTask : DependencyReportTask(), TreeDiffTask {

    private var configurationName: String = ""

    private var shouldBaseline: Boolean = false

    private val dependencyGuardTreeDiffer by lazy {
        DependencyGuardTreeDiffer(
            project = project,
            configurationName = configurationName,
            shouldBaseline = shouldBaseline,
        )
    }

    init {
        group = DependencyGuardPlugin.DEPENDENCY_GUARD_TASK_GROUP
        this.doFirst {
            ConfigurationValidators.validateConfigurationsAreAvailable(
                project,
                listOf(configurationName)
            )
        }
        this.doLast {
            dependencyGuardTreeDiffer.performDiff()
        }
    }
    override fun setParams(configurationName: String, shouldBaseline: Boolean) {
        this.shouldBaseline = shouldBaseline
        this.configurationName = configurationName
        super.setConfiguration(configurationName)
        this.outputFile = dependencyGuardTreeDiffer.buildDirOutputFile
    }
}
