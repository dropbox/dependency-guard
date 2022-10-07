package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class DependencyListDiffTest {

    @Test
    fun performDiffTestRootProject() {

        val result: DependencyListDiffResult.DiffPerformed = DependencyListDiff.performDiff(
            projectPath = ":",
            configurationName = "classpath",
            expectedDependenciesFileContent = """
            :sample:module2
            androidx.activity:activity:1.3.1
            """.trimIndent(),
            actualDependenciesFileContent = """
            :sample:module2
            androidx.activity:activity:1.4.0
            """.trimIndent()
        )

        when (result) {
            is DependencyListDiffResult.DiffPerformed.HasDiff -> {
                val actual = result.createDiffMessage(false)

                val expected = StringBuilder().apply {
                    appendLine("""Dependencies Changed in : for configuration classpath""")
                    appendLine("- androidx.activity:activity:1.3.1")
                    appendLine("+ androidx.activity:activity:1.4.0")
                    appendLine()
                    appendLine("If this is intentional, re-baseline using ./gradlew :dependencyGuardBaseline")
                    appendLine("Or use ./gradlew dependencyGuardBaseline to re-baseline dependencies in entire project.")
                }.toString()

                assertThat(actual)
                    .isEqualTo(expected)
            }
            else -> fail("Invalid Result: $result")
        }
    }

    @Test
    fun performDiffTestModule() {

        val result: DependencyListDiffResult.DiffPerformed = DependencyListDiff.performDiff(
            projectPath = ":sample:app",
            configurationName = "classpath",
            expectedDependenciesFileContent = """
            :sample:module2
            androidx.activity:activity:1.3.1
            """.trimIndent(),
            actualDependenciesFileContent = """
            :sample:module2
            androidx.activity:activity:1.4.0
            """.trimIndent()
        )

        when (result) {
            is DependencyListDiffResult.DiffPerformed.HasDiff -> {
                val actual = result.createDiffMessage(false)
                val expected = StringBuilder().apply {
                    appendLine("""Dependencies Changed in :sample:app for configuration classpath""")
                    appendLine("- androidx.activity:activity:1.3.1")
                    appendLine("+ androidx.activity:activity:1.4.0")
                    appendLine()
                    appendLine("If this is intentional, re-baseline using ./gradlew :sample:app:dependencyGuardBaseline")
                    appendLine("Or use ./gradlew dependencyGuardBaseline to re-baseline dependencies in entire project.")
                }.toString()

                assertThat(actual)
                    .isEqualTo(expected)
            }
            else -> {
                fail("Wasn't expecting $result")
            }
        }
    }
}
