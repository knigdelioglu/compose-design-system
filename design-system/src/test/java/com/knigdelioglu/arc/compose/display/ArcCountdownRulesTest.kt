package com.knigdelioglu.arc.compose.display

import org.junit.Assert.assertEquals
import org.junit.Test

class ArcCountdownRulesTest {
    @Test fun roundsUpUntilTheDeadlineAndNeverGoesBelowZero() {
        assertEquals(2L, ArcCountdownRules.remainingSeconds(2_001L, 1_000L))
        assertEquals(0L, ArcCountdownRules.remainingSeconds(999L, 1_000L))
    }

    @Test fun formatsShortAndLongDurations() {
        assertEquals("00:00", ArcCountdownRules.format(0L))
        assertEquals("02:05", ArcCountdownRules.format(125L))
        assertEquals("01:02:03", ArcCountdownRules.format(3_723L))
    }
}
