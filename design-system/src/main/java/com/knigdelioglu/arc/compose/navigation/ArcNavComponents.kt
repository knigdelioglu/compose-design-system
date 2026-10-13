package com.knigdelioglu.arc.compose.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.knigdelioglu.arc.compose.controls.ArcActionButton
import com.knigdelioglu.arc.compose.controls.ArcButton
import com.knigdelioglu.arc.compose.controls.ArcButtonSize
import com.knigdelioglu.arc.compose.controls.ArcButtonVariant
import com.knigdelioglu.arc.compose.controls.ArcSearchField
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlinx.coroutines.launch

/**
 * Arc Page Header:
 * Clean, informative workspace top bar for a title, status badges, and top-level actions.
 */
@Composable
fun ArcPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    breadcrumb: (@Composable () -> Unit)? = null,
    statusBadge: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    val colors = ArcTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            if (breadcrumb != null) {
                breadcrumb()
                Spacer(Modifier.height(4.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.foreground
                    )
                    if (statusBadge != null) {
                        Spacer(Modifier.width(12.dp))
                        statusBadge()
                    }
                }
                if (actions != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        actions()
                    }
                }
            }
        }
    }
}

/**
 * Arc Breadcrumb: hierarchy navigation for caller-defined routes and locations.
 */
@Composable
fun ArcBreadcrumb(
    items: List<String>,
    onItemClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex
            Text(
                text = item,
                fontSize = 13.sp,
                color = if (isLast) ArcTheme.colors.foreground else ArcTheme.colors.textMuted,
                fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.then(
                    if (!isLast && onItemClick != null) Modifier.clickable { onItemClick(index) } else Modifier
                )
            )
            if (!isLast) {
                Text(
                    text = "›",
                    fontSize = 14.sp,
                    color = ArcTheme.colors.textMuted,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

/**
 * Arc Tabs: horizontal tab bar with sliding active indicator and count badges.
 */
@Composable
fun ArcTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badges: List<String?>? = null
) {
    val colors = ArcTheme.colors
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs) { index, tabTitle ->
            val isSelected = index == selectedTabIndex
            val badge = badges?.getOrNull(index)

            Surface(
                modifier = Modifier
                    .clip(ArcTheme.shapes.pill)
                    .clickable { onTabSelected(index) },
                shape = ArcTheme.shapes.pill,
                color = if (isSelected) colors.accent else colors.surfaceRaised,
                border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tabTitle,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) colors.accentForeground else colors.foreground
                    )
                    if (!badge.isNullOrEmpty()) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = ArcTheme.shapes.pill,
                            color = if (isSelected) colors.accentForeground.copy(alpha = 0.2f) else colors.surfaceMuted
                        ) {
                            Text(
                                text = badge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) colors.accentForeground else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Arc Drawer: slide-out panel for details, filters, and caller-provided editing content.
 */
@Composable
fun ArcDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isOpen) {
        Dialog(onDismissRequest = onClose) {
            Surface(
                modifier = modifier
                    .fillMaxHeight(0.92f)
                    .fillMaxWidth(0.85f),
                shape = ArcTheme.shapes.panel,
                color = ArcTheme.colors.surfaceRaised,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ArcTheme.colors.foreground)
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = ArcTheme.colors.textMuted)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Arc Bottom Sheet: modal bottom sheet for narrow screens or compact interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcBottomSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isOpen) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = ArcTheme.colors.surfaceRaised,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = modifier
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

/**
 * Arc Command Palette:
 * Keyboard shortcut accessible modal (Cmd/Ctrl+K) for searching application actions.
 */
data class ArcCommandAction(
    val id: String,
    val title: String,
    val category: String,
    val shortcut: String? = null,
    val onExecute: () -> Unit
)

@Composable
fun ArcCommandPalette(
    isOpen: Boolean,
    onClose: () -> Unit,
    actions: List<ArcCommandAction>,
    modifier: Modifier = Modifier
) = ArcCommandPaletteImpl(isOpen, onClose, actions, modifier, focusRestoreRequester = null)

/**
 * Command palette overload for hosts that need deterministic focus restoration after dismissal.
 * Attach [focusRestoreRequester] to the control that opened the palette.
 */
@Composable
fun ArcCommandPalette(
    isOpen: Boolean,
    onClose: () -> Unit,
    actions: List<ArcCommandAction>,
    focusRestoreRequester: FocusRequester,
    modifier: Modifier = Modifier
) = ArcCommandPaletteImpl(isOpen, onClose, actions, modifier, focusRestoreRequester)

@Composable
private fun ArcCommandPaletteImpl(
    isOpen: Boolean,
    onClose: () -> Unit,
    actions: List<ArcCommandAction>,
    modifier: Modifier,
    focusRestoreRequester: FocusRequester?
) {
    var shouldRestoreFocus by remember { mutableStateOf(false) }
    val closePalette = {
        shouldRestoreFocus = true
        onClose()
    }
    androidx.compose.runtime.LaunchedEffect(isOpen, shouldRestoreFocus) {
        if (!isOpen && shouldRestoreFocus) {
            androidx.compose.runtime.withFrameNanos { }
            focusRestoreRequester?.requestFocus()
            shouldRestoreFocus = false
        }
    }

    if (isOpen) {
        var query by remember { mutableStateOf("") }
        val filtered = remember(query, actions) {
            if (query.isBlank()) actions else actions.filter {
                it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
            }
        }
        // Actions can be assembled inline by callers and recreated on every recomposition.
        // Stable display keys keep arrow navigation from jumping back to the first result.
        val activeKeys = filtered.map { Triple(it.id, it.title, it.category) }
        var activeIndex by remember(query, activeKeys) { mutableIntStateOf(0) }
        val searchFocusRequester = remember { FocusRequester() }
        val listState = rememberLazyListState()
        val motion = com.knigdelioglu.arc.compose.foundation.ArcMotion.current

        androidx.compose.runtime.LaunchedEffect(activeIndex, filtered.size) {
            if (filtered.isNotEmpty()) {
                val index = activeIndex.coerceIn(filtered.indices)
                if (motion.reducedMotion) listState.scrollToItem(index) else listState.animateScrollToItem(index)
            }
        }
        val searchInputModifier = Modifier
            .focusRequester(searchFocusRequester)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionDown -> {
                        if (filtered.isNotEmpty()) activeIndex = (activeIndex + 1).coerceAtMost(filtered.lastIndex)
                        true
                    }
                    Key.DirectionUp -> {
                        if (filtered.isNotEmpty()) activeIndex = (activeIndex - 1).coerceAtLeast(0)
                        true
                    }
                    Key.Enter, Key.NumPadEnter -> {
                        filtered.getOrNull(activeIndex)?.let { action ->
                            action.onExecute()
                            closePalette()
                        }
                        true
                    }
                    Key.Escape -> { closePalette(); true }
                    else -> false
                }
            }

        Dialog(onDismissRequest = closePalette) {
            Surface(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 520.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.DirectionDown -> {
                                if (filtered.isNotEmpty()) activeIndex = (activeIndex + 1).coerceAtMost(filtered.lastIndex)
                                true
                            }
                            Key.DirectionUp -> {
                                if (filtered.isNotEmpty()) activeIndex = (activeIndex - 1).coerceAtLeast(0)
                                true
                            }
                            Key.Enter, Key.NumPadEnter -> {
                                filtered.getOrNull(activeIndex)?.let { action ->
                                    action.onExecute()
                                    closePalette()
                                }
                                true
                            }
                            Key.Escape -> { closePalette(); true }
                            else -> false
                        }
                    },
                shape = ArcTheme.shapes.panel,
                color = ArcTheme.colors.surfaceRaised,
                border = BorderStroke(1.dp, ArcTheme.colors.border),
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    androidx.compose.runtime.LaunchedEffect(searchFocusRequester) {
                        searchFocusRequester.requestFocus()
                    }
                    ArcSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        inputModifier = searchInputModifier,
                        placeholder = "Komut ara..."
                    )
                    Spacer(Modifier.height(14.dp))
                    androidx.compose.foundation.lazy.LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filtered.size) { index ->
                            val action = filtered[index]
                            val isActive = index == activeIndex
                            val rowColor by animateColorAsState(
                                targetValue = if (isActive) ArcTheme.colors.accentSubtle else Color.Transparent,
                                animationSpec = motion.fastTween(),
                                label = "arcCommandActive"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ArcTheme.shapes.control)
                                    .background(rowColor)
                                    .clickable {
                                        action.onExecute()
                                        closePalette()
                                    }
                                    .semantics { selected = isActive }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = action.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ArcTheme.colors.foreground
                                    )
                                    Text(
                                        text = action.category,
                                        fontSize = 12.sp,
                                        color = ArcTheme.colors.textMuted
                                    )
                                }
                                if (action.shortcut != null) {
                                    Surface(
                                        shape = ArcTheme.shapes.small,
                                        color = ArcTheme.colors.surfaceMuted,
                                        border = BorderStroke(1.dp, ArcTheme.colors.borderSubtle)
                                    ) {
                                        Text(
                                            text = action.shortcut,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcTheme.colors.textSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (filtered.isEmpty()) {
                            item {
                                Text(
                                    text = "Sonuç bulunamadı",
                                    color = ArcTheme.colors.textMuted,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Arc Scroll Area: container with styled scroll edges and subtle boundary indicators.
 */
@Composable
fun ArcScrollArea(
    modifier: Modifier = Modifier,
    state: ScrollState = rememberScrollState(),
    content: @Composable () -> Unit
) {
    // A real scroll area; callers supply a stable scroll state when useful.
    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(state)) { content() }
        if (state.canScrollForward) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth().height(2.dp)
                    .background(ArcTheme.colors.borderStrong)
            )
        }
    }
}

/**
 * Arc Pagination: page selector controls for large result sets.
 */
@Composable
fun ArcPagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ArcActionButton(
            onClick = { onPageChange((currentPage - 1).coerceAtLeast(1)) },
            size = ArcButtonSize.Sm,
            enabled = currentPage > 1
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki Sayfa", modifier = Modifier.size(16.dp))
        }

        Text(
            text = "$currentPage / $totalPages",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArcTheme.colors.textSecondary
        )

        ArcActionButton(
            onClick = { onPageChange((currentPage + 1).coerceAtMost(totalPages)) },
            size = ArcButtonSize.Sm,
            enabled = currentPage < totalPages
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki Sayfa", modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Arc Stepper: linear step indicator for a multi-stage workflow.
 */
@Composable
fun ArcStepper(
    steps: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
    onStepClick: ((Int) -> Unit)? = null
) {
    val colors = ArcTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, title ->
            val isCompleted = index < currentStepIndex
            val isCurrent = index == currentStepIndex

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .then(if (onStepClick != null) Modifier.clickable { onStepClick(index) } else Modifier),
                    shape = CircleShape,
                    color = when {
                        isCompleted -> colors.success
                        isCurrent -> colors.accent
                        else -> colors.surfaceMuted
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) colors.accentForeground else colors.textMuted
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrent || isCompleted) colors.foreground else colors.textMuted
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .height(2.dp)
                        .background(if (index < currentStepIndex) colors.success else colors.borderSubtle)
                )
            }
        }
    }
}

