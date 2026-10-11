package com.knigdelioglu.arc.compose.advanced

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import kotlin.math.abs

/**
 * A native, pointer-aware dock. The controlled [selectedKey] is navigation state;
 * [ArcAdvancedItem.active] independently marks an item with running content.
 */
@Composable
fun ArcDock(
    items: List<ArcAdvancedItem>,
    selectedKey: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    val colors = ArcTheme.colors
    val centers = remember { mutableStateMapOf<String, Float>() }
    val pointerX = remember { mutableFloatStateOf(Float.NaN) }
    val rootLeft = remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val influenceRadius = with(density) { 112.dp.toPx() }
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier,
        shape = ArcTheme.shapes.pill,
        color = colors.surface.copy(alpha = if (colors.isDark) 0.97f else 0.94f),
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.8f)),
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showLabels) 78.dp else 64.dp)
                .onGloballyPositioned { rootLeft.floatValue = it.positionInRoot().x }
                .pointerInput(items.map { it.key }) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull()
                            when (event.type) {
                                PointerEventType.Move, PointerEventType.Press -> {
                                    if (change != null) pointerX.floatValue = change.position.x
                                }
                                PointerEventType.Release, PointerEventType.Exit -> pointerX.floatValue = Float.NaN
                            }
                        }
                    }
                }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .horizontalScroll(scrollState)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    key(item.key) {
                        val center = centers[item.key]
                        val distance = if (center == null || pointerX.floatValue.isNaN()) {
                            Float.POSITIVE_INFINITY
                        } else pointerX.floatValue + rootLeft.floatValue - center
                        val influence = arcDockInfluence(distance, influenceRadius)
                        val animatedInfluence = animateFloatAsState(influence, ArcTheme.motion.springResponsive(), label = "dock-influence-${item.key}")
                        val cellWidth = 52.dp + 22.dp * animatedInfluence.value
                        val iconSize = 24.dp + 8.dp * animatedInfluence.value
                        val pressedSource = remember { MutableInteractionSource() }
                        val pressed = pressedSource.collectIsPressedAsState()
                        val focused = remember(item.key) { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .width(cellWidth)
                                .sizeIn(minHeight = 48.dp)
                                .onGloballyPositioned { coordinates ->
                                    val next = coordinates.positionInRoot().x + coordinates.size.width / 2f
                                    val previous = centers[item.key]
                                    if (previous == null || abs(previous - next) > 0.75f) centers[item.key] = next
                                }
                                .onFocusChanged { focused.value = it.isFocused }
                                .selectable(
                                    selected = item.key == selectedKey,
                                    enabled = item.enabled,
                                    role = Role.Tab,
                                    interactionSource = pressedSource,
                                    onClick = { onItemSelected(item.key) }
                                )
                                .semantics {
                                    contentDescription = item.contentDescription
                                    if (item.active) stateDescription = "Active"
                                }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .graphicsLayer {
                                        val pressScale = if (pressed.value) 0.96f else 1f
                                        val scale = (1f + animatedInfluence.value * 0.12f) * pressScale
                                        scaleX = scale
                                        scaleY = scale
                                        translationY = -animatedInfluence.value * 3.dp.toPx()
                                    }
                                    .clip(CircleShape)
                                    .then(
                                        when {
                                            focused.value -> Modifier.border(2.dp, colors.accentStrong, CircleShape)
                                            item.key == selectedKey -> Modifier.border(1.dp, colors.accent.copy(alpha = 0.28f), CircleShape)
                                            else -> Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(Modifier.size(iconSize), contentAlignment = Alignment.Center) {
                                    if (item.icon != null) item.icon.invoke()
                                    else Text((item.label?.takeIf { it.isNotBlank() }?.take(1) ?: item.key.take(1)).uppercase(), color = if (item.enabled) colors.foreground else colors.textMuted, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (showLabels && !item.label.isNullOrBlank()) {
                                Text(
                                    text = item.label,
                                    color = when {
                                        !item.enabled -> colors.textMuted
                                        item.key == selectedKey -> colors.foreground
                                        else -> colors.textSecondary
                                    },
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp,
                                    fontWeight = if (item.key == selectedKey) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                            if (item.active) {
                                Spacer(Modifier.height(2.dp))
                                Surface(
                                    modifier = Modifier.size(4.dp),
                                    shape = CircleShape,
                                    color = if (item.key == selectedKey) colors.accent else colors.textMuted
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}
