package com.knigdelioglu.arc.compose.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import kotlin.math.roundToInt

/**
 * Arc Standard Slider: controlled range slider with track and active fill.
 */
@Composable
fun ArcSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    onValueCommit: ((Float) -> Unit)? = null,
    enabled: Boolean = true
) {
    ArcElasticSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        onValueCommit = onValueCommit,
        enabled = enabled,
        elasticOvershoot = false
    )
}

/**
 * Arc Elastic Slider:
 * Touch-ergonomic slider featuring an elevated value bubble visible above the finger,
 * rubber-band limit resistance, haptic-ready detent points, and explicit commit semantics.
 */
@Composable
fun ArcElasticSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    step: Float? = null,
    onValueCommit: ((Float) -> Unit)? = null,
    enabled: Boolean = true,
    label: String? = null,
    formatValue: (Float) -> String = { it.roundToInt().toString() },
    elasticOvershoot: Boolean = true
) {
    val min = valueRange.start
    val max = valueRange.endInclusive
    val validRange = min.isFinite() && max.isFinite() && max > min
    val safeMin = min.takeIf(Float::isFinite) ?: 0f
    val safeMax = max.takeIf { it.isFinite() && it > safeMin } ?: (safeMin + 1f)
    val safeStep = step?.takeIf { it.isFinite() && it > 0f }
    val span = safeMax - safeMin
    val safeValue = normalizeArcSliderValue(value, safeMin, safeMax, safeStep)

    var isDragging by remember { mutableStateOf(false) }
    val sliderFocusRequester = remember { FocusRequester() }
    var currentDragValue by remember(value, safeMin, safeMax, safeStep) { mutableFloatStateOf(safeValue) }
    var elasticOffsetRatio by remember { mutableFloatStateOf(0f) }
    val displayValue = if (isDragging) currentDragValue else safeValue
    val fraction = ((displayValue - safeMin) / span).coerceIn(0f, 1f)

    val motion = ArcMotion.current
    val thumbElevation by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 2.dp,
        animationSpec = motion.fastTween(),
        label = "arcSliderThumbElev"
    )
    val thumbSize by animateDpAsState(
        targetValue = if (isDragging) 30.dp else 24.dp,
        animationSpec = motion.fastTween(),
        label = "arcSliderThumbSize"
    )
    val colors = ArcTheme.colors
    val density = LocalDensity.current
    val interactionEnabled = enabled && validRange

    fun setFromPosition(x: Float, start: Boolean = false, usableWidthPx: Float, thumbRadiusPx: Float) {
        val previous = currentDragValue
        val rawRatio = (x - thumbRadiusPx) / usableWidthPx
        val boundedRatio = rawRatio.coerceIn(0f, 1f)
        elasticOffsetRatio = if (elasticOvershoot && rawRatio !in 0f..1f) {
            val beyond = if (rawRatio < 0f) rawRatio else rawRatio - 1f
            (beyond / (1f + kotlin.math.abs(beyond) * 5f)).coerceIn(-0.12f, 0.12f)
        } else 0f
        val next = normalizeArcSliderValue(safeMin + boundedRatio * span, safeMin, safeMax, safeStep)
        currentDragValue = next
        if (start || next != previous) onValueChange(next)
    }

    fun stepBy(delta: Float) {
        if (!interactionEnabled) return
        val interval = safeStep ?: (span / 100f)
        val next = normalizeArcSliderValue(displayValue + interval * delta, safeMin, safeMax, safeStep)
        if (next != displayValue) {
            currentDragValue = next
            onValueChange(next)
        }
        onValueCommit?.invoke(next)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary, modifier = Modifier.weight(1f))
                Text(formatValue(displayValue), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .focusRequester(sliderFocusRequester)
                .focusable(enabled = interactionEnabled)
                .semantics {
                    contentDescription = label ?: "Değer"
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = displayValue,
                        range = safeMin..safeMax,
                        steps = safeStep?.let { ((span / it).toInt() - 1).coerceAtLeast(0) } ?: 0
                    )
                    if (!interactionEnabled) disabled()
                    setProgress { requested ->
                        if (!interactionEnabled || !requested.isFinite()) false
                        else {
                            val next = normalizeArcSliderValue(requested, safeMin, safeMax, safeStep)
                            currentDragValue = next
                            onValueChange(next)
                            onValueCommit?.invoke(next)
                            true
                        }
                    }
                }
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || !interactionEnabled) return@onKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft, Key.DirectionDown -> { stepBy(-1f); true }
                        Key.DirectionRight, Key.DirectionUp -> { stepBy(1f); true }
                        Key.MoveHome -> { currentDragValue = safeMin; onValueChange(safeMin); onValueCommit?.invoke(safeMin); true }
                        Key.MoveEnd -> { currentDragValue = safeMax; onValueChange(safeMax); onValueCommit?.invoke(safeMax); true }
                        else -> false
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val thumbRadiusPx = with(density) { 12.dp.toPx() }
            val trackWidthPx = with(density) { maxWidth.toPx() }
            val usableWidthPx = (trackWidthPx - thumbRadiusPx * 2).coerceAtLeast(1f)
            val thumbCenterX = thumbRadiusPx + usableWidthPx * fraction +
                (if (isDragging) elasticOffsetRatio * usableWidthPx else 0f)

            if (isDragging) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (thumbCenterX - with(density) { 24.dp.toPx() }).roundToInt(),
                                y = -with(density) { 42.dp.toPx() }.roundToInt()
                            )
                        }
                        .shadow(4.dp, ArcTheme.shapes.pill)
                        .background(colors.accent, ArcTheme.shapes.pill)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(formatValue(displayValue), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.accentForeground)
                }
            }

            Box(Modifier.fillMaxWidth().height(8.dp).clip(ArcTheme.shapes.pill).background(colors.controlTrack))
            Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(ArcTheme.shapes.pill).background(colors.controlOn))
            Surface(
                modifier = Modifier
                    .offset { IntOffset((usableWidthPx * fraction + (if (isDragging) elasticOffsetRatio * usableWidthPx else 0f)).roundToInt(), 0) }
                    .size(thumbSize)
                    .shadow(thumbElevation, CircleShape),
                shape = CircleShape,
                color = colors.controlThumb,
                border = BorderStroke(1.dp, colors.borderStrong.copy(alpha = 0.4f))
            ) {}

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .pointerInput(interactionEnabled, safeMin, safeMax, safeStep, usableWidthPx, elasticOvershoot) {
                        if (!interactionEnabled) return@pointerInput
                        try {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                sliderFocusRequester.requestFocus()
                                isDragging = true
                                setFromPosition(down.position.x, start = true, usableWidthPx = usableWidthPx, thumbRadiusPx = thumbRadiusPx)
                                var released = false
                                while (!released) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    setFromPosition(change.position.x, usableWidthPx = usableWidthPx, thumbRadiusPx = thumbRadiusPx)
                                    change.consume()
                                    released = !change.pressed
                                }
                                isDragging = false
                                elasticOffsetRatio = 0f
                                onValueCommit?.invoke(currentDragValue)
                            }
                        } finally {
                            isDragging = false
                            elasticOffsetRatio = 0f
                        }
                    }
            )
        }
    }
}
