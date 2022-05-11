package com.dropbox.gradle.plugins.dependencyguard.internal.utils

import org.gradle.util.GradleVersion

internal object GradleVersion {
    private val current = GradleVersion.current()
    val isAtLeast73 = current >= GradleVersion.version("7.3")
    val isAtLeast74 = current >= GradleVersion.version("7.4")
}