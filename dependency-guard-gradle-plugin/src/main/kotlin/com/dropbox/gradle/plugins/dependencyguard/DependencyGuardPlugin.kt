package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators.requirePluginConfig
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyTreeDiffTaskNames
import com.dropbox.gradle.plugins.dependencyguard.internal.list.DependencyGuardListTask
import com.dropbox.gradle.plugins.dependencyguard.internal.tree.BuildEnvironmentDependencyTreeDiffTask
import com.dropbox.gradle.plugins.dependencyguard.internal.tree.DependencyTreeDiffTask
import com.dropbox.gradle.plugins.dependencyguard.internal.isRootProject
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin for watching dependency changes
 */
public class DependencyGuardPlugin : Plugin<Project> {

    internal companion object {
        internal const val DEPENDENCY_GUARD_TASK_GROUP = "Dependency Guard"

        internal const val DEPENDENCY_GUARD_TASK_NAME = "dependencyGuard"

        internal const val DEPENDENCY_GUARD_BASELINE_TASK_NAME = "dependencyGuardBaseline"
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create(
            DEPENDENCY_GUARD_TASK_NAME,
            DependencyGuardPluginExtension::class.java
        )

        registerTreeDiffTasks(target, extension)
        registerDependencyGuardBaselineTask(target, extension)
        registerDependencyGuardTask(target, extension)
    }

    private fun registerDependencyGuardBaselineTask(
        target: Project,
        extension: DependencyGuardPluginExtension
    ) {
        target.tasks.register(
            DEPENDENCY_GUARD_BASELINE_TASK_NAME,
            DependencyGuardListTask::class.java
        ) {
            val task = this
            task.setParams(
                extension = extension,
                shouldBaseline = true
            )
            extension.configurations.forEach {
                // Trigger the dependency tree diff task when treeReport is enabled
                if (it.tree) {
                    task.dependsOn(
                        DependencyTreeDiffTaskNames.createDependencyTreeBaselineTaskNameForConfiguration(
                            configurationName = it.configurationName
                        )
                    )
                }
            }
        }
    }

    private fun registerDependencyGuardTask(
        target: Project,
        extension: DependencyGuardPluginExtension
    ) {
        target.tasks.register(
            DEPENDENCY_GUARD_TASK_NAME,
            DependencyGuardListTask::class.java
        ) {
            val task = this
            requirePluginConfig(target, extension)
            task.setParams(
                extension = extension,
                shouldBaseline = false
            )

            extension.configurations.forEach {
                // Trigger the dependency tree diff task when treeReport is enabled
                if (it.tree) {
                    task.dependsOn(
                        DependencyTreeDiffTaskNames.createDependencyTreeTaskNameForConfiguration(
                            configurationName = it.configurationName
                        )
                    )
                }
            }
        }
    }

    private fun registerTreeDiffTasks(target: Project, extension: DependencyGuardPluginExtension) {
        // We must evaluate first so we have the extension data and all flavor configurations are recognized
        target.afterEvaluate {
            requirePluginConfig(target, extension)
            extension.configurations
                .filter { it.tree } // Only when treeReport is enabled
                .forEach { dependencyGuardConfiguration ->

                    // Register dependencyTreeDiff tasks for configuration
                    val configurationName = dependencyGuardConfiguration.configurationName
                    val taskClass = if (target.isRootProject()) {
                        BuildEnvironmentDependencyTreeDiffTask::class.java
                    } else {
                        DependencyTreeDiffTask::class.java
                    }
                    target.tasks.register(
                        DependencyTreeDiffTaskNames.createDependencyTreeTaskNameForConfiguration(
                            configurationName = configurationName
                        ),
                        taskClass
                    ) {
                        setParams(
                            configurationName = configurationName,
                            shouldBaseline = false
                        )
                    }
                    target.tasks.register(
                        DependencyTreeDiffTaskNames.createDependencyTreeBaselineTaskNameForConfiguration(
                            configurationName = configurationName
                        ),
                        taskClass
                    ) {
                        setParams(
                            configurationName = configurationName,
                            shouldBaseline = true
                        )
                    }
                }
        }
    }
}
