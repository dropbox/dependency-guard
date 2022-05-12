package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import com.dropbox.gradle.plugins.dependencyguard.internal.qualifiedBaselineTaskName
import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.Project

internal class DependencyGuardTreeDiffer(
    private val project: Project,
    private val configurationName: String,
    private val shouldBaseline: Boolean,
) {

    private val projectDirOutputFile: File by lazy {
        OutputFileUtils.projectDirOutputFile(
            project = project,
            configurationName = configurationName,
            reportType = DependencyGuardReportType.TREE,
        )
    }

    val buildDirOutputFile: File by lazy {
        OutputFileUtils.buildDirOutputFile(
            project = project,
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
                ColorTerminal.ANSI_YELLOW, "Dependency Guard Tree baseline created for : for configuration classpath."
            )
            ColorTerminal.printlnColor(ColorTerminal.ANSI_YELLOW, "File: file://${projectDirOutputFile.canonicalPath}")
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
                    StringBuilder().apply {
                        appendLine(Messaging.dependencyChangeDetected)
                        appendLine("Dependency Tree comparison to baseline does not match.")
                        appendLine()
                        appendLine("If this is intentional, re-baseline using ./gradlew ${project.qualifiedBaselineTaskName()}")
                    }.toString()
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
