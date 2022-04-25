plugins {
    id("com.android.application")
    id("com.dropbox.dependency-guard")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 23
        targetSdk = 31
    }
}

dependencies {
    implementation(project(":sample:module1"))
    implementation(libs.androidx.activity)
}

dependencyGuard {
    // All dependencies included in Production Release APK
    configuration("releaseRuntimeClasspath") {
        allowRule = {
            // Disallow dependencies with a name containing "test"
            !it.contains("test")
        }
    }
}