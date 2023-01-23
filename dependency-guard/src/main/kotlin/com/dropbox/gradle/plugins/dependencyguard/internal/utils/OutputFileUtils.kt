package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import com.dropbox.gradle.plugins.dependencyguard.internal.DependencyGuardReportType
import java.io.File
import org.gradle.api.file.Directory

internal object OutputFileUtils {

    fun buildDirDependencyGuardTmpDir(
        buildDirectory: Directory,
    ): Directory {
        val tempDir = buildDirectory
            .dir("tmp/dependency-guard")

        tempDir.asFile
            .apply {
                if (!exists()) {
                    // Create the "tmp/dependency-guard" directory if it does not exist
                    mkdirs()
                }
            }
        return tempDir
    }


    fun projectDirDependenciesDir(
        projectDirectory: Directory,
    ): Directory {
        val dependenciesDir = projectDirectory
            .dir("dependencies")
        dependenciesDir
            .asFile
            .apply {
                if (!exists()) {
                    // Create the "dependencies" directory if it does not exist
                    mkdirs()
                }
            }
        return dependenciesDir
    }

    fun buildDirOutputFile(
        buildDirectory: Directory,
        configurationName: String,
        reportType: DependencyGuardReportType,
    ): File {
        val configurationNameAndSuffix = "$configurationName${reportType.fileSuffix}"
        return buildDirectory
            .file("$configurationNameAndSuffix.txt")
            .asFile
    }

    fun projectDirOutputFile(
        projectDirectory: Directory,
        configurationName: String,
        reportType: DependencyGuardReportType
    ): File {
        val configurationNameAndSuffix = "${reportType.filePrefix}$configurationName${reportType.fileSuffix}"
        return projectDirectory
            .file("$configurationNameAndSuffix.txt")
            .asFile
            .apply {
                parentFile.apply {
                    if (!exists()) {
                        // Create the directory if it does not exist
                        mkdirs()
                    }
                }
            }
    }
}
