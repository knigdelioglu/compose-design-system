package com.knigdelioglu.arc.compose.datavis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import kotlin.math.ceil
import kotlin.math.floor

data class ArcBrushPoint(val label: String, val value: Float)

object ArcBrushChartRules {
    /** Maps normalized endpoints to inclusive data indices. Empty data has the empty range 0..-1. */
    fun visibleIndices(selection: ClosedFloatingPointRange<Float>, size: Int): IntRange {
        if (size <= 0) return 0..-1
        val normalized = normalize(selection)
        val start = normalized.start
        val end = normalized.endInclusive
        val lastIndex = size - 1
        val first = floor(start * lastIndex).toInt().coerceIn(0, lastIndex)
        val last = ceil(end * lastIndex).toInt().coerceIn(first, lastIndex)
        return first..last
    }

    fun normalize(selection: ClosedFloatingPointRange<Float>): ClosedFloatingPointRange<Float> {
        val start = if (selection.start.isNaN()) 0f else selection.start.coerceIn(0f, 1f)
        val end = if (selection.endInclusive.isNaN()) start else selection.endInclusive.coerceIn(start, 1f)
        return start..end
    }
}

/**
 * A time-series chart with a draggable overview range. The Material range slider supplies
 * keyboard adjustment and adjustable TalkBack semantics alongside the touch brush interaction.
 */
@Composable
fun ArcBrushChart(
    data: List<ArcBrushPoint>,
    selection: ClosedFloatingPointRange<Float>,
    onSelectionChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Chart range",
    onSelectionChangeFinished: (() -> Unit)? = null
) {
    val colors = ArcTheme.colors
    val safeData = data.filter { it.value.isFinite() }
    val normalized = ArcBrushChartRules.normalize(selection)
    val visible = ArcBrushChartRules.visibleIndices(normalized, safeData.size)
    val selectedData = if (visible.isEmpty()) emptyList() else safeData.subList(visible.first, visible.last + 1)
    val description = when {
        safeData.isEmpty() -> "$label, no data"
        selectedData.isEmpty() -> "$label, no selected data"
        else -> "$label, ${selectedData.size} points selected, ${selectedData.first().label} to ${selectedData.last().label}"
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ArcTheme.spacing.space2)) {
        Text(label, color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        if (safeData.isEmpty()) {
            Text("No data", color = colors.textMuted, fontSize = 13.sp)
        } else {
            Canvas(
                modifier = Modifier.fillMaxWidth().height(148.dp)
                    .semantics { contentDescription = description }
            ) { drawSeries(selectedData, colors.accent, colors.borderSubtle, 3.dp.toPx()) }
            Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(selectedData.firstOrNull()?.label.orEmpty(), color = colors.textMuted, fontSize = 11.sp)
                Text(selectedData.lastOrNull()?.label.orEmpty(), color = colors.textMuted, fontSize = 11.sp)
            }
            Canvas(Modifier.fillMaxWidth().height(38.dp)) {
                drawSeries(safeData, colors.accent.copy(alpha = 0.68f), colors.borderSubtle, 2.dp.toPx())
                val left = normalized.start * size.width
                val right = normalized.endInclusive * size.width
                drawRect(
                    color = colors.accent.copy(alpha = 0.09f),
                    topLeft = Offset(left, 0f),
                    size = androidx.compose.ui.geometry.Size((right - left).coerceAtLeast(2.dp.toPx()), size.height)
                )
            }
            RangeSlider(
                value = normalized,
                onValueChange = { onSelectionChange(ArcBrushChartRules.normalize(it)) },
                onValueChangeFinished = { onSelectionChangeFinished?.invoke() },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "$label selection" }
            )
            Text(description, color = colors.textSecondary, fontSize = 12.sp)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeries(
    points: List<ArcBrushPoint>,
    lineColor: androidx.compose.ui.graphics.Color,
    baselineColor: androidx.compose.ui.graphics.Color,
    strokeWidth: Float
) {
    if (points.isEmpty()) return
    val padding = strokeWidth
    val plotWidth = (size.width - padding * 2).coerceAtLeast(1f)
    val plotHeight = (size.height - padding * 2).coerceAtLeast(1f)
    val min = points.minOf { it.value }
    val max = points.maxOf { it.value }
    val range = (max - min).takeIf { it > 0f } ?: 1f
    val path = Path()
    points.forEachIndexed { index, point ->
        val x = if (points.size == 1) size.width / 2f else padding + plotWidth * index / (points.size - 1)
        val y = padding + plotHeight - ((point.value - min) / range) * plotHeight
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawLine(baselineColor, Offset(0f, size.height - padding), Offset(size.width, size.height - padding), 1.dp.toPx())
    if (points.size == 1) {
        val y = padding + plotHeight / 2f
        drawCircle(lineColor, radius = strokeWidth, center = Offset(size.width / 2f, y))
    } else {
        drawPath(path, lineColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}
