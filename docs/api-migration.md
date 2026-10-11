# API compatibility and migration

This update preserves existing Kotlin call sites. New parameters have defaults, existing parameter names and types remain unchanged, and the original `ArcTheme(darkTheme, reducedMotion, content)` overload remains available. Generic components now use domain-neutral Turkish placeholder and confirmation labels by default; pass the existing label parameters to restore application-specific wording.

## Additions

| API | Addition | Existing Kotlin call sites |
| --- | --- | --- |
| `ArcTheme` | `accent: ArcAccent` overload | Continue to use the original overload. |
| `ArcTimePicker` | Optional `enabled` state | Existing calls use the default enabled state. |
| `ArcShortcutRecorder` | Optional conflicting shortcut set | Existing calls record shortcuts without a conflict set. |
| `ArcMorphSelect` | Optional placeholder and enabled state | Existing calls retain the default placeholder and enabled state. |
| `ArcCombobox` | Optional enabled state | Existing calls remain enabled by default. |
| `ArcMultiSelect` | Optional search/filter controls and enabled state | Existing calls keep the size-based search default. |
| `ArcRadioGroup`, `ArcRadioCards` | Optional enabled state | Existing calls remain enabled by default. |
| `ArcColors`, `ArcTheme` | `ArcAccent`, accent variant and visualization palette | Existing theme calls keep the neutral accent; use the overload with `ArcAccent` to select one of eight palettes. |
| `ArcMotionTokens` | Timing, stagger and blur tokens plus reduced-motion-aware spring specs | Existing token construction keeps defaults; spring helpers now return `FiniteAnimationSpec<T>` rather than `SpringSpec<T>`. Calls passed directly to animation APIs continue to work. |
| `ArcSignaturePad` | Optional `onSignature` export callback; `ArcSignature`, `ArcSignatureStroke`, and `ArcSignaturePoint` data types | Existing drawing/change callbacks remain available. |
| `ArcResizablePanels` | Optional pane limits, collapse configuration, and weight callback | Existing layouts keep the default limits and behavior. |
| `ArcSortableDataTable` | Optional controlled row selection | Existing tables remain unselected unless selection is supplied. |
| Charts | Optional accessible descriptions and value bounds | Existing calls retain their existing visual configuration. |
| `ArcInput`, `ArcSearchField` | Optional text-field modifier for focus and key routing | Existing component modifiers keep their previous placement. |
| `ArcCommandPalette` | `FocusRequester` overload for deterministic invoker focus restoration | Existing calls keep platform-managed dialog focus behavior; pass a requester attached to the invoker to restore it explicitly. |
| `ArcRichTextEditor`, `ArcHoldToConfirm` | Domain-neutral default placeholder and confirmation text | Existing parameter overrides remain unchanged; callers using defaults see generic labels. |

Call sites that store `ArcMotionTokens.springResponsive()` (or another spring helper) in a `SpringSpec<T>` variable should widen that variable to `FiniteAnimationSpec<T>`. The wider type is needed because reduced motion may return a snap spec.

## Binary consumers

Compose generates JVM method signatures from composable parameters. Adding optional parameters changes those signatures even though Kotlin source calls remain valid. Applications that consume a previously compiled AAR must rebuild against this version; this update does not promise binary compatibility with an older AAR.

The repository now keeps `design-system/api/design-system.api` and runs `apiCheck` in CI to detect future public API changes. The repository had no prior ABI baseline, so the checked-in dump establishes the baseline for subsequent releases rather than validating compatibility with an already published artifact.
