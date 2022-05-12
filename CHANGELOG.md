# Change Log

## Version 0.2.0

_2022-05-12_

See: https://github.com/dropbox/dependency-guard/milestone/1?closed=1

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
