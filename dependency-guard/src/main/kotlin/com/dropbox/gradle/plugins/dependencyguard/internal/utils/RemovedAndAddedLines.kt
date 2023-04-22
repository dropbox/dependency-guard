package com.dropbox.gradle.plugins.dependencyguard.internal.utils

internal data class RemovedAndAddedLines(
    val removedLines: List<String>,
    val addedLines: List<String>,
) {
    val hasDifference = removedLines.isNotEmpty() || addedLines.isNotEmpty()

    private val diffLines = mutableListOf<DiffLine>().apply {
        removedLines.forEach {
            add(DiffLine(added = false, str = it))
        }
        addedLines.forEach {
            add(DiffLine(added = true, str = it))
        }
    }.sortedBy { it.str }

    val diffTextWithPlusAndMinus: String = diffLines.fold(StringBuilder()) { builder, it ->
        builder.appendLine(
            if (it.added) {
                "+ ${it.str}"
            } else {
                "- ${it.str}"
            }
        )
    }.toString()

    val diffTextWithPlusAndMinusWithColor: String = diffLines.fold(StringBuilder()) { builder, it ->
        builder.appendLine(
            if (it.added) {
                ColorTerminal.colorify(ColorTerminal.ANSI_GREEN, "+ ${it.str}")
            } else {
                ColorTerminal.colorify(ColorTerminal.ANSI_RED, "- ${it.str}")
            }
        )
    }.toString()
}
