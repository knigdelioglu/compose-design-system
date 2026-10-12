package com.knigdelioglu.arc.compose.advanced

import androidx.compose.ui.draw.clip
import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/**
 * Compact navigation control that grows into a destination surface.
 * Back, Escape, selection, and externally controlled dismissal all close the surface.
 */
@Composable
fun ArcMorphNavigation(
    destinations: List<ArcAdvancedItem>,
    selectedKey: String?,
    onSelectedKeyChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ArcTheme.colors
    val selected = destinations.firstOrNull { it.key == selectedKey }
    val focusRequester = remember { FocusRequester() }
    var wasExpanded by remember { mutableStateOf(false) }
    var triggerFocused by remember { mutableStateOf(false) }
    val reducedMotion = ArcTheme.motion.reducedMotion
    val corner by animateDpAsState(
        targetValue = if (expanded) 22.dp else 28.dp,
        animationSpec = ArcTheme.motion.springMorph(),
        label = "morph-navigation-corner"
    )

    val view = LocalView.current
    val currentOnExpandedChange = rememberUpdatedState(onExpandedChange)
    DisposableEffect(expanded, view) {
        if (!expanded || Build.VERSION.SDK_INT < 33) {
            onDispose { }
        } else {
            val activity = generateSequence(view.context) { context -> (context as? ContextWrapper)?.baseContext }
                .filterIsInstance<Activity>()
                .firstOrNull()
            val dispatcher = activity?.onBackInvokedDispatcher
            if (dispatcher == null) {
                onDispose { }
            } else {
                val callback = OnBackInvokedCallback { currentOnExpandedChange.value(false) }
                dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
                onDispose { dispatcher.unregisterOnBackInvokedCallback(callback) }
            }
        }
    }
    LaunchedEffect(expanded) {
        if (!expanded && wasExpanded) focusRequester.requestFocus()
        wasExpanded = expanded
    }

    BoxWithConstraints(modifier = modifier) {
        val width by animateDpAsState(
            targetValue = maxWidth.coerceAtMost(if (expanded) 264.dp else 72.dp),
            animationSpec = ArcTheme.motion.springMorph(),
            label = "morph-navigation-width"
        )
        Surface(
        modifier = Modifier
            .width(width)
            .animateContentSize(animationSpec = ArcTheme.motion.springMorph())
            .focusGroup()
            .onPreviewKeyEvent { event ->
                if ((event.key == Key.Escape || event.key == Key.Back) && expanded) {
                    onExpandedChange(false)
                    true
                } else false
            },
        shape = RoundedCornerShape(corner),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.82f)),
        shadowElevation = 8.dp
        ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { triggerFocused = it.isFocused }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClickLabel = if (expanded) "Close navigation" else "Open navigation"
                    ) { onExpandedChange(!expanded) }
                    .semantics {
                        contentDescription = selected?.contentDescription ?: "Open navigation"
                        stateDescription = if (expanded) "Expanded" else "Collapsed"
                    }
                    .border(1.dp, if (triggerFocused) colors.accentStrong else colors.border.copy(alpha = 0.32f), RoundedCornerShape(corner))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                selected?.icon?.invoke()
                if (expanded) {
                    Spacer(Modifier.width(12.dp))
                    Text(selected?.label ?: "Navigation", color = colors.foreground, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("⌃", color = colors.textSecondary, fontSize = 17.sp)
                } else if (selected?.icon == null) {
                    Text(selected?.label?.take(1)?.uppercase() ?: "⌄", color = colors.foreground, fontWeight = FontWeight.SemiBold)
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = if (reducedMotion) EnterTransition.None else expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = if (reducedMotion) ExitTransition.None else shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp).selectableGroup()) {
                    destinations.forEach { destination ->
                        val isSelected = destination.key == selectedKey
                        var focused by remember(destination.key) { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focused = it.isFocused }
                                .border(1.dp, if (focused) colors.accentStrong else colors.border.copy(alpha = 0f), ArcTheme.shapes.control)
                                .clip(ArcTheme.shapes.control)
                                .selectable(
                                    selected = isSelected,
                                    enabled = destination.enabled,
                                    role = Role.Tab,
                                    onClick = {
                                        onSelectedKeyChange(destination.key)
                                        onExpandedChange(false)
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            destination.icon?.invoke()
                            Spacer(Modifier.width(if (destination.icon == null) 0.dp else 12.dp))
                            Text(
                                destination.label ?: destination.contentDescription,
                                color = when {
                                    !destination.enabled -> colors.textMuted
                                    isSelected -> colors.foreground
                                    else -> colors.textSecondary
                                },
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            destination.badgeCount?.takeIf { it > 0 }?.let { count ->
                                Text(count.toString(), color = colors.textSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}
