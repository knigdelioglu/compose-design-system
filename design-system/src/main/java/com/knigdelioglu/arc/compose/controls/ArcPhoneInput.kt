package com.knigdelioglu.arc.compose.controls

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import java.util.Locale

data class ArcPhoneCountry(
    val isoCode: String,
    val name: String,
    /** E.164 calling code with a leading plus, for example "+44". */
    val callingCode: String
)

object ArcPhoneInputRules {
    fun nationalDigits(input: String, country: ArcPhoneCountry): String {
        val callingCode = country.callingCode.filter { it in '0'..'9' }
        val availableDigits = (15 - callingCode.length).coerceAtLeast(0)
        var digits = input.filter { it in '0'..'9' }
        if (input.trimStart().startsWith('+') && digits.startsWith(callingCode)) {
            digits = digits.drop(callingCode.length)
        } else if (input.trimStart().startsWith("00") && digits.startsWith("00$callingCode")) {
            digits = digits.drop(2 + callingCode.length)
        }
        return digits.take(availableDigits)
    }

    fun toE164(nationalNumber: String, country: ArcPhoneCountry): String? {
        val callingCode = country.callingCode.filter { it in '0'..'9' }
        val national = nationalDigits(nationalNumber, country)
        if (callingCode.isEmpty() || national.isEmpty() || callingCode.length + national.length > 15) return null
        return "+$callingCode$national"
    }
}

/** Country picker and phone field. The caller supplies localized dialing-plan choices. */
@Composable
fun ArcPhoneInput(
    value: String,
    country: ArcPhoneCountry,
    countries: List<ArcPhoneCountry>,
    onValueChange: (nationalNumber: String, e164: String?) -> Unit,
    onCountryChange: (ArcPhoneCountry) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Phone number",
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    val colors = ArcTheme.colors
    var focused by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var countrySearch by remember { mutableStateOf("") }
    val safeValue = ArcPhoneInputRules.nationalDigits(value, country)
    val visibleCountries = countries.filter {
        countrySearch.isBlank() || it.name.contains(countrySearch, ignoreCase = true) ||
            it.isoCode.contains(countrySearch, ignoreCase = true) || it.callingCode.contains(countrySearch)
    }

    Column(modifier = modifier) {
        Text(label, color = colors.textSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = ArcTheme.spacing.controlHeightMd),
            shape = ArcTheme.shapes.control,
            color = if (enabled) colors.surface else colors.surfaceMuted,
            border = BorderStroke(if (focused || errorMessage != null) 1.5.dp else 1.dp, when {
                errorMessage != null -> colors.danger
                focused -> colors.accent
                else -> colors.border
            })
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box {
                    Text(
                        text = "${country.isoCode.uppercase(Locale.ROOT)}  ${country.callingCode}  ▾",
                        color = colors.foreground,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .semantics { role = Role.Button; contentDescription = "Country ${country.name}, ${country.callingCode}" }
                            .defaultMinSize(minHeight = ArcTheme.spacing.minTouchTarget)
                            .clip(ArcTheme.shapes.small)
                            .clickable(enabled = enabled) { menuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false; countrySearch = "" }
                    ) {
                        if (countries.size > 8) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = countrySearch,
                                onValueChange = { countrySearch = it },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = colors.foreground, fontSize = 14.sp),
                                cursorBrush = SolidColor(colors.accent),
                                decorationBox = { inner ->
                                    androidx.compose.foundation.layout.Box {
                                        if (countrySearch.isEmpty()) Text("Search countries", color = colors.textMuted, fontSize = 14.sp)
                                        inner()
                                    }
                                }
                            )
                        }
                        visibleCountries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text("${option.name}  ${option.callingCode}") },
                                onClick = {
                                    menuExpanded = false
                                    countrySearch = ""
                                    onCountryChange(option)
                                    val nextDigits = ArcPhoneInputRules.nationalDigits(safeValue, option)
                                    if (nextDigits != safeValue) {
                                        onValueChange(nextDigits, ArcPhoneInputRules.toE164(nextDigits, option))
                                    }
                                },
                                enabled = enabled
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = safeValue,
                    onValueChange = { typed ->
                        val digits = ArcPhoneInputRules.nationalDigits(typed, country)
                        onValueChange(digits, ArcPhoneInputRules.toE164(digits, country))
                    },
                    modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused }
                        .semantics {
                            contentDescription = label
                            if (!errorMessage.isNullOrBlank()) error(errorMessage)
                        },
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TextStyle(color = colors.foreground, fontSize = 16.sp),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        }
        if (!errorMessage.isNullOrBlank()) Text(errorMessage, color = colors.danger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
