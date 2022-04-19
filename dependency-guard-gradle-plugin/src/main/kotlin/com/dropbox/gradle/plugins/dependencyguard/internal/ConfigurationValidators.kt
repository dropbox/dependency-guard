package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler

internal object ConfigurationValidators {

    fun requirePluginConfig(project: Project, extension: DependencyGuardPluginExtension) {
        if (project.isRootProject()) {
            println("Configured for Root Project")
            if (extension.configurations.isNotEmpty() && extension.configurations.first().configurationName != ScriptHandler.CLASSPATH_CONFIGURATION) {
                println("If you wish to use dependency guard on your root project, use the following config:")
                val message = StringBuilder().apply {
                    appendLine("dependencyGuard {")
                    appendLine("""  configuration("${ScriptHandler.CLASSPATH_CONFIGURATION}")""")
                    appendLine("}")
                }.toString()
                throw GradleException(message)
            }
            return
        }
        val configurationNames = extension.configurations.map { it.configurationName }
        require(configurationNames.isNotEmpty() && configurationNames[0].isNotBlank()) {
            StringBuilder().apply {
                appendLine("Error: No configurations provided to Dependency Guard Plugin.")
                appendLine("Here are some valid configurations you could use.")
                appendLine("")
                val availableConfigNames = project.configurations
                    .filter {
                        isClasspathConfig(it.name)
                    }.map { it.name }

                appendLine("dependencyGuard {")
                availableConfigNames.forEach {
                    appendLine("""  configuration("$it")""")
                }
                appendLine("}")
            }.toString()
        }
    }

    fun isClasspathConfig(configName: String): Boolean {
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
                    println("Available Configurations:")
                    target.configurations
                        .filter {
                            isClasspathConfig(it.name)
                        }
                        .forEach {
                            println("* " + it.name)
                        }
                    throw GradleException(
                        "Configuration with name $configurationName was not found for ${target.name}"
                    )
                }
        }
    }
}
