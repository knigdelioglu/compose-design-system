package com.knigdelioglu.arc.compose.controls

import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import java.util.Locale

enum class ArcBillingPeriod { Monthly, Yearly }

/** Month/year choice that gives the annual saving a visible, textual label. */
@Composable
fun ArcBillingToggle(
    selectedPeriod: ArcBillingPeriod,
    onPeriodChange: (ArcBillingPeriod) -> Unit,
    modifier: Modifier = Modifier,
    monthlyLabel: String = "Monthly",
    yearlyLabel: String = "Yearly",
    savingsLabel: String? = null,
    enabled: Boolean = true
) {
    val colors = ArcTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ArcTheme.spacing.space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(ArcBillingPeriod.Monthly to monthlyLabel, ArcBillingPeriod.Yearly to yearlyLabel).forEach { (period, label) ->
            val isSelected = selectedPeriod == period
            val motion = ArcMotion.current
            val backgroundColor by animateColorAsState(
                if (isSelected) colors.accent else colors.surface,
                animationSpec = motion.standardTween(),
                label = "billingToggleBackground"
            )
            val foregroundColor by animateColorAsState(
                if (isSelected) colors.accentForeground else colors.foreground,
                animationSpec = motion.standardTween(),
                label = "billingToggleForeground"
            )
            Surface(
                modifier = Modifier
                    .semantics {
                        role = Role.RadioButton
                        selected = isSelected
                        contentDescription = if (period == ArcBillingPeriod.Yearly && savingsLabel != null) "$label, $savingsLabel" else label
                    }
                    .clip(ArcTheme.shapes.pill)
                    .clickable(enabled = enabled && !isSelected) { onPeriodChange(period) },
                shape = ArcTheme.shapes.pill,
                color = backgroundColor,
                contentColor = foregroundColor,
                border = if (isSelected) null else BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ArcTheme.spacing.space1)
                ) {
                    Text(label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                    if (period == ArcBillingPeriod.Yearly && savingsLabel != null) {
                        Surface(shape = CircleShape, color = if (isSelected) colors.accentForeground.copy(alpha = 0.16f) else colors.success.copy(alpha = 0.14f)) {
                            Text(
                                savingsLabel,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                color = if (isSelected) colors.accentForeground else colors.success,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Animated price display for a plan with independently supplied monthly and annual prices. */
@Composable
fun ArcBillingPrice(
    period: ArcBillingPeriod,
    monthlyPriceMinorUnits: Long,
    yearlyPriceMinorUnits: Long,
    modifier: Modifier = Modifier,
    currencyCode: String = "USD",
    locale: Locale = Locale.getDefault(),
    monthlySuffix: String = "/ month",
    yearlySuffix: String = "/ year"
) {
    val colors = ArcTheme.colors
    val motion = ArcMotion.current
    val price = if (period == ArcBillingPeriod.Monthly) monthlyPriceMinorUnits else yearlyPriceMinorUnits
    val suffix = if (period == ArcBillingPeriod.Monthly) monthlySuffix else yearlySuffix
    val formatted = ArcMoneyInputRules.formatCurrency(price, currencyCode, locale)
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AnimatedContent(
            targetState = formatted,
            transitionSpec = { fadeIn(motion.fastTween()) togetherWith fadeOut(motion.fastTween()) },
            label = "billingPrice"
        ) { amount ->
            Text(amount, color = colors.foreground, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
        }
        Column { Text(suffix, color = colors.textMuted, fontSize = 12.sp) }
    }
}
