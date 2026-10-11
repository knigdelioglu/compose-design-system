package com.knigdelioglu.arc.compose.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.display.ArcCard
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/**
 * Selection-aware Markdown editor. It stores plain text and applies formatting
 * syntax to the selected text or the caret.
 */
@Composable
fun ArcRichTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Metin yazın…",
    characterCountLabel: (Int) -> String = { "$it karakter" }
) {
    val colors = ArcTheme.colors
    val inputFocus = remember { FocusRequester() }
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (field.text != value) {
            field = TextFieldValue(value, selection = TextRange(value.length))
        }
    }

    fun apply(format: NoteFormat) {
        val next = formatNote(
            field.text,
            field.selection.start,
            field.selection.end,
            format
        )
        field = TextFieldValue(
            text = next.text,
            selection = TextRange(next.selectionStart, next.selectionEnd)
        )
        onValueChange(next.text)
        inputFocus.requestFocus()
    }

    ArcCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(ArcTheme.spacing.space3),
            verticalArrangement = Arrangement.spacedBy(ArcTheme.spacing.space2)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    Triple(Icons.Default.FormatBold, "Kalın", NoteFormat.BOLD),
                    Triple(Icons.Default.FormatItalic, "İtalik", NoteFormat.ITALIC),
                    Triple(Icons.Default.FormatListBulleted, "Liste", NoteFormat.LIST),
                    Triple(Icons.Default.FormatQuote, "Alıntı", NoteFormat.QUOTE)
                ).forEach { (icon, label, format) ->
                    IconButton(
                        onClick = { apply(format) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(icon, contentDescription = label, tint = colors.foreground)
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ArcTheme.shapes.control,
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    BasicTextField(
                        value = field,
                        onValueChange = {
                            field = it
                            if (it.text != value) onValueChange(it.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 132.dp)
                            .focusRequester(inputFocus)
                            .onPreviewKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown ||
                                    !(event.isCtrlPressed || event.isMetaPressed)
                                ) return@onPreviewKeyEvent false

                                val format = when (event.key) {
                                    Key.B -> NoteFormat.BOLD
                                    Key.I -> NoteFormat.ITALIC
                                    else -> return@onPreviewKeyEvent false
                                }
                                apply(format)
                                true
                            },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.foreground),
                        cursorBrush = SolidColor(colors.accent),
                        visualTransformation = markdownVisualTransformation(colors.textSecondary),
                        decorationBox = { inner ->
                            Box {
                                if (field.text.isEmpty()) {
                                    Text(
                                        placeholder,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textMuted
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    Text(
                        text = characterCountLabel(value.length),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

private fun markdownVisualTransformation(secondaryColor: androidx.compose.ui.graphics.Color) =
    VisualTransformation { value ->
        val styled = AnnotatedString.Builder(value.text)
        Regex("\\*\\*(.+?)\\*\\*").findAll(value.text).forEach { match ->
            val start = match.range.first + 2
            val end = match.range.last - 1
            if (start < end) {
                styled.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
            }
        }
        Regex("(?<!\\*)\\*([^*\\n]+)\\*(?!\\*)").findAll(value.text).forEach { match ->
            val start = match.range.first + 1
            val end = match.range.last
            if (start < end) {
                styled.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
            }
        }
        var lineStart = 0
        value.text.lineSequence().forEach { line ->
            if (line.startsWith("> ") && line.length > 2) {
                styled.addStyle(
                    SpanStyle(color = secondaryColor, fontStyle = FontStyle.Italic),
                    lineStart + 2,
                    lineStart + line.length
                )
            }
            lineStart += line.length + 1
        }
        TransformedText(styled.toAnnotatedString(), OffsetMapping.Identity)
    }
