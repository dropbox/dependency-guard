package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.getQualifiedBaselineTaskForProjectPath

internal object DependencyListDiff {

    /**
     * @return Whether a diff was detected
     */
    @Suppress("LongParameterList")
    fun performDiff(
        projectPath: String,
        configurationName: String,
        expectedDependenciesFileContent:String,
        actualDependenciesFileContent: String,
        errorHandler: (String) -> Unit,
    ): Boolean {
        // Compare
        val expectedLines = expectedDependenciesFileContent.lines()
        val difference = compare(expectedLines, actualDependenciesFileContent.lines())
        if (difference.isNotBlank()) {
            val dependenciesChangedMessage = """Dependencies Changed in "$projectPath" for configuration "$configurationName""""
            val errorMessage = StringBuilder().apply {
                appendLine(
                    ColorTerminal.printlnColor(
                        ColorTerminal.ANSI_YELLOW,
                        dependenciesChangedMessage
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
                        $dependenciesChangedMessage
                        If this is intentional, re-baseline using ./gradlew ${getQualifiedBaselineTaskForProjectPath(projectPath)}
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
