package com.dropbox.gradle.plugins.dependencyguard.internal.tree

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyGuardTreeDiffer
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.BuildEnvironmentReportTask
import org.gradle.api.tasks.diagnostics.internal.DependencyReportRenderer
import org.gradle.api.tasks.diagnostics.internal.ReportGenerator
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer
import java.io.File

internal open class BuildEnvironmentDependencyTreeDiffTask : BuildEnvironmentReportTask(), TreeDiffTask {

    private val configurationName: String = ScriptHandler.CLASSPATH_CONFIGURATION

    private var shouldBaseline: Boolean = false

    private var outputFile: File? = null

    // USES INTERNAL API
    private val asciiRenderer: DependencyReportRenderer = AsciiDependencyReportRenderer()

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
        this.outputFile = dependencyGuardTreeDiffer.buildDirOutputFile
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
        println("Dependency Guard Tree baseline created for : for configuration classpath.")
        println("File: ${outputFile!!.canonicalPath}")
    }

    // USES INTERNAL API
    private fun reportGenerator(): ReportGenerator {
        return ReportGenerator(
            asciiRenderer,
            clientMetaData,
            outputFile!!,
            textOutputFactory,
        )
    }
}
