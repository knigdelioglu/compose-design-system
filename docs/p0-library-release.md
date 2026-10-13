# P0: repeatable Android library publication

## Baseline (2026-10-13)

| Repository | Baseline `main` commit | Consumption |
| --- | --- | --- |
| compose-design-system | `19b6e6cc28901d032814354c4a1d8e97a3047240` | library source |
| puanla | `60d5aefe77f995980dd56e1bdbd42bc56269f316` | external Gradle project + pinned SHA |
| plan | `6c2c4f61c684dee1ba771bc50727bd2533f5e7d5` | Kotlin source injection + pinned SHA |

Public API baseline: `design-system/api/design-system.api`. The existing packages `com.knigdelioglu.arc.compose.*` are preserved. This change does **not** assert backwards binary compatibility with previously compiled third-party AARs.

## Build and validate without publishing

```sh
./gradlew :design-system:assembleRelease :design-system:apiCheck :design-system:testDebugUnitTest
./gradlew :design-system:publishReleasePublicationToStagingRepository
./gradlew :gallery:assembleDebug -PcomposeSmokePublished=true
```

Staged artifact coordinates: `com.knigdelioglu:compose-design-system:0.1.0-SNAPSHOT`.
The smoke test resolves the published AAR and Maven/Gradle metadata rather than `project(":design-system")`.
Gradle's `api` dependencies export Compose types referenced by the public API.

## Immutable releases

After P0 source CI and real consumer acceptance tests pass, create a **new** annotated release tag such as `v0.1.0`. Tag pushes execute `publish-library.yml`; the workflow verifies release compilation, JVM tests, lint, API checks and a Maven consumer smoke build before publishing the AAR, POM, Gradle module metadata and sources archive into a separate `maven-repository` branch.

A public, credential-free Maven repository is then available at:

```kotlin
maven("https://raw.githubusercontent.com/knigdelioglu/compose-design-system/maven-repository/")
```

**A repository branch is not an immutable package registry.** Published coordinates are never intentionally overwritten; branch write access must be restricted and old artefacts retained. GitHub Pages or a managed public Maven repository can replace this distribution transport later without changing artifact coordinates. GitHub Packages is **not** used for consumer downloads because its Maven registry requires tokens even for public packages.

Before tagging, test the locally staged Maven repository in both consumers with -PcomposePublishedVersion=0.1.0-SNAPSHOT and -PcomposePublishedRepositoryDir=<absolute Compose checkout>/build/p0-maven-repository. Generate this repository from the candidate source branch. Do not publish the consumer applications until their compatibility checks pass. Releasing does not automatically publish either app's APK.

## Consumer migration and rollback

First run Puanla and Plan with opt-in `-PcomposePublishedVersion=<released-version>` and execute both apps' unit and UI tests. Only after validation, make the versioned artifact the default dependency and remove old source-path dependencies. Keep the original pinned SHA and workflow history available for immediate rollback. Product-specific UI adapters and data layers must remain unchanged.

## Quality gates

- Released Maven metadata resolves all externally visible Compose API dependencies without copied source files.
- Puanla and Plan compile, run critical UI tests, preserve app data and retain their current UX contracts.
- Consumer migration does not require synchronized Kotlin/AGP upgrades; validate both toolchains independently.
- Failed consumer checks leave current app dependencies and releases untouched.
