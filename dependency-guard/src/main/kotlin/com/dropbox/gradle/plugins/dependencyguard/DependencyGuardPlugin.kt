package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyTreeDiffTaskNames
import com.dropbox.gradle.plugins.dependencyguard.internal.list.DependencyGuardListTask
import com.dropbox.gradle.plugins.dependencyguard.internal.tree.BuildEnvironmentDependencyTreeDiffTask
import com.dropbox.gradle.plugins.dependencyguard.internal.tree.DependencyTreeDiffTask
import com.dropbox.gradle.plugins.dependencyguard.internal.isRootProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

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
            DependencyGuardPluginExtension::class.java,
            target.objects
        )

        val baselineTask = registerDependencyGuardBaselineTask(target, extension)
        val guardTask = registerDependencyGuardTask(target, extension)
        registerTreeDiffTasks(
            target = target,
            extension = extension,
            baselineTask = baselineTask,
            guardTask = guardTask
        )
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
                val taskClass = if (target.isRootProject()) {
                    BuildEnvironmentDependencyTreeDiffTask::class.java
                } else {
                    DependencyTreeDiffTask::class.java
                }

                val treeGuardTask = target.tasks.register(
                    DependencyTreeDiffTaskNames.createDependencyTreeTaskNameForConfiguration(
                        configurationName = dependencyGuardConfiguration.configurationName
                    ),
                    taskClass
                ) {
                    setParams(
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
