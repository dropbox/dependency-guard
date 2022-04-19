# Guards against unintentional dependency changes

## Configuration
`apply plugin: 'DependencyGuard'`

```
dependencyGuard {
    configuration("normalReleaseRuntimeClasspath")
}
```

## Usage
`./gradlew app:dependencyGuard`

## Additional Configuration
To generate out dependency trees as well, use the following configuration
```
dependencyGuard {
    configuration("normalReleaseRuntimeClasspath") {
        it.tree = true
    }
}
```

Diff for dependency trees are calculated using logic from https://github.com/JakeWharton/dependency-tree-diff
