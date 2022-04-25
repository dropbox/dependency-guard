package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.fixture.Builder
import com.dropbox.gradle.plugins.dependencyguard.fixture.SimpleProject
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class PluginTest {

  @Test fun `can generate baseline`(): Unit = SimpleProject().use { project ->
    val result = Builder.build(
      project = project,
      args = arrayOf(":lib:dependencyGuard")
    )

    assertThat(result.output).contains(
      "Dependency Guard Dependency baseline created for :lib for configuration compileClasspath."
    )

    val baseline = project.projectFile("lib/dependencies/compileClasspath.txt")
    assertThat(baseline.exists()).isTrue()

    val lines = baseline.readLines()
    assertThat(lines).hasSize(1)
    assertThat(lines.single()).isEqualTo("commons-io:commons-io:2.11.0")
  }

  private fun Path.exists() = Files.exists(this)
  private fun Path.readLines(): List<String> = Files.readAllLines(this).filter { it.isNotBlank() }
}