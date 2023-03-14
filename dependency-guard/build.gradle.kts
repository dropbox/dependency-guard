import com.vanniktech.maven.publish.SonatypeHost.S01
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.dokka)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.binaryCompatibilityValidator)
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

val VERSION_NAME: String by project
version = VERSION_NAME

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "1.8"
    // Because Gradle's Kotlin handling, this falls out of date quickly
    apiVersion = "1.4"
    languageVersion = "1.4"

    // We use class SAM conversions because lambdas compiled into invokedynamic are not
    // Serializable, which causes accidental headaches with Gradle configuration caching. It's
    // easier for us to just use the previous anonymous classes behavior
    @Suppress("SuspiciousCollectionReassignment")
    freeCompilerArgs += "-Xsam-conversion=class"
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(8)
}

kotlin {
  explicitApi()
}

gradlePlugin {
  plugins {
    plugins.create("dependency-guard") {
      id = "com.dropbox.dependency-guard"
      implementationClass = "com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin"
    }
  }
}

mavenPublish {
  sonatypeHost = S01
}

dependencies {
  compileOnly(gradleApi())
}

// Ensure build/gradleTest doesn't grow without bound when tests sometimes fail to clean up
// after themselves.
val deleteOldGradleTests = tasks.register<Delete>("deleteOldGradleTests") {
  delete(layout.buildDirectory.file("gradleTest"))
}

// https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html
@Suppress("UnstableApiUsage") // Test Suites is an @Incubating feature
testing {
  suites {
    // Configure the default test suite
    val test by getting(JvmTestSuite::class) {
      // This is the default
      useJUnitJupiter()
      dependencies {
        // Equivalent to `testImplementation ...` in the top-level dependencies block
        implementation(libs.truth)
      }
    }

    // Create a new test suite
    val gradleTest by registering(JvmTestSuite::class) {
      useJUnitJupiter()
      dependencies {
        // gradleTest test suite depends on the production code in tests
        implementation.invoke(project())
        implementation(libs.truth)
      }

      targets {
        configureEach {
          testTask.configure {
            shouldRunAfter(test)
            dependsOn(deleteOldGradleTests)

            systemProperty("pluginVersion", version)

            maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
            beforeTest(closureOf<TestDescriptor> {
              logger.lifecycle("Running test: $this")
            })
          }
        }
      }
    }
  }
}

gradlePlugin.testSourceSets(sourceSets.named("gradleTest").get())

@Suppress("UnstableApiUsage") // Test Suites is an @Incubating feature
tasks.named("check") {
  dependsOn(testing.suites.named("gradleTest"))
}

tasks.register("printVersionName") {
  doLast {
    println(VERSION_NAME)
  }
}