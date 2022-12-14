package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import org.gradle.api.file.Directory
import java.io.File

internal object OutputFileUtils {
    fun buildDirOutputFile(
        buildDirectory: Directory,
        configurationName: String,
        reportType: DependencyGuardReportType,
    ): File {
        val configurationNameAndSuffix = "$configurationName${reportType.fileSuffix}"
        return buildDirectory
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
        projectDirectory: Directory,
        configurationName: String,
        reportType: DependencyGuardReportType
    ): File {
        val configurationNameAndSuffix = "${reportType.filePrefix}$configurationName${reportType.fileSuffix}"
        return projectDirectory
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
