rootProject.name = "dependency-guard-gradle-plugin"

// Because this is pulled in via "includeBuild" from the root project,
// we need to point to the version catalog.  This same method is used
// in Focus: https://github.com/dropbox/focus/blob/main/focus-gradle-plugin/settings.gradle.kts
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
