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

The immutable coordinate is declared in `.github/compose-library.version` (initial value `0.1.0`). A protected `main` push that changes this file triggers `publish-library.yml`; a new annotated tag with matching semantic version is an alternative trigger. The workflow stages the release AAR, POM, module metadata and sources archive, then runs Plan's compact/portrait/landscape and Puanla's tablet *connected emulator suites against that exact AAR*. Only if all suites succeed does it write to `maven-repository`. A failed consumer suite leaves the public Maven repository untouched.

A public, credential-free Maven repository is then available at:

```kotlin
maven("https://raw.githubusercontent.com/knigdelioglu/compose-design-system/maven-repository/")
```

**A repository branch is not an immutable package registry.** Published coordinates are never intentionally overwritten; branch write access must be restricted and old artefacts retained. GitHub Pages or a managed public Maven repository can replace this distribution transport later without changing artifact coordinates. GitHub Packages is **not** used for consumer downloads because its Maven registry requires tokens even for public packages.

Before tagging, test the locally staged Maven repository in both consumers with -PcomposePublishedVersion=0.1.0-SNAPSHOT and -PcomposePublishedRepositoryDir=<absolute Compose checkout>/build/p0-maven-repository. Generate this repository from the candidate source branch. Do not publish the consumer applications until their compatibility checks pass. Releasing does not automatically publish either app's APK.

## Consumer migration and rollback

The PR and release workflows use `.github/workflows/consumer-compat.yml`, which checks out stable Plan/Puanla `main` rather than short-lived `codex/*` branches, and runs Android emulator UI tests instead of just compiling androidTest APKs. Run the consumers with opt-in `-PcomposePublishedVersion=<released-version>` before switching the default dependency. Only after the first release is publicly addressable and independently accepted should either app switch defaults and remove old source-path dependencies. Keep the original pinned SHA and workflow history available for immediate rollback. Product-specific UI adapters and data layers must remain unchanged.

## Quality gates

- Released Maven metadata resolves all externally visible Compose API dependencies without copied source files.
- Puanla and Plan compile, run critical UI tests, preserve app data and retain their current UX contracts.
- Consumer migration does not require synchronized Kotlin/AGP upgrades; validate both toolchains independently.
- Failed consumer checks leave current app dependencies and releases untouched.

## Release safety

- Never retag, overwrite or re-publish an existing Maven version. The GitHub API coordinate check rejects already published files, the publication concurrency group serializes writers, and failed checks never reach the publishing job.
- An unmerged PR does not change the public Maven branch. Update Plan and Puanla defaults only after the actual release artefact exists.
- Protect the `maven-repository` branch and `v*` tags against manual force pushes or rewrites; repository settings are separate from code and have not been changed by this PR.
- Android app release version codes and end-user APK distribution are independent of library releases.
