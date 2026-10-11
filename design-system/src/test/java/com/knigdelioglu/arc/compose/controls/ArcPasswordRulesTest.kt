package com.knigdelioglu.arc.compose.controls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcPasswordRulesTest {
    @Test fun estimatesEachIndependentPasswordRule() {
        val result = ArcPasswordRules.estimate("LongPass1!")
        assertEquals(4, result.level)
        assertTrue(result.lengthMet)
        assertTrue(result.uppercaseMet)
        assertTrue(result.numberMet)
        assertTrue(result.symbolMet)
    }

    @Test fun countsUnicodeCodePointsForTheLengthRequirement() {
        val result = ArcPasswordRules.estimate("😀😀😀😀", minimumLength = 8)
        assertFalse(result.lengthMet)
        assertEquals(1, result.level)
    }

    @Test fun emptyAndShortPasswordsKeepActionableLabels() {
        assertEquals("Enter a password", ArcPasswordRules.estimate("").label)
        assertEquals("Too short", ArcPasswordRules.estimate("Ab1!").label)
    }
}
