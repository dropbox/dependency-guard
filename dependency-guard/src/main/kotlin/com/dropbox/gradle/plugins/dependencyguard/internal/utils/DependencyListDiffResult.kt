package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.getQualifiedBaselineTaskForProjectPath
import java.io.File

internal sealed class DependencyListDiffResult {
    internal class BaselineCreated(
        projectPath: String,
        configurationName: String,
        baselineFile: File,
    ) : DependencyListDiffResult() {

        private val baselineMessage = StringBuilder()
            .apply {
                appendLine("Dependency Guard baseline created for $projectPath for configuration $configurationName.")
                appendLine("File: file://${baselineFile.canonicalPath}")
            }
            .toString()

        fun baselineCreatedMessage(withColor: Boolean): String = if (withColor) {
            ColorTerminal.colorify(ColorTerminal.ANSI_YELLOW, baselineMessage)
        } else {
            baselineMessage
        }
    }

    internal sealed class DiffPerformed : DependencyListDiffResult() {

        internal class NoDiff(
            projectPath: String,
            configurationName: String,
        ) : DiffPerformed() {
            val noDiffMessage: String =
                "No Dependency Changes Found in $projectPath for configuration \"$configurationName\""
        }

        internal data class HasDiff(
            val projectPath: String,
            val configurationName: String,
            val removedAndAddedLines: RemovedAndAddedLines,
        ) : DiffPerformed() {

            private val dependenciesChangedMessage =
                """Dependencies Changed in $projectPath for configuration $configurationName"""

            private val rebaselineTaskName = getQualifiedBaselineTaskForProjectPath(projectPath)

            private val rebaselineMessage =
                """If this is intentional, re-baseline using ./gradlew $rebaselineTaskName""".trimIndent()

            fun createDiffMessage(withColor: Boolean): String {
                StringBuilder().apply {
                    apply {
                        appendLine(
                            if (withColor) {
                                ColorTerminal.colorify(ColorTerminal.ANSI_YELLOW, dependenciesChangedMessage)
                            } else {
                                dependenciesChangedMessage
                            }
                        )
                        appendLine(
                            if (withColor) {
                                removedAndAddedLines.diffTextWithPlusAndMinusWithColor
                            } else {
                                removedAndAddedLines.diffTextWithPlusAndMinus
                            }
                        )
                        appendLine(
                            if (withColor) {
                                ColorTerminal.colorify(ColorTerminal.ANSI_RED, rebaselineMessage)
                            } else {
                                rebaselineMessage
                            }
                        )
                    }
                }.toString()

                val outputMessage = StringBuilder()
                if (removedAndAddedLines.hasDifference) {
                    outputMessage.apply {
                        appendLine(

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
