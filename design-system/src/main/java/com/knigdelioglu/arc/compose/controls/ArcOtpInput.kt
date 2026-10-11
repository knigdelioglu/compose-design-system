package com.knigdelioglu.arc.compose.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/** Rules shared by the OTP field and unit tests. Only ASCII verification digits are accepted. */
object ArcOtpInputRules {
    fun sanitize(input: String, length: Int): String =
        input.filter { it in '0'..'9' }.take(length.coerceAtLeast(0))

    fun nextCell(value: String, length: Int): Int = value.length.coerceIn(0, length.coerceAtLeast(0))
}

/** A single accessible numeric field presented as individual verification-code cells. */
@Composable
fun ArcOtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    label: String = "Verification code",
    errorMessage: String? = null,
    enabled: Boolean = true,
    onComplete: ((String) -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    val safeLength = length.coerceAtLeast(1)
    val safeValue = ArcOtpInputRules.sanitize(value, safeLength)
    val colors = ArcTheme.colors
    var focused by remember { mutableStateOf(false) }

    BasicTextField(
        value = safeValue,
        onValueChange = { typed ->
            val clean = ArcOtpInputRules.sanitize(typed, safeLength)
            if (clean != safeValue) {
                onValueChange(clean)
                if (clean.length == safeLength) onComplete?.invoke(clean)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .onFocusChanged { focused = it.isFocused }
            .semantics {
                contentDescription = "$label, ${safeValue.length} of $safeLength digits entered"
                if (!errorMessage.isNullOrBlank()) error(errorMessage)
            },
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(color = androidx.compose.ui.graphics.Color.Transparent, fontSize = 1.sp),
        cursorBrush = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ArcTheme.spacing.space2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(safeLength) { index ->
                        val active = focused && index == ArcOtpInputRules.nextCell(safeValue, safeLength)
                        Surface(
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = ArcTheme.shapes.control,
                            color = if (!enabled) colors.surfaceMuted else colors.surface,
                            border = BorderStroke(
                                if (active || errorMessage != null) 1.5.dp else 1.dp,
                                when {
                                    errorMessage != null -> colors.danger
                                    active -> colors.accent
                                    else -> colors.border
                                }
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = safeValue.getOrNull(index)?.toString().orEmpty(),
                                    modifier = Modifier.clearAndSetSemantics { },
                                    color = if (enabled) colors.foreground else colors.textMuted,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (active && safeValue.length < safeLength) {
                                    Box(Modifier.padding(vertical = 15.dp).size(1.dp, 22.dp).alpha(0.6f).background(colors.accent))
                                }
                            }
                        }
                    }
                }
                // Keep one real editable field for paste, selection, cursor movement and IME support.
                Box(Modifier.fillMaxWidth().height(52.dp).alpha(0.01f)) { innerTextField() }
            }
        }
    )
    if (!errorMessage.isNullOrBlank()) {
        Text(errorMessage, color = colors.danger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
