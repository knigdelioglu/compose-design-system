package com.knigdelioglu.arc.compose.controls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcInteractionRulesTest {
    @Test
    fun timeValuesAreClampedToTwentyFourHourClock() {
        assertEquals(0 to 0, normalizeArcTime(-1, -2))
        assertEquals(23 to 59, normalizeArcTime(24, 99))
        assertEquals(14 to 37, normalizeArcTime(14, 37))
    }

    @Test
    fun sliderIgnoresInvalidStepAndClampsFiniteValues() {
        assertEquals(11f, normalizeArcSliderValue(11f, 0f, 100f, 0f), 0f)
        assertEquals(10f, normalizeArcSliderValue(Float.NaN, 10f, 20f, 2f), 0f)
        assertEquals(20f, normalizeArcSliderValue(19.1f, 0f, 20f, 5f), 0f)
        assertEquals(4f, normalizeArcSliderValue(9f, 4f, 2f, 1f), 0f)
        assertEquals(9f, normalizeArcSliderValue(9f, 0f, 10f, 3f), 0f)
        assertEquals(10f, normalizeArcSliderValue(10f, 0f, 10f, 3f), 0f)
    }

    @Test
    fun resizingOneDividerPreservesThePairAndOtherPanes() {
        val initial = listOf(0.3f, 0.4f, 0.3f)
        val resized = resizeAdjacentPanelWeights(
            weights = initial,
            leftIndex = 0,
            delta = 0.15f,
            minimums = listOf(0.18f, 0.25f, 0.2f),
            maximums = listOf(0.82f, 0.82f, 0.82f)
        )
        assertEquals(0.7f, resized[0] + resized[1], 0.0001f)
        assertEquals(initial[2], resized[2], 0f)
        assertEquals(1f, resized.sum(), 0.0001f)
    }

    @Test
    fun resizeRespectsBothAdjacentBoundsAndRejectsNonFiniteDelta() {
        val initial = listOf(0.3f, 0.4f, 0.3f)
        val atLimit = resizeAdjacentPanelWeights(
            initial, 0, 2f,
            minimums = listOf(0.18f, 0.25f, 0.2f),
            maximums = listOf(0.5f, 0.6f, 0.82f)
        )
        assertEquals(0.45f, atLimit[0], 0.0001f)
        assertEquals(0.25f, atLimit[1], 0.0001f)
        assertEquals(initial, resizeAdjacentPanelWeights(initial, 0, Float.NaN, emptyList(), emptyList()))
    }

    @Test
    fun initialWeightsStayFiniteAndNormalizedForInfeasibleNarrowConstraints() {
        val result = normalizePanelWeights(
            weights = listOf(Float.NaN, -1f, Float.POSITIVE_INFINITY),
            count = 3,
            minimums = listOf(0.6f, 0.6f, 0.6f),
            maximums = listOf(0.8f, 0.8f, 0.8f)
        )
        assertTrue(result.all { it.isFinite() && it >= 0f })
        assertEquals(1f, result.sum(), 0.0001f)
    }
}
