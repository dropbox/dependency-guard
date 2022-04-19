package com.dropbox.gradle.plugins.dependencyguard.internal

internal enum class DependencyGuardReportType(
    val reportTypeName: String,
    val fileSuffix: String
) {
    ALL("Dependency", ""),
    ARTIFACT("Artifact", ".artifacts"),
    MODULE("Module", ".modules"),
    TREE("Dependency Tree", ".tree");

    val filePrefix: String = ""

    override fun toString(): String {
        return reportTypeName
    }
}
