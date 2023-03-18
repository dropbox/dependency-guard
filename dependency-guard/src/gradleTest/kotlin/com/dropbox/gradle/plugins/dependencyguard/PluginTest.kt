package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.fixture.Builder.build
import com.dropbox.gradle.plugins.dependencyguard.fixture.Builder.buildAndFail
import com.dropbox.gradle.plugins.dependencyguard.fixture.ConfiguredProject
import com.dropbox.gradle.plugins.dependencyguard.fixture.EmptyProject
import com.dropbox.gradle.plugins.dependencyguard.fixture.FullProject
import com.dropbox.gradle.plugins.dependencyguard.fixture.SimpleProject
import com.dropbox.gradle.plugins.dependencyguard.util.assertFileExistsWithContentEqual
import com.dropbox.gradle.plugins.dependencyguard.util.replaceText
import com.google.common.truth.Truth.assertThat

class PluginTest {

    @ParameterizedPluginTest
    fun `can generate baseline`(args: ParameterizedPluginArgs): Unit = SimpleProject(tree = true).use { project ->
        val result = build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        // verify baseline
        assertThat(result.output)
            .contains("Dependency Guard baseline created for :lib for configuration compileClasspath.")
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "simple/list_before_update.txt",
        )
        // verify tree baseline
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "simple/tree_before_update.txt",
        )

        if (args.withConfigurationCache) {
            // verify configuration-cache related stuff
            assertThat(result.output)
                .contains("Calculating task graph as no configuration cache is available for tasks: :lib:dependencyGuard")
            assertThat(result.output)
                .contains("Configuration cache entry stored.")
        }
    }

    @ParameterizedPluginTest
    fun `guard with no dependencies changes`(
        args: ParameterizedPluginArgs,
    ): Unit = SimpleProject(tree = true).use { project ->
        // create baseline
        build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        // check with no dependencies changes
        val result = build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        assertThat(result.output)
            .contains("No Dependency Changes Found in :lib for configuration \"compileClasspath\"")

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "simple/list_before_update.txt",
        )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "simple/tree_before_update.txt",
        )
    }

    @ParameterizedPluginTest
    fun `guard after dependencies tree changes`(
        args: ParameterizedPluginArgs,
    ): Unit = SimpleProject(tree = true).use { project ->
        // create baseline
        build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        project.lib.resolve("build.gradle").replaceText(
            oldValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.4'",
            newValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.5'",
        )

        // check after dependencies changes
        val result = buildAndFail(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        assertThat(result.output).contains("Dependency Tree comparison to baseline does not match.")
        assertThat(result.output).contains(
            """
    [33m***** DEPENDENCY CHANGE DETECTED *****[0m
    [31m-\--- io.reactivex.rxjava3:rxjava:3.1.4[0m
    [31m-     \--- org.reactivestreams:reactive-streams:1.0.3[0m
    [32m+\--- io.reactivex.rxjava3:rxjava:3.1.5[0m
    [32m+     \--- org.reactivestreams:reactive-streams:1.0.4[0m
        """.trimIndent()
        )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "simple/list_before_update.txt",
        )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "simple/tree_before_update.txt",
        )
    }

    @ParameterizedPluginTest
    fun `guard after dependencies list changes`(
        args: ParameterizedPluginArgs,
    ): Unit = SimpleProject(tree = false).use { project ->
        // create baseline
        build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        project.lib.resolve("build.gradle").replaceText(
            oldValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.4'",
            newValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.5'",
        )

        // check after dependencies changes
        val result = buildAndFail(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        assertThat(result.output)
            .contains(
                """
                > Dependencies Changed in :lib for configuration compileClasspath
                  - io.reactivex.rxjava3:rxjava:3.1.4
                  + io.reactivex.rxjava3:rxjava:3.1.5
                  - org.reactivestreams:reactive-streams:1.0.3
                  + org.reactivestreams:reactive-streams:1.0.4
                  
                  If this is intentional, re-baseline using ./gradlew :lib:dependencyGuardBaseline
            """.trimIndent()
            )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "simple/list_before_update.txt",
        )
    }

    @ParameterizedPluginTest
    fun `baseline after dependencies changes`(
        args: ParameterizedPluginArgs,
    ): Unit = SimpleProject(tree = true).use { project ->
        // create baseline
        build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        project.lib.resolve("build.gradle").replaceText(
            oldValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.4'",
            newValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.5'",
        )

        // check after dependencies changes
        val result = build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuardBaseline")
        )

        assertThat(result.output)
            .contains("Dependency Guard baseline created for :lib for configuration compileClasspath")

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "simple/list_after_update.txt",
        )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "simple/tree_after_update.txt",
        )
    }

    @ParameterizedPluginTest
    fun `can generate full report baseline`(args: ParameterizedPluginArgs): Unit = FullProject().use { project ->
        val result = build(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf("dependencyGuard")
        )

        // verify baseline
        assertThat(result.output).contains("Dependency Guard baseline created for : for configuration classpath.")
        assertThat(result.output).contains("Dependency Guard baseline created for :lib for configuration compileClasspath.")
        assertThat(result.output).contains("Dependency Guard baseline created for :lib for configuration testCompileClasspath.")
        assertThat(result.output).contains("Dependency Guard Tree baseline created for : for configuration classpath.")
        assertThat(result.output).contains("Dependency Guard Tree baseline created for :lib for configuration compileClasspath.")
        assertThat(result.output).contains("Dependency Guard Tree baseline created for :lib for configuration testCompileClasspath.")

        project.assertFileExistsWithContentEqual(
            filename = "dependencies/classpath.txt",
            contentFile = "full/root_list.txt",
        )
        project.assertFileExistsWithContentEqual(
            filename = "dependencies/classpath.tree.txt",
            contentFile = "full/root_tree.txt",
        )
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "full/lib_compile_list.txt",
        )
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "full/lib_compile_tree.txt",
        )
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/testCompileClasspath.txt",
            contentFile = "full/lib_test_compile_list.txt",
        )
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/testCompileClasspath.tree.txt",
            contentFile = "full/lib_test_compile_tree.txt",
        )

        if (args.withConfigurationCache) {
            // verify configuration cache is supported
            assertThat(result.output).contains("Calculating task graph as no configuration cache is available for tasks: dependencyGuard")
            assertThat(result.output).contains("Configuration cache entry stored.")
        }
    }

    @ParameterizedPluginTest
    fun `prints error when no configuration found`(args: ParameterizedPluginArgs): Unit = ConfiguredProject(
        configurations = emptyList(),
    ).use { project ->
        val result = buildAndFail(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        assertThat(result.output).contains(
            """
            > Could not create task ':lib:dependencyGuard'.
               > Error: No configurations provided to Dependency Guard Plugin for project :lib
                 Here are some valid configurations you could use.
                 
                 dependencyGuard {
                   configuration("compileClasspath")
                   configuration("runtimeClasspath")
                   configuration("testCompileClasspath")
                   configuration("testRuntimeClasspath")
                 }
            """.trimIndent()
    }

    @ParameterizedPluginTest
    fun `prints error when wrong configuration found`(args: ParameterizedPluginArgs): Unit = ConfiguredProject(
        configurations = listOf(
            "compileClasspath",
            "releaseCompileClasspath",
        ),
    ).use { project ->
        val result = buildAndFail(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        assertThat(result.output).contains("Dependency Guard could not resolve the configurations named [releaseCompileClasspath] for :lib")
    }

    @ParameterizedPluginTest
    fun `prints error when missing configuration block`(args: ParameterizedPluginArgs): Unit = EmptyProject().use { project ->
        val result = buildAndFail(
            gradleVersion = args.gradleVersion,
            withConfigurationCache = args.withConfigurationCache,
            project = project,
            args = arrayOf("dependencyGuard")
        )

        assertThat(result.output)
            .contains("If you wish to use Dependency Guard on your root project, use the following config:")

        assertThat(result.output).contains(
            """
            A problem occurred configuring root project 'project-under-test'.
            > Could not create task ':dependencyGuard'.
               > dependencyGuard {
                   configuration("classpath")
                 }
            """.trimIndent()
        )
    }

}
