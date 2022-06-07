#  Introducing the Dependency Guard 🛡️ Gradle Plugin


Dropbox has open sourced [Dependency Guard 🛡️ on GitHub](https://github.com/dropbox/dependency-guard)! Dependency Guard is a Gradle plugin that helps you guard against *unintentional dependency changes*.  This is critical for monitoring release build configurations that you ship to customers.  Changes in either one of these configurations impact what your users are downloading onto their devices.

## Why Was Dependency Guard Built?

Our Mobile Foundation team does a lot of library upgrades, and we needed insight into how dependencies are changing over time.  Any one line change can have a huge impact. On large teams like Dropbox, it's not possible to track all dependency changes in a practical way, so we needed tooling to help.  After an evaluation and conversations in the community, we realized there was a gap in what exists today.  Because of this, we set out to build something that could help us, and any other team out there. Dependency Guard solves this problem that probably every team that uses Gradle is facing.

## Goals:
- Surface transitive and non-transitive dependencies changes for our production build configurations.
- Provide developers an easy way to accept intentional changes.
- Provide helpful, targeted error messaging, so that it's clear what has changed. This includes color higlighting in the terminal.
- Provide an `allowedFilter` to deny any dependencies you wouldn't want in a production build.
- Include a record of the change in Git when their code is merged to easily diagnose/debug issues in the future. 
- Provide configurability to allow this to work for any project.
- Be deterministic. De-duplicate entries, and order alphabetically.

## Dependency Guard in Action
We've reached out to other members of the community on large teams that use Gradle for their builds and asked them to give Dependency Guard a try.  Not only have they tried it, but they have already found so much value that they have introduced it into their workflow going forward. Some of these early adopters have already found out they were shipping test dependencies, and now have an automated way to make sure that doesn't ever happen again.  Dependency Guard is here because manually checking dependency graph changes is impossible to keep up with.  For a given configuration, you may never want `junit` to be shipped.  You can prevent this by modifying the `allowedFilter` rule to return `!it.contains("junit")` for your situation.  This is 100% configurable and you can choose what works best for you project.




## The Development of Dependency Guard

It started with dependency trees.  The plugin uses the same functionality as the Gradle Task `:dependencies` under the hood.  The tree format proved to be very helpful in seeing where these new dependencies came from, but we found that the tree format created more noise than signal in our Git history.  It's super helpful to use for development, but we wouldn't recommend storing the tree format in Git, especially in large projects as it gets noisy, and it becomes ignored.  In brainstorming with [@joshfein](https://twitter.com/joshfein), it seemed like a simple orded, de-duplicated list of dependencies were better for storing in CI.

The [`dependency-tree-diff` library](https://github.com/JakeWharton/dependency-tree-diff) is an extremely helpful tool to visualize differences in the dependency tree dumps, but is impractical for many projects in Continuous Integration since it has a lot of custom setup to enable it. ([related discussion](https://github.com/JakeWharton/dependency-tree-diff/discussions/8)). `dependency-tree-diff`'s diffing logic is actually used by Dependency Guard to highlight changes in trees.

You can enable tree support if you choose with `tree = true` in your Dependency Guard configuration.

## How Does Dependency Guard Work?

Dependency Guard writes a baseline file containing all your transtitive dependencies for a given configuration that should be checked into git.

Under the hood, we're leveraging the same logic that [the Gradle `dependencies` task](https://docs.gradle.org/current/userguide/viewing_debugging_dependencies.html) uses and displays in build scans, but creating a process which can guard against changes.

A baseline file is created for each build configuration you want to guard.

If the dependencies do change, you'll get easy to read warnings to help you visualize the differences, and accept them by re-baselining.  This new baseline will be visible in your Git history to help understand when dependencies changed (including transitive).


## Full Configuration Options

```kotlin
dependencyGuard {
  configuration("releaseRuntimeClasspath") {
    // What is included in the list report
    artifacts = true // Defaults to true
    modules = false // Defaults to false

    // Tree Report
    tree = false // Defaults to false

    // Filter through dependencies and return true if allowed.  Build will fail if unallowed.
    allowedFilter = {dependencyName: String ->
        return true // Defaults to true
    }
    // Modify a dependency name or remove it (by returning null) from the baseline file
    baselineMap = { dependencyName: String ->
      return dependencyName // Defaults to return itself
    }
  }
}
```

## Suggested Workflows

The Dependency Guard plugin adds a few tasks for you to use in your Gradle Builds.  Your continuous integration environment would run the `dependencyGuard` task to ensure things did not change, and require developers to re-baseline using the `dependencyGuardBaseline` tasks when changes were intentional.

Give Dependency Guard a try, and let us know what value it provides to your team, or what we can do to make it better for everyone!

Also, our mobile team is actively hiring, please apply here if you are interested in joining our amazing team.