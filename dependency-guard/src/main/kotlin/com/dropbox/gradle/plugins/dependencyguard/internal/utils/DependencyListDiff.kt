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
    ): DependencyListDiffResult.DiffPerformed {
        // Compare Expected vs Actual
        val removedAndAddedLines: RemovedAndAddedLines = compareAndAddPlusMinusPrefixes(
            expected = expectedDependenciesFileContent.lines(),
            actual = actualDependenciesFileContent.lines()
        )

        return if (removedAndAddedLines.hasDifference) {
            DependencyListDiffResult.DiffPerformed.HasDiff(
                projectPath = projectPath,
                configurationName = configurationName,
                removedAndAddedLines = removedAndAddedLines,
            )
        } else {
            DependencyListDiffResult.DiffPerformed.NoDiff
        }
    }

    /**
     * Return the difference String, or null if there was no difference
     */
    private fun compareAndAddPlusMinusPrefixes(
        expected: List<String>,
        actual: List<String>
    ): RemovedAndAddedLines {
        val removedLines =
            expected.filter { !actual.contains(it) }
        val addedLines =
            actual.filter { !expected.contains(it) }
        return RemovedAndAddedLines(
            removedLines = removedLines,
            addedLines = addedLines,
        )
    }
}
