package com.knigdelioglu.arc.compose.controls

import androidx.compose.ui.draw.scale
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.foundation.ArcMotion
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/** Accessible two-state menu glyph whose three bars physically settle into a close mark. */
@Composable
fun ArcIconMorph(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    touchTargetSize: Dp = 48.dp,
    glyphSize: Dp = 22.dp,
    enabled: Boolean = true,
    expandedStateDescription: String = "Expanded",
    collapsedStateDescription: String = "Collapsed"
) {
    val motion = ArcMotion.current
    val progress = animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = motion.springMorph(),
        label = "arcIconMorph"
    )
    val colors = ArcTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.94f else 1f,
        animationSpec = motion.springSnappy(),
        label = "arcIconMorphPress"
    )
    Box(
        modifier = modifier.size(touchTargetSize)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
                stateDescription = if (expanded) expandedStateDescription else collapsedStateDescription
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onExpandedChange(!expanded) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(glyphSize).scale(pressScale)) {
            val p = progress.value
            val left = size.width * 0.14f
            val right = size.width * 0.86f
            val centerY = size.height * 0.5f
            val topStartY = size.height * 0.27f
            val bottomStartY = size.height * 0.73f
            val crossStartY = size.height * 0.27f
            val crossEndY = size.height * 0.73f
            val width = size.minDimension * 0.085f

            drawLine(
                color = colors.foreground,
                start = Offset(left, androidx.compose.ui.util.lerp(topStartY, crossStartY, p)),
                end = Offset(right, androidx.compose.ui.util.lerp(topStartY, crossEndY, p)),
                strokeWidth = width,
                cap = StrokeCap.Round
            )
            drawLine(
                color = colors.foreground,
                start = Offset(left, androidx.compose.ui.util.lerp(centerY, centerY, p)),
                end = Offset(right, androidx.compose.ui.util.lerp(centerY, centerY, p)),
                strokeWidth = width,
                cap = StrokeCap.Round,
                alpha = 1f - p
            )
            drawLine(
                color = colors.foreground,
                start = Offset(left, androidx.compose.ui.util.lerp(bottomStartY, crossEndY, p)),
                end = Offset(right, androidx.compose.ui.util.lerp(bottomStartY, crossStartY, p)),
                strokeWidth = width,
                cap = StrokeCap.Round
            )
        }
    }
}
