package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardConfiguration
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.logging.Logging

internal object ConfigurationValidators {

    private val logger = Logging.getLogger(DependencyGuardPlugin::class.java)

    fun requirePluginConfig(
        isForRootProject: Boolean,
        availableConfigurations: List<String>,
        monitoredConfigurations: List<DependencyGuardConfiguration>
    ) {
        if (isForRootProject) {
            logger.info("Configured for Root Project")
            if (monitoredConfigurations.isNotEmpty() && monitoredConfigurations.first().configurationName != ScriptHandler.CLASSPATH_CONFIGURATION) {
                logger.error("If you wish to use dependency guard on your root project, use the following config:")
                val message = StringBuilder().apply {
                    appendLine("dependencyGuard {")
                    appendLine("""  configuration("${ScriptHandler.CLASSPATH_CONFIGURATION}")""")
                    appendLine("}")
                }.toString()
                throw GradleException(message)
            }
        } else {
            val configurationNames = monitoredConfigurations.map { it.configurationName }
            require(configurationNames.isNotEmpty() && configurationNames[0].isNotBlank()) {
                StringBuilder().apply {
                    appendLine("Error: No configurations provided to Dependency Guard Plugin.")
                    appendLine("Here are some valid configurations you could use.")
                    appendLine("")
                    val availableConfigNames = availableConfigurations
                        .filter { isClasspathConfig(it) }

                    appendLine("dependencyGuard {")
                    availableConfigNames.forEach {
                        appendLine("""  configuration("$it")""")
                    }
                    appendLine("}")
                }.toString()
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
        target: Project,
        configurationNames: List<String>
    ) {
        val logger = target.logger

        if (target.isRootProject()) {
            if (configurationNames != listOf(ScriptHandler.CLASSPATH_CONFIGURATION)) {
                val message = StringBuilder().apply {
                    appendLine("For the root project, the only allowed configuration is ${ScriptHandler.CLASSPATH_CONFIGURATION}.")
                    appendLine("Use the following config:")
                    appendLine("""dependencyGuard {""")
                    appendLine("""  configuration("classpath")""")
                    appendLine("""}""")
                }.toString()
                throw GradleException(message)
            }
            return
        }

        configurationNames.forEach { configurationName ->
            target.configurations
                .firstOrNull { it.name == configurationName }
                ?: run {
                    val availableClasspathConfigs = target.configurations
                        .filter {
                            isClasspathConfig(it.name)
                        }
                    if (availableClasspathConfigs.isNotEmpty()) {
                        logger.quiet("Available Configurations for ${target.path}:")
                        availableClasspathConfigs.forEach {
                            logger.quiet("* " + it.name)
                        }
                    }
                    throw GradleException(
                        "Configuration with name $configurationName was not found for ${target.path}"
                    )
                }
        }
    }
}
