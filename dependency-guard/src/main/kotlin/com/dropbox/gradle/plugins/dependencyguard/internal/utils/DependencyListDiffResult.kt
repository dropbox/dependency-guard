package com.dropbox.gradle.plugins.dependencyguard.internal.utils

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

            private val rebaselineMessage = Messaging.rebaselineMessage(projectPath)

            fun createDiffMessage(withColor: Boolean): String {
                return StringBuilder().apply {
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
            }
        }
    }
}
