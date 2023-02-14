package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.file.Directory

internal class DependencyGuardTreeDiffer(
    private val configurationName: String,
    private val shouldBaseline: Boolean,
    private val projectDirOutputFile: File,
    private val buildDirOutputFile: File,
    private val projectPath: String,
) {
    companion object {
        fun projectDirOutputFile(projectDirectory: Directory, configurationName: String): File =
            OutputFileUtils.projectDirOutputFile(
                projectDirectory = projectDirectory,
                configurationName = configurationName,
                reportType = DependencyGuardReportType.TREE,
            )


        fun buildDirOutputFile(buildDirectory: Directory, configurationName: String): File = OutputFileUtils.buildDirOutputFile(
            buildDirectory = buildDirectory,
            configurationName = configurationName,
            reportType = DependencyGuardReportType.TREE,
        )
    }

    @Suppress("NestedBlockDepth")
    fun performDiff() {
        val baseline: String? = readBaselineFile()
        val generatedTree = readGeneratedFile()
            ?: throw GradleException("Tree should have been generated. Something went wrong.")

        if (shouldBaseline || baseline == null) {
            writeBaselineFile(generatedTree)

            ColorTerminal.printlnColor(
                ColorTerminal.ANSI_YELLOW,
                "Dependency Guard Tree baseline created for $projectPath for configuration $configurationName."
            )
            ColorTerminal.printlnColor(
                ColorTerminal.ANSI_YELLOW,
                "File: file://${projectDirOutputFile.canonicalPath}"
            )
        } else {
            val diff = dependencyTreeDiff(baseline, generatedTree)

            if (diff.isNotBlank()) {
                ColorTerminal.printlnColor(ColorTerminal.ANSI_YELLOW, Messaging.dependencyChangeDetected)

                // splitting string using lines() function
                diff.lines().forEach {
                    val ansiColor = if (it.startsWith("-")) {
                        ColorTerminal.ANSI_RED
                    } else if (it.startsWith("+")) {
                        ColorTerminal.ANSI_GREEN
                    } else {
                        null
                    }
                    ColorTerminal.printlnColor(ansiColor, it)
                }

                throw GradleException(
                    """
                        ${Messaging.dependencyChangeDetected}
                        Dependency Tree comparison to baseline does not match.
                        
                        ${Messaging.rebaselineMessage(projectPath)}
                    """.trimIndent()
                )
            }
        }
    }

    private fun readGeneratedFile(): String? = readFileContents(buildDirOutputFile)

    private fun readBaselineFile(): String? = readFileContents(projectDirOutputFile)

    private fun writeBaselineFile(generatedTree: String) {
        projectDirOutputFile.writeText(generatedTree)
    }

    private fun readFileContents(file: File): String? {
        if (file.exists()) {
            val text = file.readText()
            if (text.isNotBlank()) {
                return text
            }
        }
        return null
    }
}
