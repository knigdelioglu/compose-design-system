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
- An interactive native Compose component gallery in `:gallery`

## Component gallery

Build and install the showcase on a connected Android device or emulator:

```sh
./gradlew :gallery:installDebug
```

The gallery demonstrates the shared theme, all eight accent colors, reduced motion, core controls, native keyboard interactions, panels, tables, and charts. It is a separate Android application module; the library itself remains an Android library.

## Arc reference and behavior status

This library adapts interaction patterns and visual direction from [Arc Library](https://github.com/kuratlielia/arc-library) to native Android APIs. It does not embed the web implementation. A component-by-component status and the intentional platform differences are tracked in [the behavior matrix](docs/behavior-equivalence.md).

Public API additions and their source/binary compatibility notes are recorded in [the API migration guide](docs/api-migration.md).

Arc Library is MIT licensed. Its license and attribution are retained in [LICENSE](LICENSE); this project continues to include its own existing notices.

### Current rich text scope

`ArcRichTextEditor` edits plain text and supports lightweight Markdown emphasis for bold (`**text**`), italic (`*text*`), quote lines (`> text`), and list insertion through toolbar actions. It is not a full Markdown editor: it does not parse nested blocks, links, code, tables, or export HTML. Apps that need those features should use a dedicated editor integration.

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
