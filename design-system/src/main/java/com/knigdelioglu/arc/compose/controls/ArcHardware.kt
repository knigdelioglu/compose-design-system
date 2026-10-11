package com.knigdelioglu.arc.compose.controls

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/**
 * Arc Shortcut Recorder: records or displays physical keyboard shortcuts (e.g. Cmd+K, Alt+S, Enter).
 */
@Composable
fun ArcShortcutRecorder(
    shortcut: String,
    onShortcutChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Klavye Kısayolu",
    conflictingShortcuts: Set<String> = emptySet()
) {
    var isRecording by remember { mutableStateOf(false) }
    var hasConflict by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun stopRecording() {
        isRecording = false
        hasConflict = false
    }

    fun formatShortcut(event: KeyEvent): String? {
        val code = event.keyCode
        if (code in setOf(
                KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT,
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_CTRL_RIGHT,
                KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT,
                KeyEvent.KEYCODE_META_LEFT, KeyEvent.KEYCODE_META_RIGHT
            )) return null
        return buildList {
            if (event.isCtrlPressed) add("Ctrl")
            if (event.isAltPressed) add("Alt")
            if (event.isShiftPressed) add("Shift")
            if (event.isMetaPressed) add("Meta")
            val unicode = event.unicodeChar
            val key = when (code) {
                KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> "Enter"
                KeyEvent.KEYCODE_SPACE -> "Space"
                KeyEvent.KEYCODE_TAB -> "Tab"
                KeyEvent.KEYCODE_DEL -> "Backspace"
                KeyEvent.KEYCODE_FORWARD_DEL -> "Delete"
                KeyEvent.KEYCODE_DPAD_UP -> "Up"
                KeyEvent.KEYCODE_DPAD_DOWN -> "Down"
                KeyEvent.KEYCODE_DPAD_LEFT -> "Left"
                KeyEvent.KEYCODE_DPAD_RIGHT -> "Right"
                in KeyEvent.KEYCODE_F1..KeyEvent.KEYCODE_F12 -> "F${code - KeyEvent.KEYCODE_F1 + 1}"
                else -> if (unicode != 0 && !Character.isISOControl(unicode)) {
                    String(Character.toChars(unicode)).uppercase()
                } else {
                    KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_").replace('_', ' ')
                }
            }
            add(key)
        }.joinToString("+")
    }

    Surface(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (!isRecording) return@onKeyEvent false
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent true
                val event = keyEvent.nativeKeyEvent
                when (event.keyCode) {
                    KeyEvent.KEYCODE_ESCAPE -> {
                        stopRecording()
                        true
                    }
                    KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> {
                        onShortcutChange("")
                        stopRecording()
                        true
                    }
                    else -> {
                        if (event.repeatCount != 0) return@onKeyEvent true
                        val nextShortcut = formatShortcut(event) ?: return@onKeyEvent true
                        val conflicts = conflictingShortcuts.any { it.equals(nextShortcut, ignoreCase = true) }
                        if (conflicts && !nextShortcut.equals(shortcut, ignoreCase = true)) {
                            hasConflict = true
                        } else {
                            onShortcutChange(nextShortcut)
                            stopRecording()
                        }
                        true
                    }
                }
            }
            .clickable {
                if (isRecording) stopRecording() else {
                    hasConflict = false
                    isRecording = true
                    focusRequester.requestFocus()
                }
            }
            .semantics {
                contentDescription = "$label, ${shortcut.ifBlank { "atanmadı" }}"
                stateDescription = when {
                    isRecording -> "Kayıt etkin. Escape ile iptal edin."
                    hasConflict -> "Bu kısayol zaten kullanılıyor."
                    else -> "Kayıt başlatmak için etkinleştirin."
                }
            },
        shape = ArcTheme.shapes.control,
        color = ArcTheme.colors.surfaceRaised,
        border = BorderStroke(1.dp, if (hasConflict) ArcTheme.colors.danger else ArcTheme.colors.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 14.sp, color = ArcTheme.colors.foreground)
                if (isRecording || hasConflict) {
                    Text(
                        text = if (hasConflict) "Bu kısayol kullanımda. Başka bir tuş deneyin."
                        else "Tuşlayın · Esc iptal · Backspace temizle",
                        fontSize = 11.sp,
                        color = if (hasConflict) ArcTheme.colors.danger else ArcTheme.colors.textSecondary
                    )
                }
            }
            Surface(
                shape = ArcTheme.shapes.small,
                color = ArcTheme.colors.surfaceMuted,
                border = BorderStroke(1.dp, ArcTheme.colors.borderStrong)
            ) {
                Text(
                    text = when {
                        isRecording -> "Tuşlayın…"
                        shortcut.isBlank() -> "Atanmadı"
                        else -> shortcut
                    },
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ArcTheme.colors.foreground,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Native Android stylus and finger signature surface with pressure-aware strokes, undo, redo,
 * and an optional structured export callback.
 */
@Composable
fun ArcSignaturePad(
    onSignatureChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 4f,
    onSignature: ((ArcSignature) -> Unit)? = null
) {
    val strokes = remember { mutableStateListOf<ArcSignatureStroke>() }
    val redoStack = remember { mutableStateListOf<ArcSignatureStroke>() }
    var currentPath by remember { mutableStateOf<List<ArcSignaturePoint>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val safeStrokeWidthPx = with(LocalDensity.current) { strokeWidth.coerceAtLeast(1f).dp.toPx() }

    fun makeSignature(values: List<ArcSignatureStroke> = strokes.toList()) = ArcSignature(
        strokes = values,
        canvasWidthPx = canvasSize.width.coerceAtLeast(1),
        canvasHeightPx = canvasSize.height.coerceAtLeast(1)
    )

    fun publish() {
        val signature = makeSignature()
        onSignatureChanged(!signature.isEmpty)
        onSignature?.invoke(signature)
    }

    fun normalizedPoint(position: Offset, pressure: Float, width: Int, height: Int) = ArcSignaturePoint(
        x = (position.x / width.coerceAtLeast(1)).coerceIn(0f, 1f),
        y = (position.y / height.coerceAtLeast(1)).coerceIn(0f, 1f),
        pressure = pressure.takeIf { it.isFinite() && it > 0f }?.coerceIn(0.1f, 1f) ?: 0.5f,
        timeMillis = android.os.SystemClock.uptimeMillis()
    )

    Surface(
        modifier = modifier.height(180.dp),
        shape = ArcTheme.shapes.panel,
        color = ArcTheme.colors.surface,
        border = BorderStroke(1.5.dp, ArcTheme.colors.borderStrong)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    .semantics {
                        contentDescription = "İmza alanı. Kalem veya parmakla çizin; imzayı geri alın veya yineleyin."
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = listOf(normalizedPoint(offset, 1f, size.width, size.height))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPath = currentPath + normalizedPoint(change.position, change.pressure, size.width, size.height)
                            },
                            onDragEnd = {
                                if (currentPath.isNotEmpty()) {
                                    strokes.add(ArcSignatureStroke(currentPath, strokeColor, safeStrokeWidthPx))
                                    redoStack.clear()
                                    currentPath = emptyList()
                                    publish()
                                }
                            },
                            onDragCancel = { currentPath = emptyList() }
                        )
                    }
            ) {
                strokes.forEach { drawSignatureStroke(it) }
                if (currentPath.isNotEmpty()) {
                    drawSignatureStroke(ArcSignatureStroke(currentPath, strokeColor, safeStrokeWidthPx))
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ArcButton(
                    onClick = {
                        if (strokes.isNotEmpty()) {
                            redoStack.add(strokes.removeAt(strokes.lastIndex))
                            publish()
                        }
                    },
                    variant = ArcButtonVariant.Ghost,
                    size = ArcButtonSize.Sm,
                    enabled = strokes.isNotEmpty()
                ) { Icon(Icons.Default.Undo, contentDescription = "Geri al", modifier = Modifier.size(14.dp)) }
                ArcButton(
                    onClick = {
                        if (redoStack.isNotEmpty()) {
                            strokes.add(redoStack.removeAt(redoStack.lastIndex))
                            publish()
                        }
                    },
                    variant = ArcButtonVariant.Ghost,
                    size = ArcButtonSize.Sm,
                    enabled = redoStack.isNotEmpty()
                ) { Icon(Icons.Default.Redo, contentDescription = "Yinele", modifier = Modifier.size(14.dp)) }
                ArcButton(
                    onClick = {
                        strokes.clear()
                        redoStack.clear()
                        currentPath = emptyList()
                        publish()
                    },
                    variant = ArcButtonVariant.Ghost,
                    size = ArcButtonSize.Sm,
                    enabled = strokes.isNotEmpty() || currentPath.isNotEmpty()
                ) { Icon(Icons.Default.Clear, contentDescription = "Temizle", modifier = Modifier.size(14.dp)) }
                if (onSignature != null) {
                    ArcButton(
                        onClick = { onSignature(makeSignature()) },
                        variant = ArcButtonVariant.Ghost,
                        size = ArcButtonSize.Sm,
                        enabled = strokes.isNotEmpty()
                    ) { Icon(Icons.Default.Edit, contentDescription = "İmzayı dışa aktar", modifier = Modifier.size(14.dp)) }
                }
            }

            if (strokes.isEmpty() && currentPath.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kalem veya parmak ile imzalayın", fontSize = 14.sp, color = ArcTheme.colors.textMuted)
                }
            }
        }
    }
}

private fun DrawScope.drawSignatureStroke(stroke: ArcSignatureStroke) {
    val points = stroke.points
    if (points.isEmpty()) return
    val positions = points.map { Offset(it.x.coerceIn(0f, 1f) * size.width, it.y.coerceIn(0f, 1f) * size.height) }
    if (positions.size == 1) {
        drawCircle(
            color = stroke.color,
            radius = stroke.widthPx * points.first().pressure.coerceIn(0.1f, 1f) / 2f,
            center = positions.first()
        )
    } else {
        positions.zipWithNext().forEachIndexed { index, (start, end) ->
            val pressure = ((points[index].pressure + points[index + 1].pressure) / 2f).coerceIn(0.1f, 1f)
            drawLine(
                color = stroke.color,
                start = start,
                end = end,
                strokeWidth = stroke.widthPx * pressure,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
