package com.knigdelioglu.arc.compose.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Locale-aware parsing and formatting for values stored as integer minor units. */
object ArcMoneyInputRules {
    fun fractionDigits(currencyCode: String): Int =
        runCatching { Currency.getInstance(currencyCode.uppercase(Locale.ROOT)).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)

    fun parseMinorUnits(
        input: String,
        fractionDigits: Int,
        locale: Locale = Locale.getDefault()
    ): Long? {
        if (input.isBlank()) return null
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val decimal = symbols.decimalSeparator
        val grouping = symbols.groupingSeparator
        // Currency signs and surrounding spaces are allowed only outside the number.
        // Never concatenate digits separated by an unsupported currency sign or whitespace.
        fun isCurrencySymbol(char: Char): Boolean =
            Character.getType(char) == Character.CURRENCY_SYMBOL.toInt()
        val trimmed = input.trim()
        val number = trimmed.trimStart { it.isWhitespace() || isCurrencySymbol(it) }
            .trimEnd { it.isWhitespace() || isCurrencySymbol(it) }
        if (number.isEmpty()) return null
        val integerPart = number.substringBefore(decimal)
        if (grouping != decimal && grouping in integerPart) {
            val groups = integerPart.split(grouping)
            if (groups.first().length !in 1..3 ||
                groups.drop(1).any { it.length != 3 } ||
                groups.any { group -> group.any { it !in '0'..'9' } }
            ) return null
        }
        val normalized = buildString {
            var decimalSeen = false
            number.forEach { char ->
                when {
                    char in '0'..'9' -> append(char)
                    char == decimal -> {
                        if (decimalSeen) return null
                        append('.')
                        decimalSeen = true
                    }
                    char == grouping && !decimalSeen -> Unit
                    else -> return null // Invalid separators, signs, symbols or embedded spaces.
                }
            }
        }
        if (normalized.isEmpty() || normalized == ".") return null
        val scale = fractionDigits.coerceAtLeast(0)
        return runCatching {
            BigDecimal(normalized)
                .setScale(scale, RoundingMode.UNNECESSARY)
                .movePointRight(scale)
                .longValueExact()
        }.getOrNull()
    }

    fun formatCurrency(minorUnits: Long, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val digits = fractionDigits(currencyCode)
        val amount = BigDecimal.valueOf(minorUnits, digits)
        val formatter = NumberFormat.getCurrencyInstance(locale)
        runCatching { Currency.getInstance(currencyCode.uppercase(Locale.ROOT)) }
            .onSuccess { formatter.currency = it }
        return formatter.format(amount)
    }

    fun formatEditable(minorUnits: Long, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val digits = fractionDigits(currencyCode)
        val formatter = NumberFormat.getNumberInstance(locale)
        formatter.minimumFractionDigits = digits
        formatter.maximumFractionDigits = digits
        return formatter.format(BigDecimal.valueOf(minorUnits, digits))
    }
}

/** Currency amount field that reports exact integer minor units to its owner. */
@Composable
fun ArcMoneyInput(
    valueMinorUnits: Long?,
    onValueChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    currencyCode: String = "USD",
    locale: Locale = Locale.getDefault(),
    label: String = "Amount",
    description: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    val colors = ArcTheme.colors
    var focused by remember { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    var precisionError by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(valueMinorUnits, currencyCode, locale) {
        if (!focused) text = valueMinorUnits?.let { ArcMoneyInputRules.formatEditable(it, currencyCode, locale) }.orEmpty()
    }
    val fractionDigits = ArcMoneyInputRules.fractionDigits(currencyCode)
    val symbols = remember(locale) { DecimalFormatSymbols.getInstance(locale) }
    val displayedError = errorMessage ?: if (precisionError) "Enter a valid amount with up to $fractionDigits decimal places" else null

    Column(modifier = modifier) {
        Text(label, color = colors.textSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = ArcTheme.spacing.controlHeightMd),
            shape = ArcTheme.shapes.control,
            color = if (enabled) colors.surface else colors.surfaceMuted,
            border = BorderStroke(if (focused || displayedError != null) 1.5.dp else 1.dp, when {
                displayedError != null -> colors.danger
                focused -> colors.accent
                else -> colors.border
            })
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currencyCode.uppercase(Locale.ROOT), color = colors.textMuted, fontSize = 12.sp)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = text,
                    onValueChange = { typed ->
                        text = typed
                        val parsed = ArcMoneyInputRules.parseMinorUnits(typed, fractionDigits, locale)
                        precisionError = typed.isNotBlank() && typed != symbols.decimalSeparator.toString() && parsed == null
                        if (typed.isEmpty()) onValueChange(null) else if (parsed != null) onValueChange(parsed)
                    },
                    modifier = Modifier.weight(1f).onFocusChanged { focusState ->
                        val lostFocus = focused && !focusState.isFocused
                        focused = focusState.isFocused
                        if (lostFocus) {
                            text = valueMinorUnits?.let { amount ->
                                ArcMoneyInputRules.formatEditable(amount, currencyCode, locale)
                            }.orEmpty()
                            precisionError = false
                        }
                    }
                        .semantics {
                            contentDescription = label
                            if (!displayedError.isNullOrBlank()) error(displayedError)
                        },
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TextStyle(color = colors.foreground, fontSize = 18.sp, textAlign = TextAlign.End),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
        if (!description.isNullOrBlank() && displayedError.isNullOrBlank()) Text(description, color = colors.textMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        if (!displayedError.isNullOrBlank()) Text(displayedError, color = colors.danger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
