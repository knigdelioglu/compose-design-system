package com.knigdelioglu.arc.compose.advanced

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.abs

data class ArcCoverItem(
    val key: String,
    val label: String,
    val contentDescription: String = label,
    val enabled: Boolean = true
)

/**
 * Center-snapping horizontal gallery. Only visible items are composed; item keys and
 * selected state are owned by the caller, so collection updates remain deterministic.
 */
@Composable
fun ArcCoverFlow(
    items: List<ArcCoverItem>,
    selectedKey: String?,
    onSelectedKeyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showPageIndicator: Boolean = true,
    emptyContent: @Composable () -> Unit = {
        Text("No items", color = ArcTheme.colors.textSecondary)
    },
    itemContent: @Composable (ArcCoverItem, selected: Boolean) -> Unit
) {
    val colors = ArcTheme.colors
    val state = rememberLazyListState()
    val itemKeys = remember(items) { items.map { it.key } }
    val selectedIndex = itemKeys.indexOf(selectedKey)
    val reducedMotion = ArcTheme.motion.reducedMotion
    val centerIndex by remember(state, items.size) {
        derivedStateOf { nearestCoverIndex(state) }
    }

    fun requestIndex(index: Int) {
        val item = items.getOrNull(index) ?: return
        onSelectedKeyChange(item.key)
    }

    val actions = buildList {
        if (items.isNotEmpty()) {
            add(CustomAccessibilityAction("Previous item") {
                val target = (if (selectedIndex >= 0) selectedIndex else centerIndex) - 1
                if (target >= 0) requestIndex(target)
                target >= 0
            })
            add(CustomAccessibilityAction("Next item") {
                val target = (if (selectedIndex >= 0) selectedIndex else centerIndex) + 1
                if (target < items.size) requestIndex(target)
                target < items.size
            })
        }
    }

    LaunchedEffect(itemKeys, selectedKey, reducedMotion) {
        withFrameNanos { }
        val targetIndex = itemKeys.indexOf(selectedKey)
        if (targetIndex >= 0) {
            val targetInfo = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
            val viewportCenter = (state.layoutInfo.viewportStartOffset + state.layoutInfo.viewportEndOffset) / 2f
            val targetCenter = targetInfo?.let { it.offset + it.size / 2f }
            if (targetCenter == null || abs(targetCenter - viewportCenter) > 1f) {
                val itemSize = targetInfo?.size ?: state.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                val offset = arcCoverCenterScrollOffset(itemSize, state.layoutInfo.viewportSize.width)
                if (reducedMotion) state.scrollToItem(targetIndex, offset)
                else state.animateScrollToItem(targetIndex, offset)
            }
        } else if (items.isNotEmpty() && state.layoutInfo.totalItemsCount > items.size) {
            state.scrollToItem(0)
        }

        snapshotFlow {
            if (state.isScrollInProgress) -1 else nearestCoverIndex(state)
        }
            .filter { it >= 0 }
            .distinctUntilChanged()
            .collect { index ->
                items.getOrNull(index)?.let { item ->
                    if (item.key != selectedKey) onSelectedKeyChange(item.key)
                }
            }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .focusGroup()
            .onPreviewKeyEvent { event ->
                when (event.key) {
                    Key.DirectionLeft -> {
                        requestIndex(((if (selectedIndex >= 0) selectedIndex else centerIndex) - 1).coerceAtLeast(0))
                        items.isNotEmpty()
                    }
                    Key.DirectionRight -> {
                        requestIndex(((if (selectedIndex >= 0) selectedIndex else centerIndex) + 1).coerceAtMost(items.lastIndex))
                        items.isNotEmpty()
                    }
                    else -> false
                }
            }
            .semantics {
                contentDescription = "Cover flow gallery"
                stateDescription = if (items.isEmpty()) "No items" else "${(centerIndex + 1).coerceIn(1, items.size)} of ${items.size}"
                customActions = actions
            }
    ) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) { emptyContent() }
        } else {
            val cardWidth = maxWidth.coerceAtMost(400.dp) * 0.78f
            val cardHeight = (cardWidth * 0.68f).coerceIn(180.dp, 260.dp)
            val spacing = 18.dp
            LazyRow(
                state = state,
                modifier = Modifier.fillMaxWidth().height(cardHeight + 20.dp),
                contentPadding = PaddingValues(horizontal = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                flingBehavior = ScrollableDefaults.flingBehavior()
            ) {
                itemsIndexed(items, key = { _, item -> item.key }) { index, item ->
                    val selected = index == centerIndex
                    val focused = remember(item.key) { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .height(cardHeight)
                            .graphicsLayer {
                                val info = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                                val viewportCenter = (state.layoutInfo.viewportStartOffset + state.layoutInfo.viewportEndOffset) / 2f
                                val itemCenter = info?.let { it.offset + it.size / 2f } ?: viewportCenter
                                val itemStep = (cardWidth.toPx() + spacing.toPx()).coerceAtLeast(1f)
                                val distance = (itemCenter - viewportCenter) / itemStep
                                val depth = arcCoverDepth(distance)
                                val far = abs(distance).coerceAtMost(2f)
                                scaleX = if (reducedMotion) 1f else 0.84f + depth * 0.16f
                                scaleY = scaleX
                                alpha = if (reducedMotion) 1f else (1f - far * 0.2f).coerceIn(0.5f, 1f)
                                rotationY = if (reducedMotion) 0f else (-distance * 17f).coerceIn(-28f, 28f)
                                translationY = if (reducedMotion) 0f else abs(distance).coerceAtMost(1f) * 12.dp.toPx()
                                cameraDistance = 20f * density
                            }
                            .onFocusChanged { focused.value = it.isFocused }
                            .clickable(enabled = item.enabled) { requestIndex(index) }
                            .semantics(mergeDescendants = true) {
                                contentDescription = item.contentDescription
                                this.selected = selected
                            }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(cardHeight),
                            shape = RoundedCornerShape(26.dp),
                            color = if (selected) colors.surface else colors.surfaceRaised,
                            border = androidx.compose.foundation.BorderStroke(
                                if (focused.value) 2.dp else 1.dp,
                                if (focused.value) colors.accentStrong else colors.border.copy(alpha = if (selected) 0.8f else 0.48f)
                            ),
                            shadowElevation = if (selected) 12.dp else 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                .height(cardHeight),
                                contentAlignment = Alignment.Center
                            ) { itemContent(item, selected) }
                        }
                    }
                }
            }
            if (showPageIndicator) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = cardHeight + 22.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(centerIndex + 1).coerceIn(1, items.size)} / ${items.size}",
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun nearestCoverIndex(state: androidx.compose.foundation.lazy.LazyListState): Int {
    val layout = state.layoutInfo
    if (layout.visibleItemsInfo.isEmpty()) return -1
    val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2f
    return layout.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2f - center) }?.index ?: -1
}
