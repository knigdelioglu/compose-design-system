package com.knigdelioglu.arc.compose.datavis

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.display.ArcBadge
import com.knigdelioglu.arc.compose.display.ArcBadgeVariant
import com.knigdelioglu.arc.compose.display.ArcCard
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/**
 * Arc Metric Card: displays single metric with label, value, trend, or subtitle.
 */
@Composable
fun ArcMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trendPositive: Boolean? = null,
    badgeText: String? = null,
    badgeVariant: ArcBadgeVariant = ArcBadgeVariant.Default
) {
    val colors = ArcTheme.colors
    ArcCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
                if (badgeText != null) {
                    ArcBadge(text = badgeText, variant = badgeVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground,
                letterSpacing = (-0.5).sp
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 12.sp, color = colors.textMuted)
            }
        }
    }
}

/**
 * Arc Stats Band: horizontal row of caller-defined labels, values, details, and dividers.
 */
data class ArcStatItem(
    val label: String,
    val value: String,
    val detail: String? = null
)

@Composable
fun ArcStatsBand(
    stats: List<ArcStatItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArcTheme.shapes.panel,
        color = ArcTheme.colors.surfaceRaised,
        border = BorderStroke(1.dp, ArcTheme.colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stats.forEachIndexed { index, stat ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stat.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ArcTheme.colors.textSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text(stat.value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ArcTheme.colors.foreground)
                    if (stat.detail != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(stat.detail, fontSize = 11.sp, color = ArcTheme.colors.textMuted)
                    }
                }
                if (index < stats.lastIndex) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(ArcTheme.colors.borderSubtle)
                    )
                }
            }
        }
    }
}

/**
 * Arc Animated Counter: smooth integer counting animation on value change.
 */
@Composable
fun ArcAnimatedCounter(
    targetValue: Int = 0,
    modifier: Modifier = Modifier,
    suffix: String = "",
    count: Int = targetValue,
    fontSize: TextUnit = 24.sp,
    color: Color = ArcTheme.colors.foreground
) {
    val motion = ArcMotion.current
    val effectiveTarget = if (count != 0) count else targetValue
    val animatedValue by animateIntAsState(
        targetValue = effectiveTarget,
        animationSpec = motion.standardTween(),
        label = "animatedCounter"
    )

    Text(
        text = "$animatedValue$suffix",
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/**
 * Arc Slot Text: number reel / slot animation effect.
 */
@Composable
fun ArcSlotText(
    value: String,
    modifier: Modifier = Modifier
) {
    val motion = ArcMotion.current
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            if (motion.reducedMotion) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                fadeIn(motion.fastTween()) togetherWith fadeOut(motion.fastTween())
            }
        },
        label = "arcSlotText",
        modifier = modifier
    ) { target ->
        Text(
            text = target,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ArcTheme.colors.foreground
        )
    }
}

/**
 * Arc Text Reveal: smooth initial text reveal for introductions.
 */
@Composable
fun ArcTextReveal(
    text: String,
    modifier: Modifier = Modifier
) {
    val motion = ArcMotion.current
    val alpha = remember { Animatable(if (motion.reducedMotion) 1f else 0f) }
    LaunchedEffect(text, motion.reducedMotion) {
        if (motion.reducedMotion) {
            alpha.snapTo(1f)
        } else {
            alpha.snapTo(0f)
            alpha.animateTo(1f, animationSpec = motion.consideredTween())
        }
    }
    Text(
        text = text,
        color = ArcTheme.colors.foreground.copy(alpha = alpha.value),
        fontSize = 15.sp,
        modifier = modifier
    )
}

/**
 * Arc Text Morph: morphing label (e.g. "Kaydediliyor..." -> "Kaydedildi").
 */
@Composable
fun ArcTextMorph(
    label: String,
    modifier: Modifier = Modifier
) {
    val motion = ArcMotion.current
    AnimatedContent(
        targetState = label,
        transitionSpec = {
            if (motion.reducedMotion) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                fadeIn(motion.fastTween()) togetherWith fadeOut(motion.fastTween())
            }
        },
        label = "arcTextMorph",
        modifier = modifier
    ) { currentLabel ->
        Text(
            text = currentLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArcTheme.colors.textSecondary
        )
    }
}

/**
 * Arc Text Shimmer: gentle shimmer pulse for ongoing asynchronous work.
 */
@Composable
fun ArcTextShimmer(
    text: String,
    modifier: Modifier = Modifier
) {
    val motion = ArcMotion.current
    val alpha = if (motion.reducedMotion) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val animatedAlpha by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(motion.durationConsidered.coerceAtLeast(1)),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmerAlpha"
        )
        animatedAlpha
    }

    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = ArcTheme.colors.accent.copy(alpha = alpha),
        modifier = modifier
    )
}

/**
 * Arc Chip Group: horizontal filter chips for caller-defined categories and status values.
 */
@Composable
fun <T> ArcChipGroup(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String = { it.toString() }
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            val isSelected = item == selectedItem
            val colors = ArcTheme.colors
            Surface(
                modifier = Modifier
                    .clip(ArcTheme.shapes.pill)
                    .clickable { onItemSelected(item) },
                shape = ArcTheme.shapes.pill,
                color = if (isSelected) colors.accent else colors.surfaceRaised,
                border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle)
            ) {
                Text(
                    text = labelProvider(item),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) colors.accentForeground else colors.foreground,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

/**
 * Arc Filter Toolbar: combined search, status chips, and sorting controls.
 */
@Composable
fun ArcFilterToolbar(
    modifier: Modifier = Modifier,
    searchSlot: (@Composable () -> Unit)? = null,
    filterChipsSlot: (@Composable () -> Unit)? = null,
    actionsSlot: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (searchSlot != null) {
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    searchSlot()
                }
            }
            if (actionsSlot != null) {
                Spacer(Modifier.width(8.dp))
                actionsSlot()
            }
        }
        if (filterChipsSlot != null) {
            Spacer(Modifier.height(8.dp))
            filterChipsSlot()
        }
    }
}

