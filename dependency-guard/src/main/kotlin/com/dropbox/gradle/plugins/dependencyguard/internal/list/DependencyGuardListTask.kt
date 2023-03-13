package com.dropbox.gradle.plugins.dependencyguard.internal.list

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardConfiguration
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardListReportWriter
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportData
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyVisitor
import com.dropbox.gradle.plugins.dependencyguard.internal.getResolvedComponentResult
import com.dropbox.gradle.plugins.dependencyguard.internal.isRootProject
import com.dropbox.gradle.plugins.dependencyguard.internal.projectConfigurations
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyListDiffResult
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.OutputFileUtils
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.Tasks.declareCompatibilities
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class DependencyGuardListTask : DefaultTask() {

    init {
        group = DependencyGuardPlugin.DEPENDENCY_GUARD_TASK_GROUP
    }

    fun generateReportForConfiguration(
        projectPath: String,
        dependencyGuardConfiguration: DependencyGuardConfiguration,
        resolvedComponentResult: ResolvedComponentResult,
    ): DependencyGuardReportData {
        val configurationName = dependencyGuardConfiguration.configurationName

        val dependencies = DependencyVisitor.traverseComponentDependencies(resolvedComponentResult)

        return DependencyGuardReportData(
            projectPath = projectPath,
            configurationName = configurationName,
            allowedFilter = dependencyGuardConfiguration.allowedFilter,
            baselineMap = dependencyGuardConfiguration.baselineMap,
            dependencies = dependencies,
        )
    }

    @get:Input
    abstract val shouldBaseline: Property<Boolean>

    @get:Input
    abstract val forRootProject: Property<Boolean>

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val availableConfigurationNames: ListProperty<String>

    @get:Input
    abstract val monitoredConfigurationsMap: MapProperty<DependencyGuardConfiguration, Provider<ResolvedComponentResult>>

    @get:OutputDirectory
    abstract val projectDirectoryDependenciesDir: DirectoryProperty

    @Suppress("NestedBlockDepth")
    @TaskAction
    internal fun execute() {
        val dependencyGuardConfigurations = monitoredConfigurationsMap.get()

        // Validate Configuration
        ConfigurationValidators.validateListTaskConfiguration(
            projectPath = projectPath.get(),
            isForRootProject = forRootProject.get(),
            logger = logger,
            availableConfigurationNames = availableConfigurationNames.get(),
            monitoredConfigurations = dependencyGuardConfigurations.keys
        )

        // Execute Report
        val reports = dependencyGuardConfigurations.map {
            generateReportForConfiguration(
                projectPath = projectPath.get(),
                dependencyGuardConfiguration = it.key,
                resolvedComponentResult = it.value.get()
            )
        }

        // Throw Error if any Disallowed Dependencies are Found
        val reportsWithDisallowedDependencies = reports.filter { it.disallowed.isNotEmpty() }
        if (reportsWithDisallowedDependencies.isNotEmpty()) {
            throwExceptionAboutDisallowedDependencies(reportsWithDisallowedDependencies)
        }

        // Perform Diffs and Write Baselines
        val exceptionMessage = StringBuilder()
        dependencyGuardConfigurations.keys.forEach { dependencyGuardConfig ->
            val report = reports.firstOrNull { it.configurationName == dependencyGuardConfig.configurationName }
            report?.let {
                val diffResult: DependencyListDiffResult =
                    writeListReport(
                        dependencyGuardConfig = dependencyGuardConfig,
                        report = report,
                        shouldBaseline = shouldBaseline.get(),
                        projectDirectoryDependenciesDir = projectDirectoryDependenciesDir.get(),
                    )
                when (diffResult) {
                    is DependencyListDiffResult.DiffPerformed.HasDiff -> {
                        // Print to console in color
                        println(diffResult.createDiffMessage(withColor = true))

                        // Add to exception message without color
                        exceptionMessage.appendLine(diffResult.createDiffMessage(withColor = false))
                    }

                    is DependencyListDiffResult.DiffPerformed.NoDiff -> {
                        // Print no diff message
                        println(diffResult.noDiffMessage)
                    }

                    is DependencyListDiffResult.BaselineCreated -> {
                        println(diffResult.baselineCreatedMessage(true))
                    }
                }

                // If there was an exception message, throw an Exception with the message
                if (exceptionMessage.toString().isNotEmpty()) {
                    throw GradleException(exceptionMessage.toString())
                }
            }
        }
    }

    /**
     * @return Whether changes were detected
     */
    fun writeListReport(
        dependencyGuardConfig: DependencyGuardConfiguration,
        report: DependencyGuardReportData,
        shouldBaseline: Boolean,
        projectDirectoryDependenciesDir: Directory,
    ): DependencyListDiffResult {
        val reportType = DependencyGuardReportType.LIST
        val reportWriter = DependencyGuardListReportWriter(
            artifacts = dependencyGuardConfig.artifacts,
            modules = dependencyGuardConfig.modules
        )

        return reportWriter.writeReport(
            projectDirOutputFile = OutputFileUtils.projectDirOutputFile(
                projectDirectory = projectDirectoryDependenciesDir,
                configurationName = report.configurationName,
                reportType = reportType,
            ),
            report = report,
            shouldBaseline = shouldBaseline
        )
    }

    private fun throwExceptionAboutDisallowedDependencies(reportsWithDisallowedDependencies: List<DependencyGuardReportData>) {
        val errorMessage = buildString {
            reportsWithDisallowedDependencies.forEach { report ->
                val disallowed = report.disallowed
                appendLine(
                    """Disallowed Dependencies found in ${report.projectPath} for the configuration "${report.configurationName}" """
                )
                disallowed.forEach {
                    appendLine("\"${it.name}\",")
                }
                appendLine()
            }
            appendLine("These dependencies are included and must be removed based on the configured 'allowedFilter'.")
            appendLine()
        }

        throw GradleException(errorMessage)
    }

    internal fun setParams(
        target: Project,
        extension: DependencyGuardPluginExtension,
        shouldBaseline: Boolean,
        availableConfigurationNames: Collection<String>,
    ) {
        this.forRootProject.set(target.isRootProject())
        this.projectPath.set(target.path)
        this.monitoredConfigurationsMap.set(resolveMonitoredConfigurationsMap(target, extension.configurations))
        this.shouldBaseline.set(shouldBaseline)
        this.availableConfigurationNames.set(availableConfigurationNames)
        val projectDirDependenciesDir = OutputFileUtils.projectDirDependenciesDir(target)
        this.projectDirectoryDependenciesDir.set(projectDirDependenciesDir)

        declareCompatibilities()
    }

    private fun resolveMonitoredConfigurationsMap(
        project: Project,
        monitoredConfigurations: Collection<DependencyGuardConfiguration>,
    ): Map<DependencyGuardConfiguration, Provider<ResolvedComponentResult>> {
        val configurationContainer = project.projectConfigurations

        return monitoredConfigurations.associateWith {
            configurationContainer.getResolvedComponentResult(it.configurationName)
        }
    }
}
