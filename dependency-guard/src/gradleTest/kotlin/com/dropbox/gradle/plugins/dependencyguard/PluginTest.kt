package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.fixture.Builder.build
import com.dropbox.gradle.plugins.dependencyguard.fixture.SimpleProject
import com.dropbox.gradle.plugins.dependencyguard.util.exists
import com.dropbox.gradle.plugins.dependencyguard.util.readLines
import com.dropbox.gradle.plugins.dependencyguard.util.readText
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class PluginTest {

  @Test fun `can generate baseline`(): Unit = SimpleProject().use { project ->
    val result = build(
      project = project,
      args = arrayOf(":lib:dependencyGuard")
    )

    // verify baseline
    assertThat(result.output).contains(
      "Dependency Guard Dependency baseline created for :lib for configuration compileClasspath."
    )

    val baseline = project.projectFile("lib/dependencies/compileClasspath.txt")
    assertThat(baseline.exists()).isTrue()

    val lines = baseline.readLines()
    assertThat(lines).hasSize(1)
    assertThat(lines.single()).isEqualTo("commons-io:commons-io:2.11.0")

    // verify tree baseline
    val treeBaseline = project.projectFile("lib/dependencies/compileClasspath.tree.txt")
    assertThat(treeBaseline.exists()).isTrue()
    assertThat(treeBaseline.readText()).contains(
      """
        compileClasspath - Compile classpath for source set 'main'.
        \--- commons-io:commons-io:2.11.0
      """.trimIndent()
    )
  }
}