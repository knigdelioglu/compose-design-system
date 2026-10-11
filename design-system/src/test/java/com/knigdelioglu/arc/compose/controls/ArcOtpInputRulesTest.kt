package com.knigdelioglu.arc.compose.controls

import org.junit.Assert.assertEquals
import org.junit.Test

class ArcOtpInputRulesTest {
    @Test fun pasteIsSanitizedAndCappedAtTheVisibleLength() {
        assertEquals("123456", ArcOtpInputRules.sanitize("12-3 4a56 789", 6))
    }

    @Test fun unicodeDigitsAreNotSilentlyMixedIntoAsciiVerificationCodes() {
        assertEquals("12", ArcOtpInputRules.sanitize("1٢2", 6))
    }

    @Test fun nextCellStopsAtTheEndAndAcceptsEmptyInput() {
        assertEquals(0, ArcOtpInputRules.nextCell("", 6))
        assertEquals(6, ArcOtpInputRules.nextCell("12345678", 6))
    }
}
