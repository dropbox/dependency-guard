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
                val actual = result.printDiffInColor()
                val expected = """
            Dependencies Changed in ":" for configuration "classpath"
            - androidx.activity:activity:1.3.1
            + androidx.activity:activity:1.4.0
            
            
            If this is intentional, re-baseline using ./gradlew :dependencyGuardBaseline
            
            """.trimIndent()

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
                val actual = result.printDiffInColor()
                val expected = """
            Dependencies Changed in ":sample:app" for configuration "classpath"
            - androidx.activity:activity:1.3.1
            + androidx.activity:activity:1.4.0
            
            
            If this is intentional, re-baseline using ./gradlew :sample:app:dependencyGuardBaseline

            """.trimIndent()

                assertThat(actual)
                    .isEqualTo(expected)
            }
            else -> {
                fail("Wasn't expecting $result")
            }
        }
    }
}
