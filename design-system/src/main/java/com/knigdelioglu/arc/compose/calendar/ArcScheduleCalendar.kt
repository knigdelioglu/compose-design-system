package com.knigdelioglu.arc.compose.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.display.ArcCard
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Immutable
data class ArcScheduleWeek(
    val number: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val hasSpecialEvent: Boolean = false
)

@Immutable
data class ArcScheduleBreak(
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

private data class ArcScheduleDay(
    val date: LocalDate,
    val isInMonth: Boolean,
    val weekNumber: Int?,
    val isBreak: Boolean,
    val breakLabel: String?,
    val hasSpecialEvent: Boolean
)

/**
 * Monthly schedule calendar that selects teaching weeks and marks breaks/events.
 * The models are date-only UI values and do not depend on an app's plan schema.
 */
@Composable
fun ArcScheduleCalendar(
    selectedWeek: Int?,
    onSelectWeek: (Int) -> Unit,
    weeks: List<ArcScheduleWeek>,
    breaks: List<ArcScheduleBreak>,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.forLanguageTag("tr-TR"),
    previousMonthLabel: String = "Önceki ay",
    nextMonthLabel: String = "Sonraki ay",
    breakDescription: (String?) -> String = { label ->
        label?.takeIf(String::isNotBlank)?.let { "ara tatil, $it" } ?: "ara tatil"
    },
    weekDescription: (Int) -> String = { "$it. öğretim haftası" },
    specialEventLabel: String = "özel gün",
    selectedLabel: String = "seçili"
) {
    val firstDate = remember(weeks) { weeks.minOfOrNull { it.startDate } }
    val lastDate = remember(weeks) { weeks.maxOfOrNull { it.endDate } }
    val selectedStart = remember(weeks, selectedWeek) {
        weeks.firstOrNull { it.number == selectedWeek }?.startDate
    }
    val scheduleKey = firstDate?.toString()
    val selectedKey = selectedStart?.toString()

    var monthValue by rememberSaveable(scheduleKey) {
        mutableStateOf(YearMonth.from(selectedStart ?: firstDate ?: LocalDate.now()).toString())
    }
    var syncedSelection by rememberSaveable(scheduleKey) {
        mutableStateOf(selectedKey)
    }
    LaunchedEffect(selectedKey) {
        if (selectedKey != null && selectedKey != syncedSelection) {
            monthValue = YearMonth.from(selectedStart).toString()
            syncedSelection = selectedKey
        }
    }

    val displayedMonth = YearMonth.parse(monthValue)
    val earliestMonth = firstDate?.let(YearMonth::from)
    val latestMonth = lastDate?.let(YearMonth::from)
    val days = remember(displayedMonth, weeks, breaks) {
        buildScheduleMonth(displayedMonth, weeks, breaks)
    }
    val monthTitle = remember(displayedMonth, locale) {
        displayedMonth
            .format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
            .replaceFirstChar { it.titlecase(locale) }
    }
    val dayLabels = remember(locale) {
        (1..7).map { DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, locale) }
    }
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    }

    ArcCard(modifier = modifier) {
        Column(modifier = Modifier.padding(ArcTheme.spacing.space3)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(monthTitle, color = ArcTheme.colors.foreground)
                Row {
                    IconButton(
                        onClick = { monthValue = displayedMonth.minusMonths(1).toString() },
                        enabled = earliestMonth != null && displayedMonth > earliestMonth,
                        modifier = Modifier.size(ArcTheme.spacing.minTouchTarget)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = previousMonthLabel)
                    }
                    IconButton(
                        onClick = { monthValue = displayedMonth.plusMonths(1).toString() },
                        enabled = latestMonth != null && displayedMonth < latestMonth,
                        modifier = Modifier.size(ArcTheme.spacing.minTouchTarget)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = nextMonthLabel)
                    }
                }
            }

            Spacer(Modifier.height(ArcTheme.spacing.space2))
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = ArcTheme.colors.textSecondary
                    )
                }
            }

            days.chunked(7).forEach { weekDays ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekDays.forEach { day ->
                        val enabled = day.isInMonth && day.weekNumber != null
                        val isSelected = enabled && selectedWeek == day.weekNumber
                        val spokenLabel = buildString {
                            append(day.date.format(dateFormatter))
                            if (day.isBreak) append(", ").append(breakDescription(day.breakLabel))
                            day.weekNumber?.let { append(", ").append(weekDescription(it)) }
                            if (day.hasSpecialEvent) append(", ").append(specialEventLabel)
                            if (isSelected) append(", ").append(selectedLabel)
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .padding(1.dp)
                                .semantics {
                                    contentDescription = spokenLabel
                                    selected = isSelected
                                }
                                .clickable(enabled = enabled, role = Role.Button) {
                                    day.weekNumber?.let(onSelectWeek)
                                },
                            shape = ArcTheme.shapes.small,
                            color = if (isSelected) ArcTheme.colors.surfaceMuted else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, ArcTheme.colors.accent) else null
                        ) {
                            Column(
                                modifier = Modifier.height(48.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.date.dayOfMonth.toString(),
                                    color = when {
                                        !day.isInMonth -> ArcTheme.colors.textMuted.copy(alpha = 0.4f)
                                        day.isBreak -> ArcTheme.colors.warning
                                        enabled -> ArcTheme.colors.foreground
                                        else -> ArcTheme.colors.textMuted
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (day.hasSpecialEvent || day.isBreak) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (day.isBreak) ArcTheme.colors.warning
                                                else ArcTheme.colors.accent
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildScheduleMonth(
    month: YearMonth,
    weeks: List<ArcScheduleWeek>,
    breaks: List<ArcScheduleBreak>
): List<ArcScheduleDay> {
    val firstDate = month.atDay(1)
    val monday = firstDate.minusDays((firstDate.dayOfWeek.value - 1).toLong())
    val requiredCells = firstDate.dayOfWeek.value - 1 + month.lengthOfMonth()
    val cellCount = ((requiredCells + 6) / 7).coerceAtLeast(5) * 7

    return (0 until cellCount).map { offset ->
        val date = monday.plusDays(offset.toLong())
        val breakPeriod = breaks.firstOrNull { date in it.startDate..it.endDate }
        val week = if (breakPeriod == null) {
            weeks.firstOrNull { date in it.startDate..it.endDate }
        } else {
            null
        }
        ArcScheduleDay(
            date = date,
            isInMonth = YearMonth.from(date) == month,
            weekNumber = week?.number,
            isBreak = breakPeriod != null,
            breakLabel = breakPeriod?.label,
            hasSpecialEvent = week?.hasSpecialEvent == true
        )
    }
}
