package com.knigdelioglu.arc.compose.display

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import kotlinx.coroutines.delay
import kotlin.math.ceil

object ArcCountdownRules {
    fun remainingSeconds(deadlineMillis: Long, nowMillis: Long): Long =
        ceil(((deadlineMillis - nowMillis).coerceAtLeast(0L)) / 1000.0).toLong()

    fun format(totalSeconds: Long): String {
        val seconds = totalSeconds.coerceAtLeast(0L)
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remaining = seconds % 60
        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, remaining)
        else "%02d:%02d".format(minutes, remaining)
    }
}

/** Deadline-based countdown that stays correct after suspension and calls completion once. */
@Composable
fun ArcCountdown(
    deadlineMillis: Long,
    modifier: Modifier = Modifier,
    label: String = "Time remaining",
    fontSize: TextUnit = 24.sp,
    onFinished: () -> Unit = {}
) {
    var nowMillis by remember(deadlineMillis) { mutableLongStateOf(System.currentTimeMillis()) }
    val latestOnFinished by rememberUpdatedState(onFinished)
    val motion = ArcMotion.current
    LaunchedEffect(deadlineMillis) {
        while (true) {
            val now = System.currentTimeMillis()
            nowMillis = now
            val remaining = ArcCountdownRules.remainingSeconds(deadlineMillis, now)
            if (remaining == 0L) {
                latestOnFinished()
                break
            }
            delay(minOf(1000L, (deadlineMillis - now).coerceAtLeast(1L)))
        }
    }
    val display = ArcCountdownRules.format(ArcCountdownRules.remainingSeconds(deadlineMillis, nowMillis))
    val colors = ArcTheme.colors
    AnimatedContent(
        targetState = display,
        modifier = modifier.semantics { contentDescription = "$label, $display" },
        transitionSpec = { fadeIn(motion.fastTween()) togetherWith fadeOut(motion.fastTween()) },
        label = "arcCountdown"
    ) { time ->
        Text(time, color = colors.foreground, fontSize = fontSize, fontWeight = FontWeight.SemiBold)
    }
}
