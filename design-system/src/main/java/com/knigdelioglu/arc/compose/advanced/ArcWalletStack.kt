package com.knigdelioglu.arc.compose.advanced

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ArcWalletCard(
    val key: String,
    val label: String,
    val contentDescription: String = label,
    val enabled: Boolean = true
)

/**
 * A controlled physical card stack. [cards] is ordered front-to-back; a horizontal
 * release rotates that order and reports the requested stable-key order to the caller.
 */
@Composable
fun ArcWalletStack(
    cards: List<ArcWalletCard>,
    selectedKey: String?,
    onSelectedKeyChange: (String) -> Unit,
    onOrderChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit = {
        Text("No cards", color = ArcTheme.colors.textSecondary)
    },
    cardContent: @Composable (ArcWalletCard, selected: Boolean) -> Unit
) {
    val colors = ArcTheme.colors
    val motion = ArcTheme.motion
    val selectedIndex = cards.indexOfFirst { it.key == selectedKey }
    val previousEnabledIndex = nextEnabledArcIndex(cards, selectedIndex, -1) { it.enabled }
    val nextEnabledIndex = nextEnabledArcIndex(cards, selectedIndex, 1) { it.enabled }
    val displayCards = remember(cards, selectedKey) {
        if (selectedKey == null) cards else moveArcItemToFront(cards, ArcWalletCard::key, selectedKey)
    }
    // A bounded number of layers keeps large collections cheap. Any selected card is kept visible;
    // all remaining cards remain reachable by keyboard and the accessibility actions below.
    val visibleCards = remember(displayCards, selectedKey) {
        val head = displayCards.take(4)
        if (selectedKey != null && head.none { it.key == selectedKey }) {
            listOfNotNull(displayCards.firstOrNull { it.key == selectedKey }) + head.take(3)
        } else head
    }
    val scope = rememberCoroutineScope()
    val settlingX = remember { Animatable(0f) }
    val dragX = remember { mutableFloatStateOf(0f) }
    val isDragging = remember { androidx.compose.runtime.mutableStateOf(false) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 68.dp.toPx() }
    val velocityThreshold = with(density) { 900.dp.toPx() }

    fun settleDrag(offset: Float) {
        isDragging.value = false
        dragX.floatValue = 0f
        scope.launch {
            settlingX.snapTo(offset)
            settlingX.animateTo(0f, motion.springResponsive())
        }
    }

    val actions = buildList {
        if (cards.size > 1) {
            add(CustomAccessibilityAction("Previous card") {
                cards.getOrNull(previousEnabledIndex)?.let { onSelectedKeyChange(it.key) }
                previousEnabledIndex >= 0
            })
            add(CustomAccessibilityAction("Next card") {
                cards.getOrNull(nextEnabledIndex)?.let { onSelectedKeyChange(it.key) }
                nextEnabledIndex >= 0
            })
        }
        if (selectedKey != null && cards.any { it.key == selectedKey }) {
            add(CustomAccessibilityAction("Move selected card to front") {
                onOrderChange(moveArcItemToFront(cards, ArcWalletCard::key, selectedKey).map { it.key })
                true
            })
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .focusGroup()
            .focusable()
            .onPreviewKeyEvent { event ->
                when (event.key) {
                    Key.DirectionLeft -> {
                        cards.getOrNull(previousEnabledIndex)?.let { onSelectedKeyChange(it.key) }
                        previousEnabledIndex >= 0
                    }
                    Key.DirectionRight -> {
                        cards.getOrNull(nextEnabledIndex)?.let { onSelectedKeyChange(it.key) }
                        nextEnabledIndex >= 0
                    }
                    else -> false
                }
            }
            .semantics {
                contentDescription = "Wallet card stack"
                stateDescription = if (cards.isEmpty()) "No cards" else "${(selectedIndex + 1).coerceAtLeast(1)} of ${cards.size}"
                customActions = actions
            }
    ) {
        val maxCardWidth = maxWidth.coerceAtMost(480.dp)
        if (cards.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(236.dp), contentAlignment = Alignment.Center) { emptyContent() }
        } else {
            val peek = 20.dp
            val cardHeight = 228.dp
            val stackHeight = cardHeight + peek * (visibleCards.size - 1)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(stackHeight),
                contentAlignment = Alignment.TopCenter
            ) {
                visibleCards.indices.reversed().forEach { index ->
                    val card = visibleCards[index]
                    key(card.key) {
                        val selected = card.key == selectedKey
                        val focused = remember(card.key) { mutableStateOf(false) }
                        val topCard = card.key == displayCards.firstOrNull()?.key
                        val targetY = if (selected) 0.dp else peek * index
                        val y by animateDpAsState(targetY, ArcTheme.motion.springGentle(), label = "wallet-y-${card.key}")
                        val height by animateDpAsState(if (selected) cardHeight else cardHeight - 12.dp, ArcTheme.motion.springGentle(), label = "wallet-height-${card.key}")
                        val dragModifier = if (topCard && card.enabled) {
                            Modifier.pointerInput(card.key, cards) {
                                val velocityTracker = VelocityTracker()
                                detectDragGestures(
                                    onDragStart = {
                                        velocityTracker.resetTracking()
                                        dragX.floatValue = 0f
                                        isDragging.value = true
                                        scope.launch { settlingX.stop() }
                                    },
                                    onDragEnd = {
                                        val velocity = velocityTracker.calculateVelocity().x
                                    val offset = dragX.floatValue
                                    val direction = when {
                                        offset < -swipeThresholdPx || velocity < -velocityThreshold -> -1
                                        offset > swipeThresholdPx || velocity > velocityThreshold -> 1
                                        else -> 0
                                    }
                                        settleDrag(offset)
                                        if (direction != 0) {
                                            val newOrder = reorderArcStack(cards, ArcWalletCard::key, direction)
                                            onOrderChange(newOrder.map { it.key })
                                            newOrder.firstOrNull()?.let { onSelectedKeyChange(it.key) }
                                        }
                                    },
                                    onDragCancel = { settleDrag(dragX.floatValue) },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                                        dragX.floatValue += dragAmount.x
                                    }
                                )
                            }
                        } else Modifier

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = y.coerceAtLeast(0.dp))
                                .width(maxCardWidth)
                                .height(height)
                                .zIndex((visibleCards.size - index).toFloat())
                                .then(dragModifier)
                                .onFocusChanged { focused.value = it.isFocused }
                                .clickable(enabled = card.enabled) { onSelectedKeyChange(card.key) }
                                .semantics(mergeDescendants = true) {
                                    contentDescription = card.contentDescription
                                    this.selected = selected
                                    if (!card.enabled) disabled()
                                }
                                .graphicsLayer {
                                    translationX = if (topCard && isDragging.value) dragX.floatValue else if (topCard) settlingX.value else 0f
                                    rotationZ = if (topCard && (isDragging.value || settlingX.value != 0f)) {
                                        ((if (isDragging.value) dragX.floatValue else settlingX.value) / size.width.coerceAtLeast(1f) * 4f).coerceIn(-4f, 4f)
                                    } else 0f
                                    scaleX = if (selected) 1f else 1f - (index * 0.012f).coerceAtMost(0.04f)
                                    scaleY = scaleX
                                },
                            shape = RoundedCornerShape(26.dp),
                            color = if (selected) colors.surface else colors.surfaceRaised,
                            border = BorderStroke(
                                if (focused.value) 2.dp else 1.dp,
                                if (focused.value) colors.accentStrong else if (selected) colors.borderStrong.copy(alpha = 0.55f) else colors.borderSubtle
                            ),
                            shadowElevation = if (selected) 12.dp else (8 - index).coerceAtLeast(2).dp
                        ) {
                            Box(Modifier.padding(20.dp)) { cardContent(card, selected) }
                        }
                    }
                }
            }
            if (cards.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = stackHeight + 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = previousEnabledIndex >= 0,
                        onClick = { cards.getOrNull(previousEnabledIndex)?.let { onSelectedKeyChange(it.key) } }
                    ) {
                        Text("‹", modifier = Modifier.semantics { contentDescription = "Previous card" })
                    }
                    Text(
                        text = "${(selectedIndex + 1).coerceAtLeast(1)} / ${cards.size}",
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        enabled = nextEnabledIndex >= 0,
                        onClick = { cards.getOrNull(nextEnabledIndex)?.let { onSelectedKeyChange(it.key) } }
                    ) {
                        Text("›", modifier = Modifier.semantics { contentDescription = "Next card" })
                    }
                }
            }
        }
    }
}
