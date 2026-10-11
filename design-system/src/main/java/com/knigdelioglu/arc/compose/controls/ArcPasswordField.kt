package com.knigdelioglu.arc.compose.controls

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import com.knigdelioglu.arc.compose.foundation.ArcTheme

data class ArcPasswordStrengthResult(
    val level: Int,
    val label: String,
    val lengthMet: Boolean,
    val uppercaseMet: Boolean,
    val numberMet: Boolean,
    val symbolMet: Boolean
)

data class ArcPasswordStrengthLabels(
    val field: String = "New password",
    val showPassword: String = "Show password",
    val hidePassword: String = "Hide password",
    val meter: String = "Password strength",
    val lengthRule: (Int) -> String = { "At least $it characters" },
    val uppercaseRule: String = "An uppercase letter",
    val numberRule: String = "A number",
    val symbolRule: String = "A symbol",
    val empty: String = "Enter a password",
    val tooShort: String = "Too short",
    val weak: String = "Weak",
    val fair: String = "Fair",
    val good: String = "Good",
    val strong: String = "Strong"
)

object ArcPasswordRules {
    fun estimate(value: String, minimumLength: Int = 8, labels: ArcPasswordStrengthLabels = ArcPasswordStrengthLabels()): ArcPasswordStrengthResult {
        val lengthMet = value.codePointCount(0, value.length) >= minimumLength.coerceAtLeast(1)
        val uppercase = value.any(Char::isUpperCase)
        val number = value.any(Char::isDigit)
        val symbol = value.any { !it.isLetterOrDigit() }
        val level = listOf(lengthMet, uppercase, number, symbol).count { it }
        val title = when {
            value.isEmpty() -> labels.empty
            !lengthMet -> labels.tooShort
            level <= 1 -> labels.weak
            level == 2 -> labels.fair
            level == 3 -> labels.good
            else -> labels.strong
        }
        return ArcPasswordStrengthResult(level, title, lengthMet, uppercase, number, symbol)
    }
}

/** Controlled password input with an explicit reveal button and inline validation state. */
@Composable
fun ArcPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    placeholder: String = "",
    errorMessage: String? = null,
    enabled: Boolean = true,
    revealed: Boolean? = null,
    onRevealedChange: ((Boolean) -> Unit)? = null,
    showPasswordLabel: String = "Show password",
    hidePasswordLabel: String = "Hide password"
) {
    var localReveal by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    val isRevealed = revealed ?: localReveal
    val colors = ArcTheme.colors
    Column(modifier = modifier) {
        Text(label, color = colors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) Text(placeholder, color = colors.textMuted, fontSize = 15.sp)
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                            .semantics {
                                contentDescription = label
                                if (!errorMessage.isNullOrBlank()) error(errorMessage)
                            },
                        enabled = enabled,
                        singleLine = true,
                        textStyle = TextStyle(color = colors.foreground, fontSize = 15.sp),
                        cursorBrush = SolidColor(colors.accent),
                        visualTransformation = if (isRevealed) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
                Spacer(Modifier.size(8.dp))
                Box(
                    modifier = Modifier.size(ArcTheme.spacing.minTouchTarget)
                        .clickable(enabled = enabled) {
                            val next = !isRevealed
                            if (revealed == null) localReveal = next
                            onRevealedChange?.invoke(next)
                        }
                        .semantics {
                            role = Role.Button
                            contentDescription = if (isRevealed) hidePasswordLabel else showPasswordLabel
                            stateDescription = if (isRevealed) "Password visible" else "Password hidden"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }
        }
        if (!errorMessage.isNullOrBlank()) Text(errorMessage, color = colors.danger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

/** New-password field with a four-step local strength estimate and live rule feedback. */
@Composable
fun ArcPasswordStrength(
    value: String,
    onValueChange: (String, ArcPasswordStrengthResult) -> Unit,
    modifier: Modifier = Modifier,
    minimumLength: Int = 8,
    errorMessage: String? = null,
    labels: ArcPasswordStrengthLabels = ArcPasswordStrengthLabels(),
    enabled: Boolean = true
) {
    val result = ArcPasswordRules.estimate(value, minimumLength, labels)
    val colors = ArcTheme.colors
    Column(modifier = modifier.animateContentSize(ArcMotion.current.springSmooth()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArcPasswordField(
            value = value,
            onValueChange = { next -> onValueChange(next, ArcPasswordRules.estimate(next, minimumLength, labels)) },
            label = labels.field,
            errorMessage = errorMessage,
            enabled = enabled,
            showPasswordLabel = labels.showPassword,
            hidePasswordLabel = labels.hidePassword
        )
        Row(
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = labels.meter
                stateDescription = "${result.label}, ${result.level} of 4"
            },
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(4) { index ->
                val color = if (index < result.level) when {
                    result.level <= 1 -> colors.danger
                    result.level == 2 -> colors.warning
                    else -> colors.success
                } else colors.surfaceMuted
                Box(Modifier.weight(1f).height(5.dp).background(color, CircleShape))
            }
        }
        Text("${labels.meter}: ${result.label}", color = colors.textSecondary, fontSize = 12.sp)
        listOf(
            labels.lengthRule(minimumLength) to result.lengthMet,
            labels.uppercaseRule to result.uppercaseMet,
            labels.numberRule to result.numberMet,
            labels.symbolRule to result.symbolMet
        ).forEach { (rule, met) ->
            Text(text = "${if (met) "✓" else "○"}  $rule", color = if (met) colors.success else colors.textMuted, fontSize = 12.sp)
        }
    }
}
