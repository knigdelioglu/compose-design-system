package com.knigdelioglu.arc.compose.controls

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArcMoneyInputRulesTest {
    @Test fun parsesGroupedAmountsUsingTheLocaleDecimalSeparator() {
        assertEquals(123450L, ArcMoneyInputRules.parseMinorUnits("$1,234.50", 2, Locale.US))
        assertEquals(123456L, ArcMoneyInputRules.parseMinorUnits("1.234,56 €", 2, Locale.GERMANY))
    }

    @Test fun keepsMinorUnitsExactAndRejectsUnsupportedPrecisionAndSigns() {
        assertEquals(1000L, ArcMoneyInputRules.parseMinorUnits("1,000", 0, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits("1.001", 2, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits("-1.00", 2, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits(".", 2, Locale.US))
    }

    @Test fun rejectsMalformedPastedNumbersInsteadOfSilentlyChangingAmounts() {
        assertNull(ArcMoneyInputRules.parseMinorUnits("12abc34", 2, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits("1.2.3", 2, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits("1e3", 2, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits("1,2-3", 2, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits("1+2", 2, Locale.US))
        assertNull(ArcMoneyInputRules.parseMinorUnits("999999999999999999999999", 2, Locale.US))
        // Pasted localized currency symbols remain supported.
        assertEquals(123450L, ArcMoneyInputRules.parseMinorUnits("$1,234.50", 2, Locale.US))
        assertEquals(123456L, ArcMoneyInputRules.parseMinorUnits("1.234,56 €", 2, Locale.GERMANY))
    }

    @Test fun formatsWithTheRequestedCurrencyAndLocale() {
        assertEquals("$1,234.50", ArcMoneyInputRules.formatCurrency(123450, "USD", Locale.US))
        assertEquals("1.234,56 €", ArcMoneyInputRules.formatCurrency(123456, "EUR", Locale.GERMANY))
    }
}
