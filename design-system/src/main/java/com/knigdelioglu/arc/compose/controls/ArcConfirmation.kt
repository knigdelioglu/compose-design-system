package com.knigdelioglu.arc.compose.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * Arc Hold to Confirm: long-press button with a filling progress ring for critical user-confirmed actions.
 */
@Composable
fun ArcHoldToConfirm(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Onaylamak için basılı tutun",
    confirmedLabel: String = "Tamamlandı",
    holdDurationMillis: Int = 1400,
    variant: ArcButtonVariant = ArcButtonVariant.Danger
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var isConfirmed by rememberSaveable { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }
    var holdJob by remember { mutableStateOf<Job?>(null) }
    val motion = ArcMotion.current
    val colors = ArcTheme.colors

    fun cancelHold() {
        isHolding = false
        holdJob?.cancel()
        holdJob = null
        scope.launch {
            if (motion.reducedMotion) progress.snapTo(0f)
            else progress.animateTo(0f, animationSpec = tween(motion.durationExit, easing = motion.easeExit))
        }
    }

    fun startHold() {
        if (isConfirmed) return
        if (isHolding) {
            cancelHold()
            return
        }
        isHolding = true
        holdJob = scope.launch {
            val duration = holdDurationMillis.coerceAtLeast(1)
            if (motion.reducedMotion) {
                delay(duration.toLong())
                progress.snapTo(1f)
            } else {
                progress.animateTo(1f, animationSpec = tween(duration, easing = LinearEasing))
            }
            if (isHolding && !isConfirmed) {
                // Set the guard before invoking app code so re-entrant updates cannot confirm twice.
                isConfirmed = true
                isHolding = false
                onConfirm()
            }
        }
    }

    val buttonColor = if (isConfirmed) colors.success else if (variant == ArcButtonVariant.Danger) colors.danger else colors.accent

    Surface(
        modifier = modifier
            .height(48.dp)
            .pointerInput(isConfirmed, holdDurationMillis) {
                detectTapGestures(
                    onPress = {
                        focusRequester.requestFocus()
                        startHold()
                        try {
                            awaitRelease()
                        } finally {
                            if (isHolding && !isConfirmed) cancelHold()
                        }
                    }
                )
            }
            .focusRequester(focusRequester)
            .focusable(enabled = !isConfirmed)
            .onKeyEvent { event ->
                if (isConfirmed) return@onKeyEvent false
                when {
                    event.key == Key.Enter || event.key == Key.Spacebar -> {
                        when (event.type) {
                            KeyEventType.KeyDown -> if (event.nativeKeyEvent.repeatCount == 0) startHold()
                            KeyEventType.KeyUp -> if (isHolding) cancelHold()
                        }
                        true
                    }
                    event.key == Key.Escape && event.type == KeyEventType.KeyDown -> {
                        if (isHolding) cancelHold()
                        true
                    }
                    else -> false
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = if (isConfirmed) confirmedLabel else label
                stateDescription = when {
                    isConfirmed -> "Onaylandı"
                    isHolding -> "Basılı tutma sürüyor"
                    else -> "Onaylamak için basılı tutun"
                }
                onClick(label = if (isHolding) "Onaylamayı iptal et" else "Basılı tutarak onayla") {
                    focusRequester.requestFocus()
                    startHold()
                    true
                }
            },
        shape = ArcTheme.shapes.control,
        color = buttonColor.copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, buttonColor)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!isConfirmed) {
                    CircularProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier.size(20.dp),
                        color = buttonColor,
                        strokeWidth = 2.5.dp,
                        trackColor = buttonColor.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.width(10.dp))
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = when {
                        isConfirmed -> confirmedLabel
                        isHolding -> "Basılı tutun…"
                        else -> label
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = buttonColor
                )
            }
        }
    }
}

/**
 * Arc Confirm Morph: button that fluidly morphs into inline "Emin misiniz? [Evet] [İptal]" options.
 */
@Composable
fun ArcConfirmMorph(
    prompt: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    initialLabel: String = "Sil",
    confirmLabel: String = "Evet, Sil",
    cancelLabel: String = "Vazgeç",
    variant: ArcButtonVariant = ArcButtonVariant.Danger
) {
    var isConfirming by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.animateContentSize(animationSpec = ArcMotion.current.fastTween())) {
        if (!isConfirming) {
            ArcButton(
                onClick = { isConfirming = true },
                variant = variant,
                size = ArcButtonSize.Sm
            ) {
                Text(initialLabel, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Surface(
                shape = ArcTheme.shapes.control,
                color = ArcTheme.colors.surfaceRaised,
                border = BorderStroke(1.dp, ArcTheme.colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 13.sp,
                        color = ArcTheme.colors.foreground,
                        fontWeight = FontWeight.Medium
                    )
                    ArcButton(
                        onClick = {
                            isConfirming = false
                            onConfirm()
                        },
                        variant = variant,
                        size = ArcButtonSize.Sm
                    ) {
                        Text(confirmLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    ArcButton(
                        onClick = { isConfirming = false },
                        variant = ArcButtonVariant.Ghost,
                        size = ArcButtonSize.Sm
                    ) {
                        Text(cancelLabel, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
