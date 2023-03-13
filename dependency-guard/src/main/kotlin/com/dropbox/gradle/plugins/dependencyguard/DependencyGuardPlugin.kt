package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.internal.ConfigurationValidators
import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyTreeDiffTaskNames
import com.dropbox.gradle.plugins.dependencyguard.internal.isRootProject
import com.dropbox.gradle.plugins.dependencyguard.internal.list.DependencyGuardListTask
import com.dropbox.gradle.plugins.dependencyguard.internal.projectConfigurations
import com.dropbox.gradle.plugins.dependencyguard.internal.tree.DependencyTreeDiffTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * A plugin for watching dependency changes
 */
public class DependencyGuardPlugin : Plugin<Project> {

    internal companion object {
        internal const val DEPENDENCY_GUARD_TASK_GROUP = "Dependency Guard"

        internal const val DEPENDENCY_GUARD_EXTENSION_NAME = "dependencyGuard"

        internal const val DEPENDENCY_GUARD_TASK_NAME = "dependencyGuard"

        internal const val DEPENDENCY_GUARD_BASELINE_TASK_NAME = "dependencyGuardBaseline"
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create(
            DEPENDENCY_GUARD_EXTENSION_NAME,
            DependencyGuardPluginExtension::class.java,
            target.objects
        )

        val dependencyGuardBaselineTask = registerDependencyGuardBaselineTask(target, extension)
        val dependencyGuardTask = registerDependencyGuardTask(target, extension)
        registerTreeDiffTasks(
            target = target,
            extension = extension,
            baselineTask = dependencyGuardBaselineTask,
            guardTask = dependencyGuardTask
        )

        attachToCheckTask(
            target = target,
            dependencyGuardTask = dependencyGuardTask
        )

        validateConfigurationAfterEvaluate(target, extension)
    }

    /**
     * It's recommended to avoid `afterEvaluate` but in this case we are doing a validation of inputs
     *
     * Without adding this, there were cases when the `target.configurations` were not all available yet.
     */
    private fun validateConfigurationAfterEvaluate(target: Project, extension: DependencyGuardPluginExtension) {
        target.afterEvaluate {
            val availableConfigurationNames = target.configurations.map { it.name }
            val monitoredConfigurationNames = extension.configurations.map { it.configurationName }
            ConfigurationValidators.requirePluginConfig(
                projectPath = target.path,
                isForRootProject = target.isRootProject(),
                availableConfigurations = availableConfigurationNames,
                monitoredConfigurations = monitoredConfigurationNames,
            )
            ConfigurationValidators.validateConfigurationsAreAvailable(
                projectPath = target.path,
                isForRootProject = target.isRootProject(),
                logger = target.logger,
                availableConfigurationNames = availableConfigurationNames,
                monitoredConfigurationNames = monitoredConfigurationNames
            )
        }
    }

    private fun attachToCheckTask(target: Project, dependencyGuardTask: TaskProvider<DependencyGuardListTask>) {
        // Only add to the "check" lifecycle task if the base plugin is applied
        target.pluginManager.withPlugin("base") {
            // Attach the "dependencyGuard" task to the "check" lifecycle task
            target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
                this.dependsOn(dependencyGuardTask)
            }
        }
    }

    private fun registerDependencyGuardBaselineTask(
        target: Project,
        extension: DependencyGuardPluginExtension
    ): TaskProvider<DependencyGuardListTask> {
        return target.tasks.register(
            DEPENDENCY_GUARD_BASELINE_TASK_NAME,
            DependencyGuardListTask::class.java
        ) {
            val task = this
            task.setParams(
                project = target,
                extension = extension,
                shouldBaseline = true
            )
        }
    }

    private fun registerDependencyGuardTask(
        target: Project,
        extension: DependencyGuardPluginExtension
    ): TaskProvider<DependencyGuardListTask> {
        return target.tasks.register(
            DEPENDENCY_GUARD_TASK_NAME,
            DependencyGuardListTask::class.java
        ) {
            val task = this
            task.setParams(
                project = target,
                extension = extension,
                shouldBaseline = false
            )
        }
    }

    private fun registerTreeDiffTasks(
        target: Project,
        extension: DependencyGuardPluginExtension,
        baselineTask: TaskProvider<DependencyGuardListTask>,
        guardTask: TaskProvider<DependencyGuardListTask>
    ) {
        extension.configurations.all {
            val dependencyGuardConfiguration = this
            if (dependencyGuardConfiguration.tree) {
                val taskClass = DependencyTreeDiffTask::class.java

                val treeGuardTask = target.tasks.register(
                    DependencyTreeDiffTaskNames.createDependencyTreeTaskNameForConfiguration(
                        configurationName = dependencyGuardConfiguration.configurationName
                    ),
                    taskClass
                ) {
                    setParams(
                        project = target,
                        configurationName = dependencyGuardConfiguration.configurationName,
                        shouldBaseline = false
                    )
                }
                guardTask.configure {
                    dependsOn(treeGuardTask)
                }

                val treeBaselineTask = target.tasks.register(
                    DependencyTreeDiffTaskNames.createDependencyTreeBaselineTaskNameForConfiguration(
                        configurationName = dependencyGuardConfiguration.configurationName
                    ),
                    taskClass
                ) {
                    setParams(
                        project = target,
                        configurationName = dependencyGuardConfiguration.configurationName,
                        shouldBaseline = true
                    )
                }
                baselineTask.configure {
                    dependsOn(treeBaselineTask)
                }
            }
        }
    }
}
