// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    jacoco
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin) apply false
    id("com.dropbox.dependency-guard")
}

dependencyGuard {
    configuration("classpath")
}

dependencies {
    api(platform(libs.kotlin.bom))
}
