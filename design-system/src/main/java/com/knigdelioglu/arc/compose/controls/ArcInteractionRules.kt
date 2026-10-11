package com.knigdelioglu.arc.compose.controls

/** Returns a safe 24-hour clock value for externally supplied picker values. */
internal fun normalizeArcTime(hour: Int, minute: Int): Pair<Int, Int> =
    hour.coerceIn(0, 23) to minute.coerceIn(0, 59)

/**
 * Clamps a slider value and snaps it to a positive, finite step when one is provided.
 * Invalid ranges and steps are deliberately ignored instead of reaching gesture math.
 */
internal fun normalizeArcSliderValue(
    value: Float,
    min: Float,
    max: Float,
    step: Float?
): Float {
    if (!min.isFinite() || !max.isFinite() || max <= min) return min.takeIf(Float::isFinite) ?: 0f
    val safeValue = value.takeIf(Float::isFinite) ?: min
    val safeStep = step?.takeIf { it.isFinite() && it > 0f }
    val bounded = safeValue.coerceIn(min, max)
    if (safeStep == null) return bounded
    val snapped = min + (kotlin.math.round((bounded - min) / safeStep) * safeStep)
    val candidates = listOf(min, snapped.coerceIn(min, max), max)
    return candidates.minBy { kotlin.math.abs(it - bounded) }
}

/**
 * Resizes two neighboring normalized panes while keeping their combined allocation fixed.
 * Other pane weights are untouched.
 */
internal fun resizeAdjacentPanelWeights(
    weights: List<Float>,
    leftIndex: Int,
    delta: Float,
    minimums: List<Float>,
    maximums: List<Float>
): List<Float> {
    if (leftIndex < 0 || leftIndex + 1 >= weights.size || !delta.isFinite()) return weights
    val left = weights[leftIndex].coerceAtLeast(0f)
    val right = weights[leftIndex + 1].coerceAtLeast(0f)
    val pairTotal = left + right
    if (pairTotal <= 0f) return weights

    val minLeft = minimums.getOrElse(leftIndex) { 0f }.coerceAtLeast(0f)
    val minRight = minimums.getOrElse(leftIndex + 1) { 0f }.coerceAtLeast(0f)
    val maxLeft = maximums.getOrElse(leftIndex) { 1f }.coerceAtLeast(minLeft)
    val maxRight = maximums.getOrElse(leftIndex + 1) { 1f }.coerceAtLeast(minRight)
    val lower = maxOf(minLeft, pairTotal - maxRight)
    val upper = minOf(maxLeft, pairTotal - minRight)
    if (lower > upper) return weights

    val newLeft = (left + delta).coerceIn(lower, upper)
    return weights.toMutableList().also {
        it[leftIndex] = newLeft
        it[leftIndex + 1] = pairTotal - newLeft
    }
}

/** Produces finite, non-negative weights that add up to one. */
internal fun normalizePanelWeights(
    weights: List<Float>,
    count: Int,
    minimums: List<Float>,
    maximums: List<Float>
): List<Float> {
    if (count <= 0) return emptyList()
    val mins = (0 until count).map { minimums.getOrElse(it) { 0f }.takeIf(Float::isFinite)?.coerceAtLeast(0f) ?: 0f }
    val requestedMaxs = (0 until count).map {
        maximums.getOrElse(it) { 1f }.takeIf(Float::isFinite)?.coerceAtLeast(mins[it]) ?: 1f
    }
    val maxs = if (requestedMaxs.sum() < 1f) requestedMaxs.map { maxOf(it, 1f / count) } else requestedMaxs
    val minTotal = mins.sum()
    val effectiveMins = if (minTotal > 1f) mins.map { it / minTotal } else mins
    val seed = (0 until count).map { weights.getOrElse(it) { 1f / count }.takeIf(Float::isFinite)?.coerceAtLeast(0f) ?: 0f }
    val total = seed.sum()
    val normalized = if (total > 0f) seed.map { it / total } else List(count) { 1f / count }
    var result = normalized.mapIndexed { index, weight -> weight.coerceIn(effectiveMins[index], maxs[index]) }
    val difference = 1f - result.sum()
    if (kotlin.math.abs(difference) > 0.0001f) {
        val candidates = result.indices.filter { index ->
            if (difference > 0f) result[index] < maxs[index] else result[index] > effectiveMins[index]
        }
        if (candidates.isNotEmpty()) {
            val share = difference / candidates.size
            result = result.mapIndexed { index, weight ->
                if (index in candidates) (weight + share).coerceIn(effectiveMins[index], maxs[index]) else weight
            }
            val remainder = 1f - result.sum()
            val target = result.indices.firstOrNull { index ->
                if (remainder > 0f) result[index] < maxs[index] else result[index] > effectiveMins[index]
            }
            if (target != null) result = result.toMutableList().also { it[target] = it[target] + remainder }
        }
    }
    val sum = result.sum()
    return if (sum > 0f) result.map { it / sum } else List(count) { 1f / count }
}
