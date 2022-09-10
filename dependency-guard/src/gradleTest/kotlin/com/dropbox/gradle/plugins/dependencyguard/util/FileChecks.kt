package com.dropbox.gradle.plugins.dependencyguard.util

import com.dropbox.gradle.plugins.dependencyguard.fixture.AbstractProject
import com.google.common.truth.Truth.assertThat
import java.io.File

fun AbstractProject.assertFileExistsWithContentEqual(
    filename: String,
    contentFile: String,
) {
    assertFileContent(filename, contentFile) { actualContent, expectedContent ->
        assertThat(actualContent).isEqualTo(expectedContent)
    }
}

fun AbstractProject.assertFileExistsWithContentContains(
    filename: String,
    contentFile: String,
) {
    assertFileContent(filename, contentFile) { actualContent, expectedContent ->
        assertThat(actualContent).contains(expectedContent)
    }
}

private fun AbstractProject.assertFileContent(
    filename: String,
    contentFile: String,
    assertion: (actualContent: String, expectedContent: String) -> Unit,
) {
    val path = projectFile(filename)
    assertThat(path.exists()).isTrue()

    val actualContent = path.readText()
    val expectedContent: String = File("src/gradleTest/resources/$contentFile").readText()
    assertion(actualContent, expectedContent)
}