/**
 * Arc Carousel: horizontal paging container for reviewing scanned roster photos or reports.
 */
@Composable
fun <T> ArcCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val safeIndex = currentIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    LaunchedEffect(items.size) {
        // Permanently reconcile the saved index after the collection shrinks.
        if (currentIndex != safeIndex) currentIndex = safeIndex
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            items.getOrNull(safeIndex)?.let { itemContent(it) }
        }
        if (items.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { currentIndex = (safeIndex - 1).coerceAtLeast(0) },
                    enabled = safeIndex > 0
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki") }
                Text("${safeIndex + 1} / ${items.size}", fontSize = 13.sp, color = ArcTheme.colors.textSecondary)
                IconButton(
                    onClick = { currentIndex = (safeIndex + 1).coerceAtMost(items.lastIndex) },
                    enabled = safeIndex < items.lastIndex
                ) { Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki") }
            }
        }
    }
}

/**
 * Arc Card Stack: stacked-card interaction for focused, one-at-a-time review.
 */
@Composable
fun <T> ArcCardStack(
    items: List<T>,
    modifier: Modifier = Modifier,
    onDismissTop: ((T) -> Unit)? = null,
    cardContent: @Composable (T) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val visibleItems = items.take(3)
    val motion = ArcMotion.current

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        visibleItems.indices.reversed().forEach { stackIndex ->
            val item = visibleItems[stackIndex]
            val isTopCard = stackIndex == 0
            var dismissing by remember(item) { mutableStateOf(false) }
            val scale = 1f - (stackIndex * 0.04f)
            val offsetY = (stackIndex * 10).dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY)
                    .scale(scale)
                    .then(
                        if (isTopCard) {
                            Modifier
                                .offset { IntOffset(offsetX.roundToInt(), 0) }
                                .semantics {
                                    contentDescription = "Üst kart: $item"
                                    if (onDismissTop != null) {
                                        customActions = listOf(
                                            CustomAccessibilityAction("Kartı kaldır") {
                                                if (!dismissing) onDismissTop(item)
                                                true
                                            }
                                        )
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            val threshold = maxOf(size.width * 0.35f, 80f)
                                            if (onDismissTop != null && !dismissing && abs(offsetX) >= threshold) {
                                                dismissing = true
                                                val target = (if (offsetX < 0f) -1f else 1f) * (size.width + abs(offsetX))
                                                scope.launch {
                                                    animate(
                                                        initialValue = offsetX,
                                                        targetValue = target,
                                                    animationSpec = motion.springResponsive()
                                                    ) { value, _ -> offsetX = value }
                                                    onDismissTop(item)
                                                    offsetX = 0f
                                                    dismissing = false
                                                }
                                            } else {
                                                scope.launch {
                                                    animate(
                                                        initialValue = offsetX,
                                                        targetValue = 0f,
                                                    animationSpec = motion.springResponsive()
                                                    ) { value, _ -> offsetX = value }
                                                }
                                            }
                                        },
                                        onDragCancel = { offsetX = 0f },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            offsetX += dragAmount
                                        }
                                    )
                                }
                        } else Modifier
                    )
            ) {
                cardContent(item)
            }
        }
    }
}
