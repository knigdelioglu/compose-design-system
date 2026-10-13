# P1/P2 behavioral hardening and performance follow-up

## P1 — correctness and UX

| Component | Issue | Remediation | Verification |
| --- | --- | --- | --- |
| `ArcMoneyInputRules` and `ArcMoneyInput` | Unknown pasted characters and repeated separators could silently alter amounts | Reject malformed values; leave the external integer minor-unit value untouched on invalid input; show an explicit error | `ArcMoneyInputRulesTest` includes invalid letters, exponent syntax, signs, duplicate decimals and overflow |
| `ArcDonutChart` | NaN, infinities, negative counts or overflowing Float totals could reach Canvas | Normalize each segment to finite nonnegative Float and sum in Double | `CriticalComponentsTest` checks invalid legend values and render stability |
| `ArcCarousel`, `ArcResizablePanels`, `ArcCalendar`, `ArcImageCompare` | Navigation/position reset on activity recreation | Save primitive state using `rememberSaveable`, preserving existing parameter signatures | UI tests for carousel bounds and existing panel interactions; rotation smoke test recommended |
| `ArcColorPicker`, `ArcFileUpload`, calendar month controls | Undersized touch targets, limited semantics | Use 48dp touch surfaces for picker/file removal/month navigation; selectable color semantics and horizontal scrolling | Color-selection instrumentation regression |
| Gallery | Theme switch left system bar icons at the wrong contrast, light by default even on a dark device | Initialize from the device's dark theme and update status/navigation icon contrast; add safe drawing insets | Gallery emulator smoke test |

**Compatibility:** Public component signatures are unchanged. `design-system.api` is updated only to reflect compiler-generated internal Compose singleton lambda IDs (an unstable compiler implementation detail); the callable control APIs were not changed.

## P2 — targeted, not speculative

- **Graphics correctness:** Signature SVG export now uses the same per-segment pressure interpolation as PNG, rather than a single average-pressure line for a whole stroke. Single-point strokes render as circles and alpha is retained.
- **Recomposition correctness:** Chart normalization remains derived from each currently observed input list so changes to Compose snapshot-backed mutable lists cannot be hidden by stale `remember(list)` caches. Optimize further only after benchmarking and explicit immutable-input contracts.
- **State retention:** Image comparison split position persists through configuration recreation.
- **Example migration:** `advanced/examples/AdvancedComponentsExample.kt` currently appears in the published binary API. Moving/removing it outright would be a breaking change. Keep it for this compatible release; relocate and deprecate intentionally with a major-version API migration rather than deleting it silently.

## Performance acceptance

No frame time, recomposition count or memory improvements are asserted without measurement. Before a performance release, record baseline and candidate figures on the **same** device/build mode for (1) bar/donut/sparkline redraw, (2) 2- and 3-pane drag, (3) carousel item switching, (4) Gallery dark mode and font-scale changes. Use Macrobenchmark/JankStats and Compose compiler metrics as appropriate; report frame P50/P95/P99 and allocation count if measured.

## Release policy

- `:design-system:apiCheck`, JVM tests, Android lint, Gallery build, and emulator instrumentation must pass.
- Plan and Puanla binary consumers are verified independently by the Compose CI release gate; do not update application Maven pins as part of this component-only PR.
- Keep P0 library publishing and application rollout separate from P1/P2 code changes.
