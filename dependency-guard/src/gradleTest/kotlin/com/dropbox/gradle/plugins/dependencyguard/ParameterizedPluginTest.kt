package com.dropbox.gradle.plugins.dependencyguard

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER
import org.junit.jupiter.params.provider.ArgumentsSource

@ParameterizedTest(name = "$DISPLAY_NAME_PLACEHOLDER - {0}")
@ArgumentsSource(GradleVersionArgumentsProvider::class)
annotation class ParameterizedPluginTest