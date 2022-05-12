package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.getQualifiedBaselineTaskForProjectPath

internal object DependencyListDiff {

    /**
     * @return The detected difference, or null if there was none.
     */
    @Suppress("LongParameterList")
    fun performDiff(
        projectPath: String,
        configurationName: String,
        expectedDependenciesFileContent: String,
        actualDependenciesFileContent: String,
        errorHandler: (String) -> Unit,
    ): DifferenceResult.DiffPerformed {
        // Compare
        val expectedLines = expectedDependenciesFileContent.lines()
        val difference: DifferenceResult.DiffPerformed =
            compareAndAddPlusMinusPrefixes(expectedLines, actualDependenciesFileContent.lines())

        if (difference is DifferenceResult.DiffPerformed.HasDifference) {
            val dependenciesChangedMessage =
                """Dependencies Changed in "$projectPath" for configuration "$configurationName""""
            val errorMessage = StringBuilder().apply {
                appendLine(
                    ColorTerminal.printlnColor(
                        ColorTerminal.ANSI_YELLOW,
                        dependenciesChangedMessage
                    )
                )

                difference.differenceLinesText.lines().forEach {
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
                        
                        If this is intentional, re-baseline using ./gradlew ${
                            getQualifiedBaselineTaskForProjectPath(
                                projectPath
                            )
                        }
                        """.trimIndent()
                    )
                )
            }.toString()
            errorHandler(errorMessage)
        } else {
            println(
                "No Dependency Changes Found in $projectPath for configuration \"$configurationName\""
            )
        }
        return difference
    }

    /**
     * Return the difference String, or null if there was no difference
     */
    private fun compareAndAddPlusMinusPrefixes(
        expected: List<String>,
        actual: List<String>
    ): DifferenceResult.DiffPerformed {
        val removedLines =
            expected.filter { !actual.contains(it) }
        val addedLines =
            actual.filter { !expected.contains(it) }

        return if (removedLines.isEmpty() && addedLines.isEmpty()) {
            DifferenceResult.DiffPerformed.NoDifference
        } else {
            DifferenceResult.DiffPerformed.HasDifference(
                removedLines = removedLines,
                addedLines = addedLines,
            )
        }
    }
}
