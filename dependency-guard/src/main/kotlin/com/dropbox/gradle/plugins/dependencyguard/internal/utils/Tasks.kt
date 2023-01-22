package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import org.gradle.api.Task

@Suppress("UnstableApiUsage")
internal object Tasks {
    fun Task.declareCompatibilities() {
        doNotTrackState("This task only outputs to console")
    }
}
