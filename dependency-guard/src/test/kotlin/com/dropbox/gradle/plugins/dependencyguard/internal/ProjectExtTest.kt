package com.dropbox.gradle.plugins.dependencyguard.internal

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ProjectExtTest {

    @Test
    fun `not root project qualified baseline task`() {
        assertThat(getQualifiedBaselineTaskForProjectPath(":sample:app"))
            .isEqualTo(":sample:app:dependencyGuardBaseline")
    }

    @Test
    fun `root project qualified baseline task`() {
        assertThat(getQualifiedBaselineTaskForProjectPath(":"))
            .isEqualTo(":dependencyGuardBaseline")
    }
}