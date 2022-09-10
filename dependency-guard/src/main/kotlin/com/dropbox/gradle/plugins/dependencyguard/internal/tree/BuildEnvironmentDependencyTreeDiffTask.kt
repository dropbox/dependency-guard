package com.dropbox.gradle.plugins.dependencyguard.internal.tree

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyGuardTreeDiffer
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.Tasks.declareCompatibilities
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.BuildEnvironmentReportTask
import org.gradle.api.tasks.diagnostics.internal.DependencyReportRenderer
import org.gradle.api.tasks.diagnostics.internal.ReportGenerator
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer
import java.io.File

internal open class BuildEnvironmentDependencyTreeDiffTask : BuildEnvironmentReportTask(),
    TreeDiffTask {

    private var outputFile: File? = null

    // USES INTERNAL API
    private val asciiRenderer: DependencyReportRenderer = AsciiDependencyReportRenderer()

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

        this.dependencyGuardTreeDiffer = DependencyGuardTreeDiffer(
            project = project,
            configurationName = ScriptHandler.CLASSPATH_CONFIGURATION,
            shouldBaseline = shouldBaseline,
        )
        this.outputFile = dependencyGuardTreeDiffer.buildDirOutputFile

        declareCompatibilities()
    }

    @TaskAction
    override fun generate() {
        reportGenerator().generateReport(
            setOf(project)
        ) { project: Project ->
            val configuration = project.buildscript.configurations
                .getByName(ScriptHandler.CLASSPATH_CONFIGURATION)
            asciiRenderer.startConfiguration(configuration)
            asciiRenderer.render(configuration)
            asciiRenderer.completeConfiguration(configuration)
        }
    }

    // USES INTERNAL Gradle API - Issue: https://github.com/dropbox/dependency-guard/issues/25
    private fun reportGenerator(): ReportGenerator {
        return ReportGenerator(
            asciiRenderer,
            clientMetaData,
            outputFile!!,
            textOutputFactory,
        )
    }
}
