package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardConfiguration
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider

internal object ConfigurationValidators {

    private val logger = Logging.getLogger(DependencyGuardPlugin::class.java)

    fun validatePluginConfiguration(
        project: Project,
        extension: DependencyGuardPluginExtension,
        resolvedConfigurationsMap: Map<DependencyGuardConfiguration, Provider<ResolvedComponentResult>>
    ) {
        val requestedConfigurations = extension.configurations.map { it.configurationName }
        val validRequestedConfigurations = resolvedConfigurationsMap.keys.map { it.configurationName }
        val allAvailableConfigurations = project.projectConfigurations.map { it.name }

        validatePluginConfiguration(
            projectPath = project.path,
            isForRootProject = project.isRootProject(),
            requestedConfigurations = requestedConfigurations,
            validRequestedConfigurations = validRequestedConfigurations,
            allAvailableConfigurations = allAvailableConfigurations,
        )
    }

    internal fun validatePluginConfiguration(
        projectPath: String,
        isForRootProject: Boolean,
        requestedConfigurations: List<String>,
        validRequestedConfigurations: List<String>,
        allAvailableConfigurations: List<String>,
    ) {
        if (isForRootProject) {
            if (requestedConfigurations != listOf(ScriptHandler.CLASSPATH_CONFIGURATION)) {
                logger.error("If you wish to use Dependency Guard on your root project, use the following config:")
                throw GradleException(
                    """
                    dependencyGuard {
                      configuration("${ScriptHandler.CLASSPATH_CONFIGURATION}")
                    }
                """.trimIndent()
                )
            }
        } else {
            if (requestedConfigurations.isEmpty()) {
                throw GradleException(buildString {
                    appendLine("Error: No configurations provided to Dependency Guard Plugin for project $projectPath")
                    appendLine(configurationsYouCouldUseMessage(allAvailableConfigurations))
                })
            }

            val unresolvedConfigurations = requestedConfigurations.minus(validRequestedConfigurations.toSet())
            if (unresolvedConfigurations.isNotEmpty()) {
                throw GradleException(
                    buildString {
                        appendLine("Dependency Guard could not resolve the configurations named $unresolvedConfigurations for $projectPath")
                        appendLine(configurationsYouCouldUseMessage(allAvailableConfigurations))
                    }
                )
            }
        }
    }

    private fun configurationsYouCouldUseMessage(availableConfigurations: List<String>): String = buildString {
        val availableConfigNames = availableConfigurations.filter { isClasspathConfig(it) }
        if (availableConfigNames.isNotEmpty()) {
            appendLine("Here are some valid configurations you could use.")
            appendLine("")

            appendLine("dependencyGuard {")
            availableConfigNames.forEach {
                appendLine("""  configuration("$it")""")
            }
            appendLine("}")
        }
    }

    private fun isClasspathConfig(configName: String): Boolean {
        return configName.endsWith(
            suffix = "CompileClasspath",
            ignoreCase = true
        ) || configName.endsWith(
            suffix = "RuntimeClasspath",
            ignoreCase = true
        )
    }
}
