package com.knigdelioglu.arc.compose.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.controls.normalizePanelWeights
import com.knigdelioglu.arc.compose.controls.resizeAdjacentPanelWeights
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/**
 * Resizable two or three pane layout. Pane weights always sum to one, and moving a divider
 * redistributes only the two panes touching it. Narrow windows switch to vertical panes.
 */
@Composable
fun ArcResizablePanels(
    modifier: Modifier = Modifier,
    initialWeights: List<Float> = listOf(0.30f, 0.40f, 0.30f),
    minWeights: List<Float> = listOf(0.18f, 0.25f, 0.20f),
    firstPane: @Composable () -> Unit,
    secondPane: @Composable () -> Unit,
    thirdPane: (@Composable () -> Unit)? = null,
    maxWeights: List<Float> = listOf(0.82f, 0.82f, 0.82f),
    collapsiblePanes: Set<Int> = emptySet(),
    onWeightsChanged: ((List<Float>) -> Unit)? = null
) {
    val panes: List<@Composable () -> Unit> = if (thirdPane == null) listOf(firstPane, secondPane)
    else listOf(firstPane, secondPane, thirdPane)
    val paneCount = panes.size
    val minimums = (0 until paneCount).map { index ->
        val requested = minWeights.getOrElse(index) { 0.18f }.coerceAtLeast(0f)
        if (index in collapsiblePanes) minOf(requested, 0.04f) else requested
    }
    val maximums = (0 until paneCount).map { index ->
        maxWeights.getOrElse(index) { 0.82f }.coerceAtLeast(minimums[index])
    }
    val resetWeights = remember(paneCount, initialWeights, minimums, maximums) {
        val defaults = if (paneCount == 2 && initialWeights.size != 2) listOf(0.4f, 0.6f) else initialWeights
        normalizePanelWeights(defaults, paneCount, minimums, maximums)
    }
    var weights by rememberSaveable(
        paneCount,
        resetWeights,
        stateSaver = listSaver(
            save = { it.toList() },
            restore = { it.toList() }
        )
    ) { mutableStateOf(resetWeights) }
    var savedWidths by remember(paneCount) { mutableStateOf(emptyMap<Int, Float>()) }
    LaunchedEffect(weights) { onWeightsChanged?.invoke(weights) }

    fun resizeDivider(index: Int, deltaRatio: Float) {
        weights = resizeAdjacentPanelWeights(weights, index, deltaRatio, minimums, maximums)
    }

    fun reset() {
        weights = resetWeights
        savedWidths = emptyMap()
    }

    fun toggleCollapse(index: Int) {
        val trailing = index + 1
        if (trailing !in collapsiblePanes) return
        val current = weights[trailing]
        val collapsed = current <= 0.05f
        val pairTotal = weights[index] + current
        val targetTrailing = if (collapsed) {
            savedWidths[index]?.coerceIn(minimums[trailing], pairTotal - minimums[index])
                ?: resetWeights[trailing].coerceIn(minimums[trailing], pairTotal - minimums[index])
        } else {
            savedWidths = savedWidths + (index to current)
            0.04f.coerceIn(minimums[trailing], pairTotal - minimums[index])
        }
        val targetLeading = pairTotal - targetTrailing
        weights = weights.toMutableList().also {
            it[index] = targetLeading
            it[trailing] = targetTrailing
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val horizontal = maxWidth >= 600.dp
        val density = LocalDensity.current
        val dividerThickness = 14.dp
        val dividerCount = paneCount - 1
        val axisSizePx = with(density) { (if (horizontal) maxWidth else maxHeight).toPx() }
        val paneAreaPx = (axisSizePx - with(density) { dividerThickness.toPx() } * dividerCount).coerceAtLeast(1f)

        if (horizontal) {
            Row(Modifier.fillMaxSize()) {
                panes.forEachIndexed { index, pane ->
                    Box(Modifier.weight(weights[index]).fillMaxHeight()) { pane() }
                    if (index < paneCount - 1) {
                        ArcPanelDivider(
                            horizontal = true,
                            value = weights[index],
                            valueRange = minimums[index]..maximums[index],
                            canCollapse = index + 1 in collapsiblePanes,
                            onDelta = { resizeDivider(index, it / paneAreaPx) },
                            onAdjust = { resizeDivider(index, it) },
                            onReset = ::reset,
                            onToggleCollapse = { toggleCollapse(index) }
                        )
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                panes.forEachIndexed { index, pane ->
                    Box(Modifier.weight(weights[index]).fillMaxWidth()) { pane() }
                    if (index < paneCount - 1) {
                        ArcPanelDivider(
                            horizontal = false,
                            value = weights[index],
                            valueRange = minimums[index]..maximums[index],
                            canCollapse = index + 1 in collapsiblePanes,
                            onDelta = { resizeDivider(index, it / paneAreaPx) },
                            onAdjust = { resizeDivider(index, it) },
                            onReset = ::reset,
                            onToggleCollapse = { toggleCollapse(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArcPanelDivider(
    horizontal: Boolean,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    canCollapse: Boolean,
    onDelta: (Float) -> Unit,
    onAdjust: (Float) -> Unit,
    onReset: () -> Unit,
    onToggleCollapse: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val colors = ArcTheme.colors
    val dividerModifier = if (horizontal) Modifier.width(14.dp).fillMaxHeight() else Modifier.height(14.dp).fillMaxWidth()

    Box(
        modifier = dividerModifier
            .semantics {
                contentDescription = "Panel boyutlandırma"
                stateDescription = "Bölme boyutu"
                progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange, steps = 0)
                setProgress { requested ->
                    if (!requested.isFinite()) false else {
                        onAdjust(requested - value)
                        true
                    }
                }
            }
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    if (horizontal) Key.DirectionLeft else Key.DirectionUp -> { onAdjust(-0.02f); true }
                    if (horizontal) Key.DirectionRight else Key.DirectionDown -> { onAdjust(0.02f); true }
                    Key.MoveHome -> { onReset(); true }
                    Key.Spacebar, Key.Enter -> if (canCollapse) { onToggleCollapse(); true } else false
                    else -> false
                }
            }
            .pointerInput(horizontal) {
                detectTapGestures(
                    onTap = { focusRequester.requestFocus() },
                    onDoubleTap = {
                        focusRequester.requestFocus()
                        onReset()
                    }
                )
            }
            .pointerInput(horizontal) {
                detectDragGestures(
                    onDragStart = {
                        focusRequester.requestFocus()
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDelta(if (horizontal) dragAmount.x else dragAmount.y)
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = (if (horizontal) Modifier.width(1.5.dp).fillMaxHeight()
            else Modifier.height(1.5.dp).fillMaxWidth()).background(if (isDragging) colors.accent else colors.border)
        )
        Box(
            modifier = if (horizontal) Modifier.width(14.dp).fillMaxHeight()
            else Modifier.height(14.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .then(if (horizontal) Modifier.size(width = 6.dp, height = 36.dp) else Modifier.size(width = 36.dp, height = 6.dp))
                    .shadow(if (isDragging) 4.dp else 1.dp, CircleShape),
                shape = CircleShape,
                color = if (isDragging) colors.accent else colors.surfaceRaised,
                border = BorderStroke(1.dp, colors.borderStrong)
            ) {}
        }
    }
}
