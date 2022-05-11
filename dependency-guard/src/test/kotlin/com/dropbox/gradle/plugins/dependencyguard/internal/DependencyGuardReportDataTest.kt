package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.models.ArtifactDependency
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

import java.io.File

internal class DependencyGuardReportDataTest {
    @Test
    fun `new library is added`() {
        TestDelegate(DependencyGuardReportType.ARTIFACT).apply {

            // Baseline Sample Report
            val report1 = SAMPLE_REPORT
            whenReportWritten(
                report = report1,
                shouldBaseline = true,
            )

            val report2 = report1.copy(
                dependencies = report1.dependencies.toMutableList().apply {
                    add(
                        ArtifactDependency(
                            group = "com.dropbox",
                            artifact = "focus",
                            version = "1.0.0",
                        )
                    )
                },
            )
            whenReportWritten(
                report = report2,
                shouldBaseline = false,
            )

            assertThat(report2.reportForConfig())
                .isNotEqualTo(report1.reportForConfig())
            assertThat(report2.reportForConfig().lines().size)
                .isEqualTo(report1.reportForConfig().lines().size + 1)
            assertThat(errorMessages.size)
                .isEqualTo(1)
            assertThat(errorMessages[0]).contains("+ com.dropbox:focus:1.0.0")
        }
    }

    @Test
    fun `version upgrade of existing library`() {
        TestDelegate(DependencyGuardReportType.ARTIFACT).apply {
            val report1 = SAMPLE_REPORT.copy(
                dependencies = listOf(
                    ArtifactDependency(
                        group = "com.dropbox",
                        artifact = "focus",
                        version = "1.0.0",
                    )
                )
            )
            // Baselines
            whenReportWritten(report = report1, shouldBaseline = true)
            val report2 = SAMPLE_REPORT.copy(
                dependencies = listOf(
                    ArtifactDependency(
                        group = "com.dropbox",
                        artifact = "focus",
                        version = "1.1.0",
                    )
                )
            )
            // Should cause error with the difference in versions
            whenReportWritten(report = report2, shouldBaseline = false)

            assertThat(errorMessages.size)
                .isEqualTo(1)
            assertThat(report2.reportForConfig())
                .isNotEqualTo(report1.reportForConfig())
            assertThat(report2.reportForConfig().lines().size)
                .isEqualTo(report1.reportForConfig().lines().size)
            assertThat(errorMessages[0])
                .contains("- com.dropbox:focus:1.0.0")
            assertThat(errorMessages[0])
                .contains("+ com.dropbox:focus:1.1.0")
        }
    }

    @Test
    fun `test baselineMap modify`() {
        val simpleReport = SAMPLE_REPORT.copy(
            dependencies = listOf(
                ArtifactDependency(
                    group = "group",
                    artifact = "artifact",
                    version = "1"
                )
            ),
            baselineMap = { "$it-extra" }
        )

        assertThat(simpleReport.reportForConfig())
            .contains("group:artifact:1-extra")
    }

    @Test
    fun `test baselineMap no-op`() {
        val simpleReport = SAMPLE_REPORT.copy(
            dependencies = listOf(
                ArtifactDependency(
                    group = "group",
                    artifact = "artifact",
                    version = "1"
                )
            )
        )
        assertThat(simpleReport.reportForConfig())
            .contains("group:artifact:1")
    }

    @Test
    fun `test baselineMap remove`() {
        val simpleReport = SAMPLE_REPORT.copy(
            dependencies = listOf(
                ArtifactDependency(
                    group = "group",
                    artifact = "artifact",
                    version = "1"
                )
            ),
            baselineMap = { null },
        )
        assertThat(simpleReport.reportForConfig())
            .isEmpty()
    }

    private class TestDelegate(val reportType: DependencyGuardReportType) {
        val errorMessages = mutableListOf<String>()
        val errorHandler: (String) -> Unit = { errorMessages.add(it) }
        private val buildDirOutputFile = File.createTempFile("buildDir", "")
        private val projectDirOutputFile = File.createTempFile("projectDir", "")
        private val reportWriter = DependencyGuardListReportWriter(
            artifacts = true,
            modules = true,
        )

        fun whenReportWritten(report: DependencyGuardReportData, shouldBaseline: Boolean) {
            reportWriter.writeReport(
                buildDirOutputFile = buildDirOutputFile,
                projectDirOutputFile = projectDirOutputFile,
                report = report,
                shouldBaseline = shouldBaseline,
                errorHandler = errorHandler,
            )
        }
    }

    companion object {
        const val PROJECT_PATH = "project-path"
        const val CONFIGURATION_NAME = "configuration-name"
        val SAMPLE_REPORT = DependencyGuardReportData(
            projectPath = PROJECT_PATH,
            configurationName = CONFIGURATION_NAME,
            allowRule = { true },
            baselineMap = { it },
            dependencies = listOf()
        )
    }
}
