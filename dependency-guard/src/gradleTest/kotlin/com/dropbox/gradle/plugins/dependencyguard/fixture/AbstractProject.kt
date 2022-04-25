package com.dropbox.gradle.plugins.dependencyguard.fixture

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

abstract class AbstractProject : AutoCloseable {

  val projectDir = Paths.get("build/gradleTest/${slug()}").also {
    Files.createDirectories(it)
  }
  private val buildDir = projectDir.resolve("build")

  fun buildFile(filename: String): Path {
    return buildDir.resolve(filename)
  }

  fun projectFile(filename: String): Path {
    return projectDir.resolve(filename)
  }

  private fun slug(): String {
    val worker = System.getProperty("org.gradle.test.worker")?.let { w ->
      "-$w"
    }.orEmpty()
    return "${javaClass.simpleName}-${UUID.randomUUID().toString().take(16)}$worker"
  }

  override fun close() {
    projectDir.toFile().deleteRecursively()
  }
}