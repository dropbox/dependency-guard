package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import java.io.File
import org.gradle.api.Project

internal object OutputFileUtils {
    fun buildDirOutputFile(
        project: Project,
        configurationName: String,
        reportType: DependencyGuardReportType,
    ): File {
        val configurationNameAndSuffix = "$configurationName${reportType.fileSuffix}"
        return project.layout
            .buildDirectory
            .get()
            .dir("tmp/dependency-guard")
            .file("$configurationNameAndSuffix.txt")
            .asFile
            .apply {
                parentFile.apply {
                    if (!exists()) {
                        // Create the "dependencies" directory if it does not exist
                        mkdirs()
                    }
                }
            }
    }

    fun projectDirOutputFile(
        project: Project,
        configurationName: String,
        reportType: DependencyGuardReportType
    ): File {
        val configurationNameAndSuffix = "${reportType.filePrefix}$configurationName${reportType.fileSuffix}"
        return project.layout
            .projectDirectory
            .dir("dependencies")
            .file("$configurationNameAndSuffix.txt")
            .asFile
            .apply {
                parentFile.apply {
                    if (!exists()) {
                        // Create the "dependencies" directory if it does not exist
                        mkdirs()
                    }
                }
            }
    }

    private const val DIRECTORY_NAME = "dependency-guard"
}
