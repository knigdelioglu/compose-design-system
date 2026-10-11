package com.knigdelioglu.arc.compose.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme

data class ArcMentionCandidate(
    val id: String,
    val displayName: String,
    val token: String = displayName,
    val trigger: Char = '@',
    val supportingText: String? = null
)

data class ArcMentionQuery(val trigger: Char, val query: String, val start: Int, val cursor: Int)

data class ArcMentionInsertion(val value: TextFieldValue, val tokenStart: Int, val tokenEnd: Int)

object ArcMentionInputRules {
    fun activeQuery(text: String, cursor: Int): ArcMentionQuery? {
        val caret = cursor.coerceIn(0, text.length)
        val start = text.substring(0, caret).indexOfLast { it == '@' || it == '#' }
        if (start < 0) return null
        if (start > 0 && !text[start - 1].isWhitespace() && text[start - 1] !in "([{\n\t") return null
        val query = text.substring(start + 1, caret)
        if (query.any { it.isWhitespace() || it in "@#" }) return null
        return ArcMentionQuery(text[start], query, start, caret)
    }

    fun insert(value: TextFieldValue, query: ArcMentionQuery, candidate: ArcMentionCandidate): ArcMentionInsertion {
        val cursor = query.cursor.coerceIn(query.start + 1, value.text.length)
        val prefix = value.text.substring(0, query.start)
        val suffix = value.text.substring(cursor)
        val token = "${query.trigger}${candidate.token}"
        val separator = if (suffix.firstOrNull()?.isWhitespace() == true || suffix.isEmpty()) "" else " "
        val updated = prefix + token + separator + suffix
        val tokenStart = prefix.length
        val tokenEnd = tokenStart + token.length
        return ArcMentionInsertion(TextFieldValue(updated, TextRange(tokenEnd + separator.length)), tokenStart, tokenEnd)
    }
}

/** Multiline message field with caret-aware @people and #channel suggestions. */
@Composable
fun ArcMentionInput(
    value: TextFieldValue,
    candidates: List<ArcMentionCandidate>,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onMentionSelected: ((ArcMentionCandidate, IntRange) -> Unit)? = null,
    label: String = "Message",
    placeholder: String = "Write a message…",
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colors = ArcTheme.colors
    val query = if (value.selection.collapsed) ArcMentionInputRules.activeQuery(value.text, value.selection.end) else null
    val matches = query?.let { active ->
        candidates.filter { it.trigger == active.trigger && it.displayName.contains(active.query, ignoreCase = true) }
    }.orEmpty()
    val queryKey = query?.let { "${it.trigger}:${it.start}:${it.query}" }
    var dismissedQuery by remember { mutableStateOf<String?>(null) }
    var activeIndex by remember { mutableIntStateOf(0) }
    var focused by remember { mutableStateOf(false) }
    val showSuggestions = enabled && focused && matches.isNotEmpty() && queryKey != dismissedQuery

    fun choose(index: Int) {
        val activeQuery = query ?: return
        val candidate = matches.getOrNull(index) ?: return
        val inserted = ArcMentionInputRules.insert(value, activeQuery, candidate)
        onValueChange(inserted.value)
        onMentionSelected?.invoke(candidate, inserted.tokenStart until inserted.tokenEnd)
        dismissedQuery = null
    }

    Column(modifier = modifier) {
        Text(label, color = colors.textSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        androidx.compose.foundation.layout.Box {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ArcTheme.shapes.control,
                color = if (enabled) colors.surface else colors.surfaceMuted,
                border = BorderStroke(if (focused || errorMessage != null) 1.5.dp else 1.dp, when {
                    errorMessage != null -> colors.danger
                    focused -> colors.accent
                    else -> colors.border
                })
            ) {
                androidx.compose.foundation.layout.Box(Modifier.padding(14.dp)) {
                    if (value.text.isEmpty() && placeholder.isNotEmpty()) Text(placeholder, color = colors.textMuted, fontSize = 15.sp)
                    BasicTextField(
                        value = value,
                        onValueChange = {
                            dismissedQuery = null
                            activeIndex = 0
                            onValueChange(it)
                        },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                            .onPreviewKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown || !showSuggestions) return@onPreviewKeyEvent false
                                when (event.key) {
                                    Key.DirectionDown -> { activeIndex = (activeIndex + 1).coerceAtMost(matches.lastIndex); true }
                                    Key.DirectionUp -> { activeIndex = (activeIndex - 1).coerceAtLeast(0); true }
                                    Key.Enter -> { choose(activeIndex); true }
                                    Key.Escape -> { dismissedQuery = queryKey; true }
                                    else -> false
                                }
                            }
                            .semantics {
                                contentDescription = label
                                if (!errorMessage.isNullOrBlank()) error(errorMessage)
                            },
                        enabled = enabled,
                        minLines = 3,
                        maxLines = 8,
                        textStyle = TextStyle(color = colors.foreground, fontSize = 15.sp, lineHeight = 22.sp),
                        cursorBrush = SolidColor(colors.accent),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Default),
                        keyboardActions = keyboardActions
                    )
                }
            }
            DropdownMenu(expanded = showSuggestions, onDismissRequest = { dismissedQuery = queryKey }) {
                matches.forEachIndexed { index, candidate ->
                    DropdownMenuItem(
                        modifier = Modifier.semantics { selected = index == activeIndex },
                        text = {
                            Column {
                                Text("${candidate.trigger}${candidate.displayName}")
                                candidate.supportingText?.let { Text(it, color = colors.textMuted, fontSize = 12.sp) }
                            }
                        },
                        onClick = { choose(index) }
                    )
                }
            }
        }
        if (!errorMessage.isNullOrBlank()) Text(errorMessage, color = colors.danger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
