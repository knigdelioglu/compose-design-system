package com.knigdelioglu.arc.compose.datavis

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.display.ArcCard
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/**
 * Arc Bar Chart: distribution of scores across grade intervals.
 */
data class ArcBarData(
    val label: String,
    val value: Float,
    val color: Color? = null
)

@Composable
fun ArcBarChart(
    data: List<ArcBarData>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 140.dp,
    title: String = "Dağılım"
) {
    val safeData = data.map { it.copy(value = it.value.takeIf(Float::isFinite)?.coerceAtLeast(0f) ?: 0f) }
    val maxValue = (safeData.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    val colors = ArcTheme.colors

    ArcCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(barHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                safeData.forEachIndexed { index, item ->
                    val fraction = (item.value / maxValue).coerceIn(0f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.width(52.dp).fillMaxHeight()
                    ) {
                        Text(item.value.toInt().toString(), fontSize = 11.sp, color = colors.textMuted)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(item.color ?: colors.dataVisualizationPalette[index % colors.dataVisualizationPalette.size])
                                .semantics { contentDescription = "${item.label}: ${item.value}" }
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(item.label, fontSize = 11.sp, color = colors.textSecondary)
                    }
                }
            }
        }
    }
}

/**
 * Arc Donut Chart: completion distribution (Tamamlanan / Kısmi / Başlanmadı).
 */
data class ArcDonutSegment(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun ArcDonutChart(
    segments: List<ArcDonutSegment>,
    modifier: Modifier = Modifier,
    centerTitle: String = "Toplam",
    centerSubtitle: String = ""
) {
    val total = (segments.sumOf { it.value.toDouble() }).toFloat().coerceAtLeast(1f)
    val colors = ArcTheme.colors

    ArcCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val strokeWidth = 22.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val arcSize = Size(diameter, diameter)

                    segments.forEach { seg ->
                        val sweep = (seg.value / total) * 360f
                        drawArc(
                            color = seg.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(centerTitle, fontSize = 11.sp, color = colors.textMuted)
                    Text(centerSubtitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                segments.forEach { seg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = seg.color) {}
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${seg.label}: ${seg.value.toInt()}",
                            fontSize = 13.sp,
                            color = colors.foreground
                        )
                    }
                }
            }
        }
    }
}

/**
 * Arc Gauge: arc progress meter showing completion percentage.
 */
@Composable
fun ArcGauge(
    percentage: Float,
    label: String = "Tamamlanma Oranı",
    modifier: Modifier = Modifier
) {
    val colors = ArcTheme.colors
    val safePercentage = percentage.takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f
    ArcCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.size(120.dp, 70.dp), contentAlignment = Alignment.BottomCenter) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Track
                    drawArc(
                        color = colors.surfaceMuted,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Progress
                    drawArc(
                        color = colors.accent,
                        startAngle = 180f,
                        sweepAngle = 180f * safePercentage,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(safePercentage * 100).toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.foreground
                )
            }
        }
    }
}

/**
 * Arc Sparkline: lightweight historical score curve.
 */
