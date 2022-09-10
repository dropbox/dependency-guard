package com.dropbox.gradle.plugins.dependencyguard.internal.tree

import org.gradle.api.Project

internal interface TreeDiffTask {
    fun setParams(
        project: Project,
        configurationName: String,
        shouldBaseline: Boolean,
    )
}
