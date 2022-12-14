# Change Log

## Version 0.4.0

* Support for Configuration Cache by [@qwert2603](https://github.com/qwert2603) in [#51](https://github.com/dropbox/dependency-guard/pull/51)

## Version 0.3.2

_2022-09-14_

See: [0.3.2 Milestone](https://github.com/dropbox/dependency-guard/milestone/5?closed=1)

* Fix "baseline created" message in DependencyGuardTreeDiffer by [@handstandsam](https://github.com/handstandsam) in [#49](https://github.com/dropbox/dependency-guard/pull/49)
* Fix: Update to latest dependency-tree-diff source code which has fixes by [@qwert2603](https://github.com/qwert2603) in [#47](https://github.com/dropbox/dependency-guard/pull/47)


## Version 0.3.1

_2022-06-13_

See: [0.3.1 Milestone](https://github.com/dropbox/dependency-guard/milestone/4?closed=1)

* Fix: Tree format required Gradle 7.4 or higher. by [@handstandsam](https://github.com/handstandsam) in [#40](https://github.com/dropbox/dependency-guard/issues/40)
* Fix: Crash while using in root project if clean task already applied. by [@ZacSweers](https://github.com/ZacSweers) in [#38](https://github.com/dropbox/dependency-guard/issues/38)


## Version 0.3.0

_2022-05-26_

See: [0.3.0 Milestone](https://github.com/dropbox/dependency-guard/milestone/2?closed=1)

* Hook `dependencyGuard` task automatically into `check` task. by [@handstandsam](https://github.com/handstandsam) in [#30](https://github.com/dropbox/dependency-guard/issues/30)
* Removed implementation dependencies of dependency-guard that aren't needed. [@handstandsam](https://github.com/handstandsam) in [#32](https://github.com/dropbox/dependency-guard/issues/32)

## Version 0.2.0

_2022-05-12_

See: [0.2.0 Milestone](https://github.com/dropbox/dependency-guard/milestone/1?closed=1)

* **BREAKING CHANGE**: Renamed `allowRule` -> `allowedFilter` by [@handstandsam](https://github.com/handstandsam) in [#21](https://github.com/dropbox/dependency-guard/pull/21)
* **BEHAVIOR CHANGE**: Set `artifacts=true` and `modules=false` as the default config. by [@handstandsam](https://github.com/handstandsam) in [#17](https://github.com/dropbox/dependency-guard/pull/17)
* Fixes: dependencyGuard failure output contains a double-colon (::) when run on root project. Should be a single colon. by [@handstandsam](https://github.com/handstandsam) in [#2](https://github.com/dropbox/dependency-guard/issues/2)
* Fixes: Tasks not compatible with the configuration cache by [@autonomousapps](https://github.com/autonomousapps) in [#4](https://github.com/dropbox/dependency-guard/issues/4)
* baselineFilter - Filter out certain dependencies from baseline (allowed, but ignored for baseline purposes) by [@handstandsam](https://github.com/handstandsam) in [#15](https://github.com/dropbox/dependency-guard/issues/15) 
* Surface the "diff" in the Thrown Exception. by [@handstandsam](https://github.com/handstandsam) in [#19](https://github.com/dropbox/dependency-guard/issues/19) 
* Update Output to use the file:/// syntax so that links are clickable.  by [@handstandsam](https://github.com/handstandsam) in [#23](https://github.com/dropbox/dependency-guard/issues/23) 

## Version 0.1.0

_2022-04-25_

* Initial Version