@Composable
fun ArcSparkline(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = ArcTheme.colors.accent
) {
    val safePoints = points.filter(Float::isFinite)
    if (safePoints.size < 2) return

    val min = safePoints.minOrNull() ?: 0f
    val max = (safePoints.maxOrNull() ?: 1f).coerceAtLeast(min + 1f)

    Canvas(modifier = modifier.height(36.dp).width(100.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / (safePoints.size - 1)

        val path = Path()
        safePoints.forEachIndexed { i, p ->
            val x = i * stepX
            val y = height - (((p - min) / (max - min)) * height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * Arc Usage Meter: capacity / progress meter with threshold indicators.
 */
@Composable
fun ArcUsageMeter(
    used: Int,
    total: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val safeTotal = total.coerceAtLeast(0)
    val safeUsed = used.coerceIn(0, safeTotal)
    val fraction = if (safeTotal > 0) (safeUsed.toFloat() / safeTotal).coerceIn(0f, 1f) else 0f
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = ArcTheme.colors.textSecondary)
            Text("$safeUsed / $safeTotal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ArcTheme.colors.foreground)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(ArcTheme.shapes.pill)
                .background(ArcTheme.colors.surfaceMuted)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(ArcTheme.shapes.pill)
                    .background(if (fraction >= 1f) ArcTheme.colors.success else ArcTheme.colors.accent)
            )
        }
    }
}

/**
 * Arc Line Chart: score progress across assessment sessions.
 */
@Composable
fun ArcLineChart(
    sessionScores: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    title: String = "Eğilim",
    valueRange: ClosedFloatingPointRange<Float>? = null
) {
    val colors = ArcTheme.colors
    val validScores = sessionScores.filter { it.second.isFinite() }
    val dataMin = validScores.minOfOrNull { it.second } ?: 0f
    val dataMax = validScores.maxOfOrNull { it.second } ?: 1f
    val rangeMin = valueRange?.start?.takeIf { it.isFinite() } ?: dataMin
    val rangeMax = valueRange?.endInclusive?.takeIf { it.isFinite() && it > rangeMin } ?: maxOf(dataMax, rangeMin + 1f)
    ArcCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
            Spacer(Modifier.height(14.dp))
            if (validScores.size < 2) {
                Text("Eğilim için en az 2 geçerli değer gerekir.", fontSize = 13.sp, color = colors.textMuted)
            } else {
                Canvas(
                    modifier = Modifier.fillMaxWidth().height(120.dp).semantics {
                        contentDescription = validScores.joinToString(prefix = "$title: ") { "${it.first}, ${it.second}" }
                    }
                ) {
                    val stepX = size.width / (validScores.size - 1)
                    val path = Path()
                    for (grid in 0..4) {
                        val y = size.height * grid / 4f
                        drawLine(colors.borderSubtle, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                    }

                    validScores.forEachIndexed { i, (_, score) ->
                        val x = i * stepX
                        val normalized = ((score.coerceIn(rangeMin, rangeMax) - rangeMin) / (rangeMax - rangeMin)).coerceIn(0f, 1f)
                        val y = size.height - (normalized * size.height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = colors.dataVisualizationPalette.first(), radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                    drawPath(path = path, color = colors.dataVisualizationPalette.first(), style = Stroke(width = 2.5.dp.toPx()))
                }
            }
        }
    }
}

/**
 * Arc Waffle Chart: 10x10 block grid displaying student completion percentage.
 */
@Composable
fun ArcWaffleChart(
    percentage: Int, // 0..100
    modifier: Modifier = Modifier,
    title: String = "İlerleme"
) {
    val colors = ArcTheme.colors
    val safePercentage = percentage.coerceIn(0, 100)
    ArcCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                Text("$safePercentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accent)
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (row in 0 until 10) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (col in 0 until 10) {
                            val index = row * 10 + col
                            val filled = index < safePercentage
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (filled) colors.accent else colors.surfaceMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Arc Slope Chart: compare pre vs post performance slope.
 */
@Composable
fun ArcSlopeChart(
    items: List<Pair<String, Pair<Float, Float>>>, // Name to (InitialScore, FinalScore)
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float>? = null
) {
    val colors = ArcTheme.colors
    val finiteValues = items.flatMap { listOf(it.second.first, it.second.second) }.filter(Float::isFinite)
    val dataMin = finiteValues.minOrNull() ?: 0f
    val dataMax = finiteValues.maxOrNull() ?: (dataMin + 1f)
    val rangeMin = valueRange?.start?.takeIf(Float::isFinite) ?: dataMin
    val rangeMax = valueRange?.endInclusive?.takeIf { it.isFinite() && it > rangeMin } ?: maxOf(dataMax, rangeMin + 1f)
    ArcCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gelişim Eğrileri", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
            Spacer(Modifier.height(12.dp))
            Canvas(
                modifier = Modifier.fillMaxWidth().height(140.dp).semantics {
                    contentDescription = items.joinToString(prefix = "Karşılaştırma: ") { (label, pair) ->
                        "$label, ${pair.first} – ${pair.second}"
                    }
                }
            ) {
                val leftX = 30.dp.toPx()
                val rightX = size.width - 30.dp.toPx()

                items.forEach { (_, scores) ->
                    if (!scores.first.isFinite() || !scores.second.isFinite()) return@forEach
                    val y1 = size.height - (((scores.first.coerceIn(rangeMin, rangeMax) - rangeMin) / (rangeMax - rangeMin)) * size.height)
                    val y2 = size.height - (((scores.second.coerceIn(rangeMin, rangeMax) - rangeMin) / (rangeMax - rangeMin)) * size.height)
                    drawLine(
                        color = if (y2 <= y1) colors.success else colors.danger,
                        start = Offset(leftX, y1),
                        end = Offset(rightX, y2),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = colors.foreground, radius = 3.dp.toPx(), center = Offset(leftX, y1))
                    drawCircle(color = colors.foreground, radius = 3.dp.toPx(), center = Offset(rightX, y2))
                }
            }
        }
    }
}

/**
 * Arc Activity Heatmap: weekly activity density calendar for scoring history.
 */
@Composable
fun ArcActivityHeatmap(
    daysIntensity: List<Int>, // 0..4
    dayLabels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    title: String = "Son 28 Gün"
) {
    val colors = ArcTheme.colors
    ArcCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                daysIntensity.take(28).chunked(7).forEachIndexed { weekIndex, week ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEachIndexed { dayIndex, level ->
                            val flattenedIndex = weekIndex * 7 + dayIndex
                            val color = when (level) {
                                1 -> colors.dataVisualizationPalette.first().copy(alpha = 0.25f)
                                2 -> colors.dataVisualizationPalette.first().copy(alpha = 0.50f)
                                3 -> colors.dataVisualizationPalette.first().copy(alpha = 0.75f)
                                in 4..Int.MAX_VALUE -> colors.dataVisualizationPalette.first()
                                else -> colors.surfaceMuted
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                                    .semantics {
                                        contentDescription = dayLabels.getOrNull(flattenedIndex)
                                            ?: "Grafik değeri $level"
                                    }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Açık renkten koyuya: düşükten yüksek etkinliğe",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
    }
}
