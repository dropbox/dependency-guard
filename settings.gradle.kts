include(":sample:app")
include(":sample:module1")
include(":sample:module2")

rootProject.name = "dependency-guard-root"

pluginManagement {
    includeBuild("dependency-guard")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
