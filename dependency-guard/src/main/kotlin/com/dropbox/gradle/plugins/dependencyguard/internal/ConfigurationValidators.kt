package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import org.gradle.api.GradleException
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

internal object ConfigurationValidators {

    private val logger = Logging.getLogger(DependencyGuardPlugin::class.java)

    fun requirePluginConfig(
        projectPath: String,
        isForRootProject: Boolean,
        availableConfigurations: List<String>,
        monitoredConfigurations: List<String>
    ) {
        if (isForRootProject) {
            logger.info("Configured for Root Project")
            if (monitoredConfigurations.isNotEmpty() && monitoredConfigurations.first() != ScriptHandler.CLASSPATH_CONFIGURATION) {
                logger.error("If you wish to use dependency guard on your root project, use the following config:")
                throw GradleException(
                    """
                    dependencyGuard {
                      configuration("${ScriptHandler.CLASSPATH_CONFIGURATION}")
                    }
                """.trimIndent()
                )
            }
        } else {
            val availableConfigNames = availableConfigurations
                .filter { isClasspathConfig(it) }

            require(availableConfigNames.isNotEmpty()) {
                buildString {
                    appendLine("Error: The Dependency Guard Plugin can not find any configurations ending in 'RuntimeClasspath' or 'CompileClasspath'.")
                    appendLine("Suggestion: Move your usage of Dependency Guard as access to these Classpath configurations are required to configure Dependency Guard correctly.")
                    appendLine("Why: Either you have applied the plugin too early or your project just doesn't have any matching Classpath configurations.")
                    appendLine("All available Gradle configurations for project $projectPath at this point of time:")
                    availableConfigurations.forEach {
                        appendLine("* $it")
                    }
                }
            }

            val configurationNames = monitoredConfigurations.map { it }
            require(configurationNames.isNotEmpty() && configurationNames[0].isNotBlank()) {
                buildString {
                    appendLine("Error: No configurations provided to Dependency Guard Plugin.")
                    appendLine("Here are some valid configurations you could use.")
                    appendLine("")

                    appendLine("dependencyGuard {")
                    availableConfigNames.forEach {
                        appendLine("""  configuration("$it")""")
                    }
                    appendLine("}")
                }
            }
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


    fun validateConfigurationsAreAvailable(
        isForRootProject: Boolean,
        projectPath: String,
        logger: Logger,
        availableConfigurationNames: Collection<String>,
        monitoredConfigurationNames: List<String>
    ) {

        if (isForRootProject) {
            if (monitoredConfigurationNames != listOf(ScriptHandler.CLASSPATH_CONFIGURATION)) {
                throw GradleException(
                    """
                        For the root project, the only allowed configuration is ${ScriptHandler.CLASSPATH_CONFIGURATION}.
                        Use the following config:
                        dependencyGuard {
                          configuration("classpath")
                        }
                    """.trimIndent()
                )
            }
            return
        }

        monitoredConfigurationNames.forEach { monitoredConfigurationName ->
            availableConfigurationNames
                .firstOrNull { it == monitoredConfigurationName }
                ?: run {
                    val availableClasspathConfigs = availableConfigurationNames
                        .filter {
                            isClasspathConfig(it)
                        }
                    if (availableClasspathConfigs.isNotEmpty()) {
                        logger.quiet("Configuration with name $monitoredConfigurationName was not found. Available Configurations for $projectPath are:")
                        availableClasspathConfigs.forEach {
                            logger.quiet("* $it")
                        }
                    }
                    throw GradleException(
                        "Configuration with name $monitoredConfigurationName was not found for $projectPath"
                    )
                }
        }
    }
}
