package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.utils.ColorTerminal.ANSI_GREEN
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.ColorTerminal.ANSI_RED
import com.dropbox.gradle.plugins.dependencyguard.internal.utils.ColorTerminal.colorify
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class RemovedAndAddedLinesTest {

    @Test
    fun `hasDifference returns false`() {
        val lines = RemovedAndAddedLines(
            addedLines = emptyList(),
            removedLines = emptyList(),
        )
        assertThat(lines.hasDifference).isFalse()
    }

    @Test
    fun `hasDifference returns true`() {
        val lines = RemovedAndAddedLines(
            addedLines = listOf("androidx.activity:activity:1.4.0"),
            removedLines = listOf("androidx.activity:activity:1.3.1"),
        )
        assertThat(lines.hasDifference).isTrue()
    }

    @Test
    fun `diffTextWithPlusAndMinus formatting`() {
        val lines = RemovedAndAddedLines(
            addedLines = listOf("androidx.activity:activity:1.4.0"),
            removedLines = listOf("androidx.activity:activity:1.3.1"),
        )
        assertThat(lines.diffTextWithPlusAndMinus)
            .isEqualTo(
                """
                - androidx.activity:activity:1.3.1
                + androidx.activity:activity:1.4.0
                
                """.trimIndent()
            )
    }

    @Test
    fun `diffTextWithPlusAndMinusWithColor formatting`() {
        val lines = RemovedAndAddedLines(
            addedLines = listOf("androidx.activity:activity:1.4.0"),
            removedLines = listOf("androidx.activity:activity:1.3.1"),
        )
        assertThat(lines.diffTextWithPlusAndMinusWithColor)
            .isEqualTo(
                """
                ${colorify(ANSI_RED, "- androidx.activity:activity:1.3.1")}
                ${colorify(ANSI_GREEN, "+ androidx.activity:activity:1.4.0")}
                
                """.trimIndent()
            )
    }

}
