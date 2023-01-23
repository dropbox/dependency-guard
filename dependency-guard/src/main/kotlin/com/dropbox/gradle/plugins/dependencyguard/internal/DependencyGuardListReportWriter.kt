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
        projectDirOutputFile: File,
        report: DependencyGuardReportData,
        shouldBaseline: Boolean,
    ): DependencyListDiffResult {
        val reportContent = report.reportForConfig(
            artifacts = artifacts,
            modules = modules
        )

        val projectDirOutputFileExists = projectDirOutputFile.exists()
        return if (shouldBaseline || !projectDirOutputFileExists) {
            projectDirOutputFile.writeText(reportContent)
            return DependencyListDiffResult.BaselineCreated(
                projectPath = report.projectPath,
                configurationName = report.configurationName,
                baselineFile = projectDirOutputFile,
            )
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
}
