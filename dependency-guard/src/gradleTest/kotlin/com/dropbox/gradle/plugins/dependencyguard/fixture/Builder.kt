package com.dropbox.gradle.plugins.dependencyguard.fixture

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.nio.file.Path

object Builder {

    fun build(
        gradleVersion: GradleVersion,
        project: AbstractProject,
        withConfigurationCache: Boolean,
        vararg args: String
    ): BuildResult = runner(gradleVersion, project.projectDir, withConfigurationCache, *args).build()

    fun buildAndFail(
        gradleVersion: GradleVersion,
        project: AbstractProject,
        withConfigurationCache: Boolean,
        vararg args: String
    ): BuildResult = runner(gradleVersion, project.projectDir, withConfigurationCache, *args).buildAndFail()

    private fun runner(
        gradleVersion: GradleVersion,
        projectDir: Path,
        withConfigurationCache: Boolean,
        vararg args: String
    ): GradleRunner = GradleRunner.create().apply {
        forwardOutput()
        withPluginClasspath()
        withGradleVersion(gradleVersion.version)
        withProjectDir(projectDir.toFile())

        val arguments = listOfNotNull(
            *args,
            "-s",
            if (withConfigurationCache) "--configuration-cache" else null,
        )
        withArguments(arguments)
        // Ensure this value is true when `--debug-jvm` is passed to Gradle, and false otherwise
        withDebug(getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0)
    }
}
