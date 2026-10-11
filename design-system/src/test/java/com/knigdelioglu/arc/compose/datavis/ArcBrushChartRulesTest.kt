package com.knigdelioglu.arc.compose.datavis

import org.junit.Assert.assertEquals
import org.junit.Test

class ArcBrushChartRulesTest {
    @Test fun selectionMapsToAnInclusiveRangeOfPoints() {
        assertEquals(1..3, ArcBrushChartRules.visibleIndices(0.25f..0.75f, 5))
        assertEquals(2..2, ArcBrushChartRules.visibleIndices(0.5f..0.5f, 5))
    }

    @Test fun emptyAndSinglePointDataHaveValidRanges() {
        assertEquals(0..-1, ArcBrushChartRules.visibleIndices(0f..1f, 0))
        assertEquals(0..0, ArcBrushChartRules.visibleIndices(0.2f..0.8f, 1))
    }

    @Test fun selectionIsClampedAndCannotReverse() {
        assertEquals(1f..1f, ArcBrushChartRules.normalize(1.4f..0.2f))
    }
}
