# 🛡️ Dependency Guard

A Gradle plugin that helps you **guard against unintentional dependency changes**.

## Real World Use Cases & How Dependency Guard Can Help
* Accidentally shipping a testing dependency to production because `implementation` was used instead of `testImplementation` - [@handstandsam](https://twitter.com/joreilly)
  * Dependency Guard has List and Tree baseline formats that can compared against to identify changes. If changes occur, and they are expected, you can re-baseline.
* Dependency versions were transitively upgraded which worked fine typically, but caused a runtime crash later that was hard to figure out why it happened.
  * You would see the diff off dependencies in Git since the last working version and be able to have quickly track down the issue.
* After adding `Ktor 2.0.0`, it transitively upgraded to `Kotlin 1.6.20` which caused incompatibilities with `Jetpack Compose 1.1.1` which requires `Kotlin 1.6.10` - [@joreilly](https://twitter.com/joreilly)
  * During large upgrades it is important to see how a single version bump will impact the rest of your application.
* Upgrading to the Android Gradle Plugin transitively downgraded protobuf plugin which caused issues - [@AutonomousApps](https://twitter.com/AutonomousApps)

## Why Was Dependency Guard Built?
As platform engineers, we do a lot of library upgrades, and needed insight into how dependencies were changing over time.  Any small change can have a large impact. On large teams, it's not possible to track all dependency changes in a practical way, so we needed tooling to help.  This is a tool that surfaces these changes to any engineer making dependency changes, and allows them to re-baseline if it's intentional.  This provides us with historical reference on when dependencies (including transitive) are changed for a given configuration.

### Which Configurations Should be Guarded?
* Production App Configurations (what you ship)
* Production Build Configurations (what you use to build your app)

Changes to either one of these can change your resulting application.

### Tree Format
Dependency Guard started with just finding a way to easily create leverage `dependency-tree-diff` of a specific configuration of the `:dependencies` task from Gradle.  

The [`dependency-tree-diff` library](https://github.com/JakeWharton/dependency-tree-diff) in extremely helpful tool, but is impractical for many projects in Continuous Integration since it has a lot of custom setup to enable it. ([related discussion](https://github.com/JakeWharton/dependency-tree-diff/discussions/8)).  `dependency-tree-diff`'s diffing logic is actually used by Dependency Guard to highlight changes in trees.

The tree format proved to be very helpful, but as we looked at it from a daily usage standpoint, we found that the tree format created more noise than signal.  It's super helpful to use for development, but we wouldn't recommend storing the tree format in Git, especially in large projects as it gets noisy, and it becomes ignored.  In brainstorming with [@joshfein](https://twitter.com/joshfein), it seemed like a simple orded, de-duplicated list of dependencies were better for storing in CI.

`./gradlew :sample:app:dependencyGuard` generates `sample/app/dependencies/releaseRuntimeClasspath.tree.txt` 
```

------------------------------------------------------------
Project ':sample:app'
------------------------------------------------------------

releaseRuntimeClasspath - Resolved configuration for runtime for variant: release
\--- project :sample:module1
     +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10
     |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.6.10
     |    |    +--- org.jetbrains:annotations:13.0
     |    |    \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.6.10
     |    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.10
     |         \--- org.jetbrains.kotlin:kotlin-stdlib:1.6.10 (*)
     \--- project :sample:module2
          +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10 (*)
          \--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2
               \--- org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.2
                    +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.30 -> 1.6.10 (*)
                    \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.5.30 -> 1.6.10

(*) - dependencies omitted (listed previously)

A web-based, searchable dependency report is available by adding the --scan option.

```

If you want to be able to use the tree format for development and debugging, you can leave it enabled, but add it to your `.gitignore` file so that it doesn't get checked into Git.
```
//.gitignore
dependencies/*.tree.txt
```

### List Format
The list format is generated at `dependencies.${configurationName}.txt`.  By default it contains both a list of modules and 3rd party artifacts.  You can customize this configuration if you choose with the following configuration:


sample/app/dependency-guard/runtimeClasspath.txt
```
:sample:module1
:sample:module2
org.jetbrains.kotlin:kotlin-stdlib-common:1.6.10
org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.10
org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10
org.jetbrains.kotlin:kotlin-stdlib:1.6.10
org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.2
org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2
org.jetbrains:annotations:13.0
```


### Filtering for Allowed Dependencies




The tree diffs are super helpful to debug with and can be checked into Git, but on larger projects it gets VERY noisy and the list format works must better.  If you still want to enable it, you can add the tree format to your `.gitignore` so you can still use it, but not have it checked into Git.

  
## How to Use Dependency Guard


to be able t For a given module and configuration, a baseline will be saved at `dependencies/${configurationName}.txt`
#### List Format Baseline



### Goals:
- Surface transitive and non-transitive dependencies changes for our production build configurations.  This helps during version bumps and when new libraries are added.
- Provide the developer with the ability to easily accept changes as needed.
- Add deny-listing for production build configurations to explicitly block some dependencies from production.
- Include a record of the change in Git when their code is merged to easily diagnose/debug crashes in the future.
- Be deterministic.  De-duplicate entries, and order alphabetically.


### How Does Dependency Guard Work?

Dependency Guard writes a baseline file containing all your transtitive dependencies for a given configuration that should be checked into git.

Under the hood, we're leveraging the same logic that [the Gradle `dependencies` task](https://docs.gradle.org/current/userguide/viewing_debugging_dependencies.html) uses and displays in build scans, but creating a process which can guard against changes.

A baseline file is created for each build configuration you want to guard.

If the dependencies do change, you'll get easy to read warnings to help you visualize the differences, and accept them by re-baselining.  This new baseline will be visible in your Git history to help understand when dependencies changed (including transitive).

### Usage
Apply the Dependency Guard plugin to any module using the following configuration.

```groovy
plugins {
  id("com.dropbox.dependency-guard")
}

dependencyGuard {}
```

```kotlin
dependencyGuard {
  configuration("releaseRuntimeClasspath") {
    // What is included in the list report
    modules = true // Defaults to true
    artifacts = true // Defaults to true

    // Tree Report
    tree = false // Defaults to false

    // Filter through dependencies
    isAllowed = {dependencyName: String ->
        return true // Defaults to true
    }
  }
}
```

If you don't know what configurations are available, just run the `dependencyGuard` task with no configurations provided, and you will get a warning with available configurations for this module:
```
> Error: No configurations provided to Dependency Guard Plugin.
 Here are some valid configurations you could use.
 
 dependencyGuard {
   configuration("compileClasspath")
   configuration("runtimeClasspath")
   configuration("testCompileClasspath")
   configuration("testRuntimeClasspath")
 }
```

Full configuration:
```groovy
// module/build.gradle(.kts)
plugins {
  id("com.dropbox.dependency-guard")
}

dependencyGuard {
    configuration("runtimeClasspath")
}
```


### Setup

Be sure to include the Dependency Guard plugin to your buildscript classpath, just like you do with the Kotlin Gradle Plugin or Android Gradle Plugin.

```groovy
// build.gradle(.kts)
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // SNAPSHOT Versions
        maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots" }
    }
    dependencies {
        classpath("com.dropbox.dependency-guard:dependency-guard:0.1.0-SNAPSHOT")
    }
}
```

## Suggested Workflow

The Dependency Guard plugin adds a few tasks for you to use in your Gradle Builds.  Your continuous integration environment would run the `dependencyGuard` task to ensure things did not change, and require developers to re-baseline using the `dependencyGuardBaseline` tasks when changes were intentional.

## Tasks

### `dependencyGuard`

Compare against the configured transitive dependency report baselines, or generate them if they don't exist.

This task is added to any project that you apply the plugin to.  The plugin should only be applied to project you are interested in tracking transtive dependencies for.

### `dependencyGuardBaseline`

This task overwrites the dependencies in the `dependency-guard` directory in your module.

## Baseline Files

Baseline files are created in the "dependency-guard" folder in your module.  The following reports are created for the `:sample:app` module by running `./gradlew :sample:app:dependencyGuardBaseline`


## License

    Copyright (c) 2022 Dropbox, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


