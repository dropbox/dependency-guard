package com.dropbox.gradle.plugins.dependencyguard.util

import com.dropbox.gradle.plugins.dependencyguard.fixture.AbstractProject
import com.google.common.truth.Truth.assertThat
import java.io.File

fun AbstractProject.assertFileExistsWithContentEqual(
    filename: String,
    contentFile: String,
) {
    val path = projectFile(filename)
    assertThat(path.exists()).isTrue()

    val actualContent = path.readText()
    val expectedContent: String = File("src/gradleTest/resources/$contentFile").readText()
    assertThat(actualContent).isEqualTo(expectedContent)
}