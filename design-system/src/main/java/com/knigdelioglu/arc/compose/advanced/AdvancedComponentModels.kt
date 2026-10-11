package com.knigdelioglu.arc.compose.advanced

import androidx.compose.runtime.Composable

/** Stable, app-owned navigation item. Keys must remain unique while the item is present. */
data class ArcAdvancedItem(
    val key: String,
    val label: String? = null,
    val contentDescription: String = label ?: key,
    val enabled: Boolean = true,
    val active: Boolean = false,
    val badgeCount: Int? = null,
    val icon: (@Composable () -> Unit)? = null
)

/** Returns a smooth, bounded influence for the dock's pointer response. */
internal fun arcDockInfluence(distancePx: Float, radiusPx: Float): Float {
    if (radiusPx <= 0f) return 0f
    val linear = (1f - kotlin.math.abs(distancePx) / radiusPx).coerceIn(0f, 1f)
    return linear * linear * (3f - 2f * linear)
}

/** A bounded depth factor used by the cover-flow transforms. */
internal fun arcCoverDepth(distanceInItems: Float): Float =
    (1f - kotlin.math.abs(distanceInItems).coerceAtMost(1f)).coerceIn(0f, 1f)

/** Signed LazyList offset that places an item at the viewport center. */
internal fun arcCoverCenterScrollOffset(itemSizePx: Int, viewportWidthPx: Int): Int =
    (itemSizePx - viewportWidthPx) / 2

/** Moves a stable-key item to the front while preserving the relative order of the rest. */
internal fun <T> moveArcItemToFront(items: List<T>, key: (T) -> String, targetKey: String): List<T> {
    val targetIndex = items.indexOfFirst { key(it) == targetKey }
    if (targetIndex <= 0) return items
    return buildList(items.size) {
        add(items[targetIndex])
        items.forEachIndexed { index, item -> if (index != targetIndex) add(item) }
    }
}

/** Rotates the front card to the back (left swipe) or the last card to the front (right swipe). */
internal fun <T> reorderArcStack(items: List<T>, key: (T) -> String, direction: Int): List<T> {
    if (items.size < 2 || direction == 0) return items
    return if (direction < 0) items.drop(1) + items.first() else moveArcItemToFront(items, key, key(items.last()))
}

/** Finds the next enabled item without wrapping, starting just beyond [currentIndex]. */
internal fun <T> nextEnabledArcIndex(
    items: List<T>,
    currentIndex: Int,
    direction: Int,
    isEnabled: (T) -> Boolean
): Int {
    if (direction == 0 || items.isEmpty()) return -1
    return if (direction > 0) {
        val start = (currentIndex + 1).coerceAtLeast(0)
        if (start > items.lastIndex) -1
        else (start..items.lastIndex).firstOrNull { isEnabled(items[it]) } ?: -1
    } else {
        val start = (currentIndex - 1).coerceAtMost(items.lastIndex)
        if (start < 0) -1
        else (start downTo 0).firstOrNull { isEnabled(items[it]) } ?: -1
    }
}
