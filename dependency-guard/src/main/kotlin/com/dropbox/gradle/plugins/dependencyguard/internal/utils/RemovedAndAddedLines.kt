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

    val diffTextWithPlusAndMinus: String = diffLines.joinToString(separator = "") {
        val prefix = if (it.added) {
            "+ "
        } else {
            "- "
        }
        prefix + it.str + "\n"
    }

    val diffTextWithPlusAndMinusWithColor: String = diffTextWithPlusAndMinus.lines().joinToString(separator = "") {
        if (it.startsWith("-")) {
            ColorTerminal.colorify(ColorTerminal.ANSI_RED, it)
        } else if (it.startsWith("+")) {
            ColorTerminal.colorify(ColorTerminal.ANSI_GREEN, it)
        } else {
            it
        } + "\n"
    }
}