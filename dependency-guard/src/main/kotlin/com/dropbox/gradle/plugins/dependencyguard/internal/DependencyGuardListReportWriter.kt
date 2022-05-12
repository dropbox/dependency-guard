package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.internal.utils.ColorTerminal
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyListDiff
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.DependencyListDiffResult
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
    ): DependencyListDiffResult {
        val type: DependencyGuardReportType = DependencyGuardReportType.LIST
        val reportContent = report.reportForConfig(
            artifacts = artifacts,
            modules = modules
        )

        buildDirOutputFile.writeText(reportContent)

        val projectDirOutputFileExists = projectDirOutputFile.exists()
        return if (shouldBaseline || !projectDirOutputFileExists) {
            writeBaseline(
                type = type,
                content = reportContent,
                projectPath = report.projectPath,
                configurationName = report.configurationName,
                baselineFile = projectDirOutputFile
            )
            DependencyListDiffResult.BaselineCreated
        } else {
            val expectedFileContent = projectDirOutputFile.readText()
            // Perform Diff
            val diffResult = DependencyListDiff.performDiff(
                projectPath = report.projectPath,
                configurationName = report.configurationName,
                expectedDependenciesFileContent = expectedFileContent,
                actualDependenciesFileContent = reportContent
            )
            diffResult
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
                    appendLine("File: file://${baselineFile.canonicalPath}")
                }
                .toString()
        )
        baselineFile.writeText(content)
    }
}
