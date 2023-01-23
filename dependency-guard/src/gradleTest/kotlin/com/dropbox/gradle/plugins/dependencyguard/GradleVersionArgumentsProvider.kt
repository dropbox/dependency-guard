package com.dropbox.gradle.plugins.dependencyguard

import org.gradle.util.GradleVersion
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class GradleVersionArgumentsProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> = Stream
        .of(
            ParameterizedPluginArgs(gradleVersion = GradleVersion.version("7.4.2"), withConfigurationCache = false),
            ParameterizedPluginArgs(gradleVersion = GradleVersion.version("7.4.2"), withConfigurationCache = true),
            ParameterizedPluginArgs(gradleVersion = GradleVersion.version("7.5.1"), withConfigurationCache = false),
            ParameterizedPluginArgs(gradleVersion = GradleVersion.version("7.5.1"), withConfigurationCache = true),
            ParameterizedPluginArgs(gradleVersion = GradleVersion.version("7.6"), withConfigurationCache = false),
            ParameterizedPluginArgs(gradleVersion = GradleVersion.version("7.6"), withConfigurationCache = true),
        )
        .map(Arguments::of)
}