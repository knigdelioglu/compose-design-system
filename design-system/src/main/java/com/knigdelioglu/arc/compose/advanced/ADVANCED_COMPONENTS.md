# Advanced Components

This package contains native Jetpack Compose components inspired by broad interaction ideas in Arc Library. The implementations, state APIs, and motion calculations here are independently written for Android; they are not official Arc Pro ports. In the public Arc Library repository, Cover Flow and Wallet Stack are identified as Pro-only showcases, so their private implementation details were not used or inferred.

All components use `ArcTheme` colors, shapes, spacing, and motion tokens when present. Item models are app-owned and keyed by stable, unique strings. Selection and expansion are controlled by the caller. See `examples/AdvancedComponentsExample.kt` for a complete Compose usage sample.

## ArcDock

`ArcDock(items, selectedKey, onItemSelected, showLabels, modifier)` is a horizontal destination dock. `ArcAdvancedItem.active` displays a separate running/active marker; `selectedKey` remains the navigation selection. Labels and icons are optional, while the content description remains available for accessibility. A pointer position applies a smooth bounded influence to the icon size and cell width, which lets nearby items shift gradually. Cells scroll horizontally when the available width cannot fit them.

Touch and mouse/stylus pointer movement are handled by Compose pointer input; selection uses a selectable semantics node, standard focus traversal, and keyboard activation. The full cell retains at least a 48dp touch target. Reduced motion uses the theme's high-stiffness spring. Labels may be hidden visually while the content description remains available.

## ArcMorphNavigation

`ArcMorphNavigation(destinations, selectedKey, onSelectedKeyChange, expanded, onExpandedChange)` keeps the compact trigger and destination list in one rounded surface. Width, corner radius, and content height transition together. A choice updates the selected key, closes the surface, and restores focus to the trigger. Android Back and Escape request a close; callers can also close it by changing `expanded`.

Rows expose selected/enabled tab semantics and use Compose focus traversal. The component is an in-place anchored surface and does not install a window-level outside-tap interceptor; a host that needs outside dismissal should close `expanded` from its surrounding interaction policy. It registers the platform back dispatcher on Android 13+ and handles Escape/Back key events. Android 12 and earlier hosts should close `expanded` from their activity's back callback.

## ArcGlassTabBar

`ArcGlassTabBar(tabs, selectedKey, onTabSelected, modifier)` provides equal-width tabs while they fit and a horizontally scrollable 76dp minimum cell when they do not. Its selected capsule tracks measured cell positions with a spring, including after a programmatic selection. Labels and icons are optional; `badgeCount` is shown visually and included in the accessibility description.

The surface uses translucent tonal layers, border, and elevation. It intentionally does not apply blur: this keeps contrast and behavior consistent across Android versions and hardware. Selection uses tab semantics, focus traversal, and standard keyboard activation. Reduced motion is inherited by the indicator spring.

## ArcWalletStack

`ArcWalletStack(cards, selectedKey, onSelectedKeyChange, onOrderChange, cardContent)` accepts a front-to-back list and reports reorder requests as a list of stable keys. A horizontal drag of the front card rotates it to the back on a left release or brings the last card to front on a right release; short drags and cancelled gestures spring to rest. The selected card is visually raised and may expand its content through the slot's `selected` argument.

At most four layers are composed at once. The selected item is kept among those layers; all cards remain reachable through left/right keyboard movement and Previous/Next accessibility actions. A Move selected card to front accessibility action exposes the same explicit ordering callback. An empty slot is rendered for zero cards. Card body content is application supplied.

## ArcCoverFlow

`ArcCoverFlow(items, selectedKey, onSelectedKeyChange, itemContent, showPageIndicator)` uses `LazyRow`, so only visible content is composed. Side cards use bounded scale, opacity, vertical offset, and Y rotation; the nearest item becomes selected after scrolling settles. Clicking, keyboard arrows, or Previous/Next accessibility actions select and center an item. External selection and keyed data changes scroll to the requested item. Empty content can be replaced with `emptyContent`.

Touch and pointer scrolling use Compose's native scroll handling; cards are keyboard focusable through their click action, and each item reports its label and selected state. Reduced motion switches selection movement to an immediate list scroll and disables the nonessential perspective transforms. There is no reflection bitmap or blur pass.

## Integration notes

- Include the existing `:design-system` module and import from `com.knigdelioglu.arc.compose.advanced`.
- Provide unique keys and update the supplied list when `onOrderChange` requests a new order.
- Wrap app content in `ArcTheme` to inherit the project's tokens and reduced-motion setting.
- The package does not require a new Gradle module or modify shared theme APIs.
- Compose UI/instrumentation dependencies are not declared in the current module. Automated coverage in this change focuses on the pure motion/order helpers; compile validation covers the component sources.
