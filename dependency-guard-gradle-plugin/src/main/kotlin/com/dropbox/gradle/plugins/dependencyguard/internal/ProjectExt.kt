package com.dropbox.gradle.plugins.dependencyguard.internal

import org.gradle.api.Project

internal fun Project.isRootProject(): Boolean = (path == rootProject.path)