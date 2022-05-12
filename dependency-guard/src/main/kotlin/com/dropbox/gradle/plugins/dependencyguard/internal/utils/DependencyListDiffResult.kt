package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.getQualifiedBaselineTaskForProjectPath

internal sealed class DependencyListDiffResult {

    internal object BaselineCreated : DependencyListDiffResult()

    internal sealed class DiffPerformed : DependencyListDiffResult() {

        internal object NoDiff : DiffPerformed()

        internal data class HasDiff(
            val projectPath: String,
            val configurationName: String,
            val removedAndAddedLines: RemovedAndAddedLines,
        ) : DiffPerformed() {

            val dependenciesChangedMessage =
                """Dependencies Changed in $projectPath for configuration $configurationName"""

            val rebaselineTaskName = getQualifiedBaselineTaskForProjectPath(projectPath)

            val errorMessage = StringBuilder().apply {

            }.toString()

            // TODO Move this somewhere else?
            fun printDiffInColor(): String {
                val outputMessage = StringBuilder()
                if (removedAndAddedLines.hasDifference) {
                    outputMessage.apply {
                        appendLine(
                            ColorTerminal.printlnColor(
                                ColorTerminal.ANSI_YELLOW,
                                dependenciesChangedMessage
                            )
                        )

                        removedAndAddedLines.diffTextWithPlusAndMinus.lines().forEach {
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
                                """If this is intentional, re-baseline using ./gradlew $rebaselineTaskName""".trimIndent()
                            )
                        )
                    }
                } else {
                    outputMessage.appendLine(
                        "No Dependency Changes Found in $projectPath for configuration \"$configurationName\""
                    )
                }
                return outputMessage.toString()
            }
        }
    }
}