package com.knigdelioglu.arc.compose.advanced

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import kotlin.math.max

/**
 * A translucent, layered tab surface with a spring-driven selection capsule.
 * Uses tonal layers and borders instead of platform blur so contrast is predictable on all Android versions.
 */
@Composable
fun ArcGlassTabBar(
    tabs: List<ArcAdvancedItem>,
    selectedKey: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ArcTheme.colors
    val scrollState = rememberScrollState()
    val centers = remember { mutableStateMapOf<String, Float>() }
    val rootLeft = remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val minCellWidth = 76.dp
        val visibleCapacity = max(1, (maxWidth / minCellWidth).toInt())
        val cellWidth = if (tabs.size <= visibleCapacity) {
            if (tabs.isEmpty()) maxWidth else maxWidth / tabs.size
        } else minCellWidth
        val selectedIndex = tabs.indexOfFirst { it.key == selectedKey }
        val slotPx = with(density) { cellWidth.toPx() }
        val viewportPx = with(density) { maxWidth.toPx() }

        LaunchedEffect(selectedIndex, tabs.size, cellWidth, viewportPx) {
            if (selectedIndex >= 0 && tabs.size > visibleCapacity) {
                val maxScroll = with(density) { (cellWidth * tabs.size - maxWidth).coerceAtLeast(0.dp).roundToPx() }
                val target = (selectedIndex * slotPx + slotPx / 2f - viewportPx / 2f)
                    .toInt()
                    .coerceIn(0, maxScroll)
                scrollState.animateScrollTo(target)
            }
        }

        val selectedCenter = selectedKey?.let { centers[it] } ?: rootLeft.floatValue + slotPx / 2f
        val indicatorLeft by animateDpAsState(
            targetValue = with(density) { (selectedCenter - rootLeft.floatValue - slotPx / 2f + 6.dp.toPx()).toDp() },
            animationSpec = ArcTheme.motion.springGentle(),
            label = "glass-tab-indicator-offset"
        )
        val indicatorWidth by animateDpAsState(
            targetValue = (cellWidth - 12.dp).coerceAtLeast(48.dp),
            animationSpec = ArcTheme.motion.springGentle(),
            label = "glass-tab-indicator-width"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .onGloballyPositioned { rootLeft.floatValue = it.positionInRoot().x },
            shape = ArcTheme.shapes.panel,
            color = colors.surface.copy(alpha = if (colors.isDark) 0.92f else 0.86f),
            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.8f)),
            shadowElevation = 8.dp
        ) {
            Box(
                Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.surface.copy(alpha = if (colors.isDark) 0.54f else 0.66f),
                                colors.surfaceMuted.copy(alpha = if (colors.isDark) 0.44f else 0.5f)
                            )
                        )
                    )
            ) {
                if (selectedIndex >= 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = indicatorLeft)
                            .width(indicatorWidth)
                            .height(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surfaceRaised.copy(alpha = 0.86f))
                            .border(1.dp, colors.border.copy(alpha = 0.62f), RoundedCornerShape(18.dp))
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .horizontalScroll(scrollState)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val selected = tab.key == selectedKey
                        val focused = remember(tab.key) { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(64.dp)
                                .onGloballyPositioned { coordinates ->
                                    val center = coordinates.positionInRoot().x + coordinates.size.width / 2f
                                    val prior = centers[tab.key]
                                    if (prior == null || kotlin.math.abs(prior - center) > 0.75f) centers[tab.key] = center
                                }
                                .onFocusChanged { focused.value = it.isFocused }
                                .border(1.dp, if (focused.value) colors.accentStrong else Color.Transparent, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .selectable(
                                    selected = selected,
                                    enabled = tab.enabled,
                                    role = Role.Tab,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onTabSelected(tab.key) }
                                )
                                .semantics {
                                    val badge = tab.badgeCount?.takeIf { it > 0 }
                                    contentDescription = if (badge == null) tab.contentDescription else "${tab.contentDescription}, $badge notifications"
                                }
                                .padding(horizontal = 4.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                tab.icon?.let {
                                    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) { it() }
                                }
                                tab.badgeCount?.takeIf { it > 0 }?.let { count ->
                                    Surface(
                                        modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
                                        shape = ArcTheme.shapes.pill,
                                        color = if (selected) colors.accent else colors.surfaceMuted
                                    ) {
                                        Text(
                                            text = if (count > 99) "99+" else count.toString(),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            color = if (selected) colors.accentForeground else colors.textSecondary,
                                            fontSize = 9.sp,
                                            lineHeight = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            if (!tab.label.isNullOrBlank()) Text(
                                text = tab.label,
                                color = when {
                                    !tab.enabled -> colors.textMuted
                                    selected -> colors.foreground
                                    else -> colors.textSecondary
                                },
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
