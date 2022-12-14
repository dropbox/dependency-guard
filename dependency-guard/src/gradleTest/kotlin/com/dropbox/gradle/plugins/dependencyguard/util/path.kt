package com.dropbox.gradle.plugins.dependencyguard.util

import java.nio.file.Files
import java.nio.file.Path

fun Path.exists(): Boolean = Files.exists(this)
fun Path.createDirectories(): Path = Files.createDirectories(this)
fun Path.readLines(): List<String> = Files.readAllLines(this).filter { it.isNotBlank() }
fun Path.readText(): String = Files.readAllBytes(this).decodeToString()
fun Path.writeText(content: String): Path = Files.write(this, content.toByteArray())
fun Path.replaceText(oldValue: String, newValue: String): Path = writeText(readText().replace(oldValue, newValue))
