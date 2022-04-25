package com.dropbox.gradle.plugins.dependencyguard.internal.tree

internal interface TreeDiffTask {
    fun setParams(configurationName: String, shouldBaseline: Boolean)
}