/**
 * Arc Sortable Data Table:
 * Useful for browsing and reviewing large result sets.
 */
data class ArcTableColumn<T>(
    val title: String,
    val weight: Float = 1f,
    val comparator: Comparator<T>? = null,
    val content: @Composable (T) -> Unit
)

@Composable
fun <T> ArcSortableDataTable(
    items: List<T>,
    columns: List<ArcTableColumn<T>>,
    modifier: Modifier = Modifier,
    onRowClick: ((T) -> Unit)? = null,
    pageSize: Int? = null,
    rowKey: ((T) -> Any)? = null,
    selectedItems: Set<T> = emptySet(),
    onSelectionChanged: ((Set<T>) -> Unit)? = null
) {
    // Keep the active sort attached to a column identity across caller-owned schema changes.
    var sortColumnTitle by remember { mutableStateOf<String?>(null) }
    var sortAscending by remember { mutableStateOf(true) }

    val sortColumnIndex = sortColumnTitle?.let { title ->
        columns.indexOfFirst { it.title == title }.takeIf { it >= 0 }
    }
    val sortedItems = remember(items, columns, sortColumnIndex, sortAscending) {
        val colIndex = sortColumnIndex
        if (colIndex != null) {
            val comp = columns.getOrNull(colIndex)?.comparator
            if (comp != null) {
                if (sortAscending) items.sortedWith(comp) else items.sortedWith(comp.reversed())
            } else items
        } else items
    }

    // Sort the complete result first, then paginate so global ordering stays consistent.
    var requestedPage by remember(items) { mutableIntStateOf(1) }
    val window = pageSortedRows(sortedItems, pageSize, requestedPage)
    val pageCount = window.pageCount
    val page = window.page
    val visibleItems = window.rows

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArcTheme.shapes.panel,
        color = ArcTheme.colors.surface,
        border = BorderStroke(1.dp, ArcTheme.colors.borderSubtle)
    ) {
        Column {
            // Header Row
            Surface(color = ArcTheme.colors.surfaceRaised) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onSelectionChanged != null) {
                        val allSelected = visibleItems.isNotEmpty() && visibleItems.all { it in selectedItems }
                        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterStart) {
                            com.knigdelioglu.arc.compose.controls.ArcCheckbox(
                                checked = allSelected,
                                modifier = Modifier.semantics { contentDescription = "Sayfadaki tüm satırları seç" },
                                enabled = visibleItems.isNotEmpty(),
                                onCheckedChange = { checked ->
                                    val pageRows = visibleItems.toSet()
                                    onSelectionChanged(if (checked) selectedItems + pageRows else selectedItems - pageRows)
                                }
                            )
                        }
                    }
                    columns.forEachIndexed { index, col ->
                        val columnWeight = col.weight.takeIf { it.isFinite() && it > 0f } ?: 1f
                        Row(
                            modifier = Modifier
                                .weight(columnWeight)
                                .then(
                                    if (col.comparator != null) {
                                        Modifier.clickable {
                                            if (sortColumnIndex == index) {
                                                sortAscending = !sortAscending
                                            } else {
                                                sortColumnTitle = col.title
                                                sortAscending = true
                                            }
                                        }.semantics {
                                            role = Role.Button
                                            stateDescription = when {
                                                sortColumnIndex != index -> "Sıralama yok"
                                                sortAscending -> "Artan sıralama"
                                                else -> "Azalan sıralama"
                                            }
                                        }
                                    } else Modifier
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = col.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcTheme.colors.foreground
                            )
                            if (sortColumnIndex == index) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = ArcTheme.colors.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = ArcTheme.colors.borderSubtle)

            // Items Rows
            LazyColumn(modifier = Modifier.fillMaxWidth().then(if (pageSize != null) Modifier.weight(1f) else Modifier)) {
                items(visibleItems, key = rowKey) { item ->
                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (onRowClick != null) Modifier.clickable { onRowClick(item) } else Modifier)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onSelectionChanged != null) {
                                val selected = item in selectedItems
                                Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterStart) {
                                    com.knigdelioglu.arc.compose.controls.ArcCheckbox(
                                        checked = selected,
                                        modifier = Modifier.semantics {
                                            contentDescription = "${rowKey?.invoke(item) ?: item} satırını seç"
                                        },
                                        onCheckedChange = { checked ->
                                            onSelectionChanged(if (checked) selectedItems + item else selectedItems - item)
                                        }
                                    )
                                }
                            }
                            columns.forEach { col ->
                                val columnWeight = col.weight.takeIf { it.isFinite() && it > 0f } ?: 1f
                                Box(modifier = Modifier.weight(columnWeight)) {
                                    col.content(item)
                                }
                            }
                        }
                    }
                    Divider(color = ArcTheme.colors.borderSubtle.copy(alpha = 0.5f))
                }
            }
            if (pageSize != null && pageSize > 0 && pageCount > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.knigdelioglu.arc.compose.navigation.ArcPagination(
                        currentPage = page,
                        totalPages = pageCount,
                        onPageChange = { requestedPage = it.coerceIn(1, pageCount) }
                    )
                }
            }
        }
    }
}
