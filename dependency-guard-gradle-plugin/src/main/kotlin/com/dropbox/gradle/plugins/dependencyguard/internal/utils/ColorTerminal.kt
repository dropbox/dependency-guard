package com.dropbox.gradle.plugins.dependencyguard.internal.utils

// COLORED Terminal Output

internal object ColorTerminal {
    val ANSI_RESET = "\u001B[0m"
    val ANSI_BLACK = "\u001B[30m"
    val ANSI_RED = "\u001B[31m"
    val ANSI_GREEN = "\u001B[32m"
    val ANSI_YELLOW = "\u001B[33m"
    val ANSI_BLUE = "\u001B[34m"
    val ANSI_PURPLE = "\u001B[35m"
    val ANSI_CYAN = "\u001B[36m"
    val ANSI_WHITE = "\u001B[37m"

    /**
     * Prints in color
     *
     * @return normal, unformatted string
     */
    fun printlnColor(ansiColor: String?, str: String): String {
        println(colorify(ansiColor, str))
        return str
    }

    private fun colorify(ansiColor: String?, str: String): String = if (ansiColor != null) {
        ansiColor + str + ANSI_RESET
    } else {
        str
    }
}
