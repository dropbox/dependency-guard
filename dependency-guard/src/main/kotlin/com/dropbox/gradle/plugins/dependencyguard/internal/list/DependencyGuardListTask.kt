package com.dropbox.gradle.plugins.dependencyguard.internal.list

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardConfiguration
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardListReportWriter
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportData
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyVisitor
import com.dropbox.gradle.plugins.dependencyguard.internal.isRootProject
import com.dropbox.gradle.plugins.dependencyguard.internal.qualifiedBaselineTaskName
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.ColorTerminal
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.OutputFileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.tasks.TaskAction

internal open class DependencyGuardListTask : DefaultTask() {

    init {
        group = DependencyGuardPlugin.DEPENDENCY_GUARD_TASK_GROUP
    }

    private lateinit var extension: DependencyGuardPluginExtension

    private var shouldBaseline: Boolean = false

    private val dependencyGuardConfigurations: MutableList<DependencyGuardConfiguration> get() = extension.configurations

    private fun generateReport(
        dependencyGuardConfiguration: DependencyGuardConfiguration
    ): DependencyGuardReportData {
        val configurationName = dependencyGuardConfiguration.configurationName

        var config = project.configurations.firstOrNull { it.name == configurationName }

        if (config == null) {
            if (project.isRootProject()) {
                // Assuming this is the root project
                config = project.buildscript.configurations.findByName(ScriptHandler.CLASSPATH_CONFIGURATION)
            }
            if (config == null) {
                throw GradleException("Could not find $configurationName in ${project.path}")
            }
        }

        val dependencies = DependencyVisitor.traverseDependenciesForConfiguration(config)

        return DependencyGuardReportData(
            projectPath = project.path,
            configurationName = configurationName,
            isAllowed = dependencyGuardConfiguration.isAllowed,
            dependencies = dependencies,
        )
    }

    @Suppress("NestedBlockDepth")
    @TaskAction
    internal fun execute() {
        ConfigurationValidators.validateConfigurationsAreAvailable(
            target = project,
            configurationNames = dependencyGuardConfigurations.map { it.configurationName }
        )

        val dependencyChangesDetectedInConfigurations = mutableListOf<String>()
        val reports = mutableListOf<DependencyGuardReportData>()
        dependencyGuardConfigurations.forEach { dependencyGuardConfig ->
            val report = generateReport(dependencyGuardConfig)
            reports.add(report)
        }

        val reportsWithDisallowedDependencies = reports.filter { it.disallowed.isNotEmpty() }
        if (reportsWithDisallowedDependencies.isNotEmpty()) {
            throwErrorAboutDisallowedDependencies(reportsWithDisallowedDependencies)
        }

        dependencyGuardConfigurations.forEach { dependencyGuardConfig ->
            val report = reports.firstOrNull { it.configurationName == dependencyGuardConfig.configurationName }
            report?.let {
                if (writeListReport(dependencyGuardConfig, report)) {
                    dependencyChangesDetectedInConfigurations.add(report.configurationName)
                }
            }
        }

        if (dependencyChangesDetectedInConfigurations.isNotEmpty()) {
            throwErrorAboutDetectedChanges(
                DependencyGuardReportType.ALL,
                dependencyChangesDetectedInConfigurations
            )
        }
    }

    private fun writeListReport(
        dependencyGuardConfig: DependencyGuardConfiguration,
        report: DependencyGuardReportData
    ): Boolean {
        val reportType = DependencyGuardReportType.ALL
        val reportWriter = DependencyGuardListReportWriter(
            artifacts = dependencyGuardConfig.artifacts,
            modules = dependencyGuardConfig.modules
        )
        return reportWriter.writeReport(
            buildDirOutputFile = OutputFileUtils.buildDirOutputFile(
                project = project,
                configurationName = report.configurationName,
                reportType = reportType,
            ),
            projectDirOutputFile = OutputFileUtils.projectDirOutputFile(
                project = project,
                configurationName = report.configurationName,
                reportType = reportType,
            ),
            report = report,
            shouldBaseline = shouldBaseline,
            errorHandler = { }
        )
    }

    private fun throwErrorAboutDisallowedDependencies(reportsWithDisallowedDependencies: List<DependencyGuardReportData>) {
        val errorMessage = StringBuilder().apply {
            reportsWithDisallowedDependencies.forEach { report ->
                val disallowed = report.disallowed
                appendLine(
                    """Disallowed Dependencies found in ${project.path} for the configuration "${report.configurationName}" """
                )
                disallowed.forEach {
                    appendLine("\"${it.name}\",")
                }
                appendLine()
            }
            appendLine("These dependencies are transitively included and must be removed based on the configured 'isAllowed' rules.")
            appendLine()
        }.toString()

        throw GradleException(errorMessage)
    }

    private fun throwErrorAboutDetectedChanges(
        type: DependencyGuardReportType,
        changesDetectedInConfigurations: List<String>
    ) {
        val errorMessage = StringBuilder().apply {
            appendLine("$type change for ${project.path} for the following configurations:")
            changesDetectedInConfigurations.forEach {
                appendLine("* $it")
            }
            appendLine()
            appendLine("$type comparison to baseline does not match.")
            appendLine("If this is a desired change, you can re-baseline using ./gradlew ${project.qualifiedBaselineTaskName()}")
        }.toString()
        throw GradleException(errorMessage)
    }

    internal fun setParams(extension: DependencyGuardPluginExtension, shouldBaseline: Boolean) {
        this.extension = extension
        this.shouldBaseline = shouldBaseline
    }
}
