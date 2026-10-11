# Compose Design System

Reusable Android UI components built with Kotlin and Jetpack Compose.

This repository consolidates the shared component library used by the Plan and Puanla applications. The library is an Android Gradle module with no dependency on either application's domain models or data layer.

## What's included

- Theme, color, spacing, shape, and motion tokens
- Buttons, forms, selection controls, pickers, navigation, and panels
- Feedback, media, data visualization, and table components
- Selection-aware rich text editor
- Teaching schedule calendar using library-owned date models
- System animator-scale observation with an explicit reduced-motion override

## Requirements

- Android min SDK 26
- Java 17
- Compile SDK 36

## Build

```sh
./gradlew :design-system:assembleDebug
```

Run the library's JVM tests with:

```sh
./gradlew :design-system:testDebugUnitTest
```

## Module

The public Kotlin packages currently use `com.knigdelioglu.arc.compose.*` to preserve source compatibility with the existing Puanla module while both applications migrate. The repository and distribution identity are `Compose Design System`.

## Licensing

See [LICENSE](LICENSE) for the license and required notices.
