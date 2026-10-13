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

The immutable coordinate is declared in `.github/compose-library.version` (initial value `0.1.0`). A protected `main` push that changes this file triggers `publish-library.yml`; a new annotated tag with matching semantic version is an alternative trigger. The release workflow stages and validates the library AAR, POM, Gradle module metadata and sources archive, runs library JVM tests, lint, Kotlin API checks and an independent Gallery build against the staged binary, then publishes to `maven-repository`. Plan and Puanla are downstream consumers, **not release prerequisites**. Their UI suites can be run independently using `optional-consumer-compat.yml`; a downstream incompatibility does not prevent a valid library release.

A public, credential-free Maven repository is then available at:

```kotlin
maven("https://raw.githubusercontent.com/knigdelioglu/compose-design-system/maven-repository/")
```

**A repository branch is not an immutable package registry.** Published coordinates are never intentionally overwritten; branch write access must be restricted and old artefacts retained. GitHub Pages or a managed public Maven repository can replace this distribution transport later without changing artifact coordinates. GitHub Packages is **not** used for consumer downloads because its Maven registry requires tokens even for public packages.

Before tagging, test the locally staged Maven repository in both consumers with -PcomposePublishedVersion=0.1.0-SNAPSHOT and -PcomposePublishedRepositoryDir=<absolute Compose checkout>/build/p0-maven-repository. Generate this repository from the candidate source branch. Do not publish the consumer applications until their compatibility checks pass. Releasing does not automatically publish either app's APK.

## Consumer migration and rollback

The **optional** `.github/workflows/optional-consumer-compat.yml` manually prepares a staged AAR and calls `.github/workflows/consumer-compat.yml`; this checks stable Plan/Puanla `main` rather than short-lived `codex/*` branches and actually executes connected Android tests. Normal library CI and the Maven release workflow do not depend on these applications. Run the consumers with opt-in `-PcomposePublishedVersion=<released-version>` before switching the default dependency. Only after the first release is publicly addressable and independently accepted should either app switch defaults and remove old source-path dependencies. Keep the original pinned SHA and workflow history available for immediate rollback. Product-specific UI adapters and data layers must remain unchanged.

## Quality gates

- Released Maven metadata resolves all externally visible Compose API dependencies without copied source files.
- The library passes its own tests, lint and API checks, and Gallery resolves the staged AAR independently. Each downstream consumer is responsible for its own critical UI tests, data safety and UX contract when it adopts a newer library version.
- A consumer's migration does not force synchronized Kotlin/AGP upgrades or delay the library release; validate each consuming toolchain independently.
- Failed downstream compatibility checks leave current app dependencies and releases untouched; they do not block the library publication.

## Release safety

- Never retag, overwrite or re-publish an existing Maven version. The GitHub API coordinate check rejects already published files, the publication concurrency group serializes writers, and failed checks never reach the publishing job.
- An unmerged PR does not change the public Maven branch. Update Plan and Puanla defaults only after the actual release artefact exists.
- Protect the `maven-repository` branch and `v*` tags against manual force pushes or rewrites; repository settings are separate from code and have not been changed by this PR.
- Android app release version codes and end-user APK distribution are independent of library releases.

## Independent product lifecycle

The Compose Design System is a reusable product, not a Plan/Puanla submodule. Library releases use semantic versions and only first-party quality gates. Consumers pin whichever published version they have independently accepted. To investigate regressions affecting the current applications, manually run **Optional downstream compatibility (Plan and Puanla)** from Actions. This is advisory to each consuming team, never a publication gate.
