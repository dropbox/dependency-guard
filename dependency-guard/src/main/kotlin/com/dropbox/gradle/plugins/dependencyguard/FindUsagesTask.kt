package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyVisitor
import com.dropbox.gradle.plugins.dependencyguard.models.ModuleDependency
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public open class FindUsagesTask : DefaultTask() {

    /**
     * This would be an input, and not hard coded
     */
    private val projectPathToFindUsagesOf = ":sample:module2"

    /**
     * Only uses *CompileClasspath and *RuntimeClasspath
     */
    @TaskAction
    public fun execute() {
        val foundIn = mutableListOf<String>()
        // We want to go over all the projects
        project.rootProject.subprojects {
            val subproject = this
            val classpathConfigs =
                subproject.configurations.filter { it.name.endsWith("CompileClasspath") || it.name.endsWith("RuntimeClasspath") }

            classpathConfigs.forEach { config ->
                val found = DependencyVisitor.traverseDependenciesForConfiguration(config)
                val foundMatching = found.distinct().filter {
                    if (it is ModuleDependency) {
                        val matches = (it.path == projectPathToFindUsagesOf)
                        matches
                    } else {
                        false
                    }
                }
                if (foundMatching.isNotEmpty()) {
                    foundIn.add(subproject.path)
                }
            }
        }

        println("Found Usages of $projectPathToFindUsagesOf (including transitive) In:")
        foundIn.distinct().sortedBy { it }.forEach {
            println("* $it")
        }
    }
}