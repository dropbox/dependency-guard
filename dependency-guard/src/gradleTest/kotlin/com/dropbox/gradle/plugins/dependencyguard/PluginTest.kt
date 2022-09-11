package com.dropbox.gradle.plugins.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.fixture.Builder.build
import com.dropbox.gradle.plugins.dependencyguard.fixture.Builder.buildAndFail
import com.dropbox.gradle.plugins.dependencyguard.fixture.FullProject
import com.dropbox.gradle.plugins.dependencyguard.fixture.SimpleProject
import com.dropbox.gradle.plugins.dependencyguard.util.assertFileExistsWithContentEqual
import com.dropbox.gradle.plugins.dependencyguard.util.replaceText
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class PluginTest {

    @Test
    fun `can generate baseline`(): Unit = SimpleProject().use { project ->
        val result = build(
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        // verify baseline
        assertThat(result.output)
            .contains("Dependency Guard baseline created for :lib for configuration compileClasspath.")
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "list_before_update.txt",
        )
        // verify tree baseline
        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "tree_before_update.txt",
        )
        // verify configuration-cache related stuff
        assertThat(result.output)
            .contains("Calculating task graph as no configuration cache is available for tasks: :lib:dependencyGuard")
        assertThat(result.output)
            .contains("Configuration cache entry stored.")
    }

    @Test
    fun `guard with no dependencies changes`(): Unit = SimpleProject().use { project ->
        // create baseline
        build(
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        // check with no dependencies changes
        val result = build(
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        assertThat(result.output)
            .contains("No Dependency Changes Found in :lib for configuration \"compileClasspath\"")

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "list_before_update.txt",
        )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "tree_before_update.txt",
        )
    }

    @Test
    fun `guard after dependencies tree changes`(): Unit = SimpleProject().use { project ->
        // create baseline
        build(
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        project.lib.resolve("build.gradle").replaceText(
            oldValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.4'",
            newValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.5'",
        )

        // check after dependencies changes
        val result = buildAndFail(
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
            contentFile = "list_before_update.txt",
        )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "tree_before_update.txt",
        )
    }

    @Test
    fun `guard after dependencies list changes`(): Unit = SimpleProject(tree = false).use { project ->
        // create baseline
        build(
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        project.lib.resolve("build.gradle").replaceText(
            oldValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.4'",
            newValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.5'",
        )

        // check after dependencies changes
        val result = buildAndFail(
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
            contentFile = "list_before_update.txt",
        )
    }

    @Test
    fun `baseline after dependencies changes`(): Unit = SimpleProject().use { project ->
        // create baseline
        build(
            project = project,
            args = arrayOf(":lib:dependencyGuard")
        )

        project.lib.resolve("build.gradle").replaceText(
            oldValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.4'",
            newValue = "implementation 'io.reactivex.rxjava3:rxjava:3.1.5'",
        )

        // check after dependencies changes
        val result = build(
            project = project,
            args = arrayOf(":lib:dependencyGuardBaseline")
        )

        assertThat(result.output)
            .contains("Dependency Guard baseline created for :lib for configuration compileClasspath")

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.txt",
            contentFile = "list_after_update.txt",
        )

        project.assertFileExistsWithContentEqual(
            filename = "lib/dependencies/compileClasspath.tree.txt",
            contentFile = "tree_after_update.txt",
        )
    }

    @Test
    fun `can generate full report baseline`(): Unit = FullProject().use { project ->
        val result = build(
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

        // verify configuration cache is supported
        assertThat(result.output).contains("Calculating task graph as no configuration cache is available for tasks: dependencyGuard")
        assertThat(result.output).contains("Configuration cache entry stored.")
    }
}
