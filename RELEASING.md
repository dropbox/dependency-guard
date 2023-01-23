Releasing
=========

1. Change the version in `dependency-guard/gradle.properties` to a non-SNAPSHOT version.
2. Update the `README.md` to reflect the new version number.
3. Update the `CHANGELOG.md` for the impending release.
4. Create a pull request with the stable version number and merge it to trigger CI to deploy the release.
5. Pull the latest code from `main`.
6. Tag the release with `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
7. Push the tag `git push origin X.Y.Z` 
8. Create a new release from the new tag at https://github.com/dropbox/dependency-guard/releases
9. Checkout a new branch from main.
10. Update the `gradle.properties` to the next SNAPSHOT version.
11. Create a PR to update to the new SNAPSHOT version and merge it.
