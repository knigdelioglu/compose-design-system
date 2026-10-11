package com.knigdelioglu.arc.compose.controls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArcPhoneInputRulesTest {
    private val us = ArcPhoneCountry("US", "United States", "+1")

    @Test fun buildsInternationalNumberFromCountryAndNationalDigits() {
        assertEquals("+12025550100", ArcPhoneInputRules.toE164("(202) 555-0100", us))
        assertNull(ArcPhoneInputRules.toE164("", us))
    }

    @Test fun inputIsDigitOnlyAndCappedByE164MaximumLength() {
        assertEquals("12345678901234", ArcPhoneInputRules.nationalDigits("+1 (123) 456-7890 123456789", us))
    }
}
