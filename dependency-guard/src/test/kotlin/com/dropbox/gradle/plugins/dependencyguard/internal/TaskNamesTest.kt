package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyTreeDiffTaskNames.capitalized
import org.gradle.configurationcache.extensions.capitalized
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TaskNamesTest {
    @Test
    fun `new library is added`() {
        listOf(
            "" to "",
            "a" to "A",
            "A" to "A",
            "aa" to "Aa",
            "aaa" to "Aaa",
            "aA" to "AA",
            "Aa" to "Aa",
        ).forEach {
            assertEquals(it.first.capitalized(), it.second)
        }
    }
}
