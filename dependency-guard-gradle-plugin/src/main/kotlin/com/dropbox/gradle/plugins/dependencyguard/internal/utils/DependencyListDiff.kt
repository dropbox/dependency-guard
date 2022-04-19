package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import java.io.File

internal object DependencyListDiff {

    /**
     * @return Whether a diff was detected
     */
    @Suppress("LongParameterList")
    fun performDiff(
        type: DependencyGuardReportType,
        projectPath: String,
        configurationName: String,
        projectDirOutputFile: File,
        depsReportString: String,
        errorHandler: (String) -> Unit,
    ): Boolean {
        // Compare
        val expectedFileContent = projectDirOutputFile.readText()
        val expectedLines = expectedFileContent.lines()
        val difference = compare(expectedLines, depsReportString.lines())
        if (difference.isNotBlank()) {
            val errorMessage = StringBuilder().apply {
                appendLine(
                    ColorTerminal.printlnColor(
                        ColorTerminal.ANSI_YELLOW,
                        "$type Dependencies Changed $projectPath for configuration \"$configurationName\":"
                    )
                )
                difference.lines().forEach {
                    appendLine(
                        if (it.startsWith("-")) {
                            ColorTerminal.printlnColor(ColorTerminal.ANSI_RED, it)
                        } else if (it.startsWith("+")) {
                            ColorTerminal.printlnColor(ColorTerminal.ANSI_GREEN, it)
                        } else {
                            it
                        }
                    )
                }
                appendLine(
                    ColorTerminal.printlnColor(
                        ColorTerminal.ANSI_RED,
                        """
                        $type Dependencies Changed in $projectPath for configuration "$configurationName"
                        If this is intentional, re-baseline using $projectPath:${DependencyGuardPlugin.DEPENDENCY_GUARD_BASELINE_TASK_NAME}
                        """.trimIndent()
                    )
                )
            }.toString()
            errorHandler(errorMessage)
            return true
        } else {
            println(
                "No Dependency Changes Found in $projectPath for configuration \"$configurationName\""
            )
        }
        return false
    }

    private fun compare(expected: List<String>, actual: List<String>): String {
        val removedLines =
            expected.filter { !actual.contains(it) }
        val addedLines =
            actual.filter { !expected.contains(it) }

        data class Line(val added: Boolean, val str: String)

        val lines = mutableListOf<Line>()
        removedLines.forEach {
            lines.add(Line(added = false, str = it))
        }
        addedLines.forEach {
            lines.add(Line(added = true, str = it))
        }

        return StringBuilder().apply {
            lines.sortedBy { it.str }.forEach {
                val prefix = if (it.added) {
                    "+ "
                } else {
                    "- "
                }
                appendLine(prefix + it.str)
            }
        }.toString()
    }
}
