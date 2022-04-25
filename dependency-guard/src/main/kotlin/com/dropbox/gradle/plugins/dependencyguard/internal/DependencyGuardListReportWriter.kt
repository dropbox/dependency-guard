package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.internal.utils.ColorTerminal
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyListDiff
import java.io.File

internal class DependencyGuardListReportWriter(
    private val artifacts: Boolean,
    private val modules: Boolean
) {

    /**
     * @return Whether changes were detected
     */
    internal fun writeReport(
        buildDirOutputFile: File,
        projectDirOutputFile: File,
        report: DependencyGuardReportData,
        shouldBaseline: Boolean,
        errorHandler: (String) -> Unit,
    ): Boolean {
        val type: DependencyGuardReportType = DependencyGuardReportType.ALL
        val reportContent = report.reportForConfig(
            artifacts = artifacts,
            modules = modules
        )

        buildDirOutputFile.writeText(report.artifactDepsReport)

        val projectDirOutputFileExists = projectDirOutputFile.exists()
        return if (shouldBaseline || !projectDirOutputFileExists) {
            writeBaseline(
                type = type,
                content = reportContent,
                projectPath = report.projectPath,
                configurationName = report.configurationName,
                baselineFile = projectDirOutputFile
            )
            false
        } else {
            // Perform Diff
            DependencyListDiff.performDiff(
                type = type,
                projectPath = report.projectPath,
                configurationName = report.configurationName,
                projectDirOutputFile = projectDirOutputFile,
                depsReportString = reportContent,
                errorHandler = errorHandler
            )
        }
    }

    private fun writeBaseline(
        type: DependencyGuardReportType,
        content: String,
        projectPath: String,
        configurationName: String,
        baselineFile: File
    ) {
        // Write Baseline
        ColorTerminal.printlnColor(
            ColorTerminal.ANSI_YELLOW,
            StringBuilder()
                .apply {
                    appendLine("Dependency Guard $type baseline created for $projectPath for configuration $configurationName.")
                    appendLine("File: ${baselineFile.canonicalPath}")
                }
                .toString()
        )
        baselineFile.writeText(content)
    }
}
