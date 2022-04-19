import com.vanniktech.maven.publish.SonatypeHost.S01
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.dokka)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.binaryCompatibilityValidator)
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

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
  implementation(platform(libs.kotlin.bom))
  implementation(libs.kotlin.plugin)

  testImplementation(gradleTestKit())
  testImplementation(libs.junit)
  testImplementation(libs.truth)
}

tasks.register("printVersionName") {
  doLast {
    val VERSION_NAME: String by project
    println(VERSION_NAME)
  }
}