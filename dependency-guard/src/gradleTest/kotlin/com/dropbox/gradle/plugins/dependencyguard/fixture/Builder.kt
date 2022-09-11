package com.dropbox.gradle.plugins.dependencyguard.fixture

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.nio.file.Path

object Builder {

  fun build(
    gradleVersion: GradleVersion = GradleVersion.current(),
    project: AbstractProject,
    vararg args: String
  ): BuildResult = runner(gradleVersion, project.projectDir, *args).build()

  fun build(
    gradleVersion: GradleVersion = GradleVersion.current(),
    projectDir: Path,
    withPluginClasspath: Boolean = true,
    vararg args: String
  ): BuildResult = runner(gradleVersion, projectDir, *args).build()

  fun buildAndFail(
    gradleVersion: GradleVersion = GradleVersion.current(),
    project: AbstractProject,
    vararg args: String
  ): BuildResult = runner(
    gradleVersion,
    project.projectDir,
    *args
  ).buildAndFail()

  fun buildAndFail(
    gradleVersion: GradleVersion = GradleVersion.current(),
    projectDir: Path,
    vararg args: String
  ): BuildResult = runner(gradleVersion, projectDir, *args).buildAndFail()

  private fun runner(
    gradleVersion: GradleVersion,
    projectDir: Path,
    vararg args: String
  ): GradleRunner = GradleRunner.create().apply {
    forwardOutput()
    withPluginClasspath()
    withGradleVersion(gradleVersion.version)
    withProjectDir(projectDir.toFile())
    withArguments(args.toList() + "-s" + "--configuration-cache")
    // Ensure this value is true when `--debug-jvm` is passed to Gradle, and false otherwise
    withDebug(getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0)
  }
}
