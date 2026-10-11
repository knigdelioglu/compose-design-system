package com.knigdelioglu.arc.compose.gallery

import android.app.Activity
import android.os.Bundle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.controls.ArcButton
import com.knigdelioglu.arc.compose.controls.ArcButtonVariant
import com.knigdelioglu.arc.compose.controls.ArcCheckbox
import com.knigdelioglu.arc.compose.controls.ArcColorPicker
import com.knigdelioglu.arc.compose.controls.ArcCombobox
import com.knigdelioglu.arc.compose.controls.ArcContextAction
import com.knigdelioglu.arc.compose.controls.ArcContextMenu
import com.knigdelioglu.arc.compose.controls.ArcDatePicker
import com.knigdelioglu.arc.compose.controls.ArcDateRangePicker
import com.knigdelioglu.arc.compose.controls.ArcElasticSlider
import com.knigdelioglu.arc.compose.controls.ArcHoldToConfirm
import com.knigdelioglu.arc.compose.controls.ArcMorphSelect
import com.knigdelioglu.arc.compose.controls.ArcMultiSelect
import com.knigdelioglu.arc.compose.controls.ArcRadioCards
import com.knigdelioglu.arc.compose.controls.ArcRadioGroup
import com.knigdelioglu.arc.compose.controls.ArcSegmentedControl
import com.knigdelioglu.arc.compose.controls.ArcSelect
import com.knigdelioglu.arc.compose.controls.ArcShortcutRecorder
import com.knigdelioglu.arc.compose.controls.ArcSignature
import com.knigdelioglu.arc.compose.controls.ArcSignaturePad
import com.knigdelioglu.arc.compose.controls.ArcSwitch
import com.knigdelioglu.arc.compose.controls.ArcTimePicker
import com.knigdelioglu.arc.compose.datavis.ArcBarChart
import com.knigdelioglu.arc.compose.datavis.ArcBarData
import com.knigdelioglu.arc.compose.datavis.ArcLineChart
import com.knigdelioglu.arc.compose.datavis.ArcSortableDataTable
import com.knigdelioglu.arc.compose.datavis.ArcTableColumn
import com.knigdelioglu.arc.compose.foundation.ArcAccent
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import com.knigdelioglu.arc.compose.navigation.ArcCardStack
import com.knigdelioglu.arc.compose.navigation.ArcCarousel
import com.knigdelioglu.arc.compose.navigation.ArcCommandAction
import com.knigdelioglu.arc.compose.navigation.ArcCommandPalette
import com.knigdelioglu.arc.compose.navigation.ArcResizablePanels
import androidx.compose.ui.graphics.Color

private data class GalleryRow(val id: Int, val name: String, val amount: Int)

class GalleryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ComposeView(this).apply { setContent { ComponentGallery() } })
    }
}

@Composable
private fun ComponentGallery() {
    var dark by remember { mutableStateOf(false) }
    var accent by remember { mutableStateOf(ArcAccent.Violet) }
    var reducedMotion by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }
    var switched by remember { mutableStateOf(false) }
    var radioChoice by remember { mutableStateOf("One") }
    var cardChoice by remember { mutableStateOf("Compact") }
    var selectedIndex by remember { mutableStateOf(0) }
    var selectedText by remember { mutableStateOf<String?>("Beta") }
    var multiSelection by remember { mutableStateOf(setOf("A")) }
    var sliderValue by remember { mutableStateOf(42f) }
    var time by remember { mutableStateOf(9 to 30) }
    var date by remember { mutableStateOf("2026-10-11") }
    var range by remember { mutableStateOf("2026-10-05" to "2026-10-11") }
    var shortcut by remember { mutableStateOf("Ctrl+K") }
    var paletteOpen by remember { mutableStateOf(false) }
    var exportSummary by remember { mutableStateOf("İmza çıktısı burada gösterilir.") }
    var cards by remember { mutableStateOf(listOf("First card", "Second card", "Third card", "Fourth card")) }
    var tableSelection by remember { mutableStateOf(emptySet<GalleryRow>()) }
    var panelWeights by remember { mutableStateOf(emptyList<Float>()) }
    val tableRows = remember { listOf(GalleryRow(1, "One", 16), GalleryRow(2, "Two", 28), GalleryRow(3, "Three", 9)) }
    val tableColumns = remember {
        listOf(
            ArcTableColumn<GalleryRow>("Name", comparator = compareBy { it.name }) { Text(it.name) },
            ArcTableColumn<GalleryRow>("Amount", comparator = compareBy { it.amount }) { Text(it.amount.toString()) }
        )
    }

    ArcTheme(darkTheme = dark, accent = accent, reducedMotion = reducedMotion) {
        Surface(Modifier.fillMaxSize(), color = ArcTheme.colors.background) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text("Arc Compose · Component Gallery", style = MaterialTheme.typography.headlineSmall, color = ArcTheme.colors.foreground)
                Text("Native Compose controls, states, keyboard behavior, and motion tokens.", color = ArcTheme.colors.textSecondary)
                GallerySection("Theme and motion") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ArcButton(onClick = { dark = !dark }, variant = ArcButtonVariant.Outline) { Text(if (dark) "Dark" else "Light") }
                        ArcButton(onClick = { reducedMotion = !reducedMotion }, variant = ArcButtonVariant.Outline) { Text(if (reducedMotion) "Motion off" else "Motion on") }
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ArcAccent.entries.forEach { choice ->
                            ArcButton(onClick = { accent = choice }, variant = if (accent == choice) ArcButtonVariant.Primary else ArcButtonVariant.Outline) {
                                Text(choice.name)
                            }
                        }
                    }
                }
                GallerySection("Actions and selection") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ArcButton(onClick = {}, variant = ArcButtonVariant.Primary) { Text("Primary") }
                        ArcButton(onClick = {}, variant = ArcButtonVariant.Outline) { Text("Outline") }
                        ArcHoldToConfirm(onConfirm = {}, label = "Hold to confirm", holdDurationMillis = 800)
                    }
                    ArcCheckbox(checked, onCheckedChange = { checked = it }, label = "Checkbox state")
                    ArcSwitch(switched, onCheckedChange = { switched = it }, label = "Switch state")
                    ArcRadioGroup(listOf("One", "Two", "Three"), radioChoice, onOptionSelected = { radioChoice = it })
                    ArcRadioCards(
                        options = listOf("Compact", "Comfortable"), selectedOption = cardChoice,
                        onOptionSelected = { cardChoice = it }, titleProvider = { it },
                        descriptionProvider = { "Selectable option card" }
                    )
                    ArcSegmentedControl(listOf("Overview", "Details", "History"), selectedIndex, { selectedIndex = it })
                }
                GallerySection("Menus and selection fields") {
                    ArcSelect(selectedText, listOf("Alpha", "Beta", "Gamma"), { selectedText = it })
                    ArcCombobox(selectedText, listOf("Alpha", "Beta", "Gamma"), { selectedText = it })
                    ArcMorphSelect(selectedText, listOf("Alpha", "Beta", "Gamma"), { selectedText = it })
                    ArcMultiSelect(
                        selectedItems = multiSelection,
                        items = ('A'..'J').map { it.toString() },
                        onSelectionChanged = { multiSelection = it },
                        searchable = true
                    )
                    ArcContextMenu(
                        content = { ArcButton(onClick = {}, variant = ArcButtonVariant.Outline) { Text("Long press or open menu") } },
                        actions = listOf(ArcContextAction("Action one") {}, ArcContextAction("Action two") {})
                    )
                    ArcColorPicker(Color(0xFF7747FF), onColorSelected = {})
                }
                GallerySection("Time, date, and input") {
                    ArcTimePicker(time.first, time.second, onTimeChange = { h, m -> time = h to m })
                    ArcDatePicker(date, onDateSelected = { date = it })
                    ArcDateRangePicker(range.first, range.second, onRangeSelected = { start, end -> range = start to end })
                    ArcShortcutRecorder(shortcut, onShortcutChange = { shortcut = it }, conflictingShortcuts = setOf("Ctrl+P"))
                    ArcElasticSlider(sliderValue, onValueChange = { sliderValue = it }, valueRange = 0f..100f, step = 2f, label = "Elastic slider")
                    ArcSignaturePad(
                        onSignatureChanged = {},
                        onSignature = { signature: ArcSignature -> exportSummary = "${signature.strokes.size} strokes · ${signature.toSvg().length} SVG chars" }
                    )
                    Text(exportSummary, color = ArcTheme.colors.textSecondary)
                }
                GallerySection("Cards, panels, and commands") {
                    ArcCardStack(cards, onDismissTop = { cards = cards.drop(1) }) { item ->
                        Surface(color = ArcTheme.colors.surfaceRaised, shape = ArcTheme.shapes.panel) {
                            Text(item, Modifier.padding(24.dp), color = ArcTheme.colors.foreground)
                        }
                    }
                    ArcCarousel(listOf("First", "Second", "Third")) { Text(it, color = ArcTheme.colors.foreground) }
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        ArcResizablePanels(
                            firstPane = { Text("Pane 1", Modifier.padding(16.dp), color = ArcTheme.colors.foreground) },
                            secondPane = { Text("Pane 2", Modifier.padding(16.dp), color = ArcTheme.colors.foreground) },
                            thirdPane = { Text("Pane 3", Modifier.padding(16.dp), color = ArcTheme.colors.foreground) },
                            collapsiblePanes = setOf(2),
                            onWeightsChanged = { panelWeights = it }
                        )
                    }
                    Text("Pane weights: ${panelWeights.joinToString { "%.2f".format(it) }}", color = ArcTheme.colors.textSecondary)
                    ArcButton(onClick = { paletteOpen = true }, variant = ArcButtonVariant.Outline) { Text("Open command palette") }
                    ArcCommandPalette(
                        isOpen = paletteOpen,
                        onClose = { paletteOpen = false },
                        actions = listOf(
                            ArcCommandAction("appearance", "Change appearance", "Theme", "Ctrl+T") { dark = !dark },
                            ArcCommandAction("reset", "Reset cards", "Gallery") { cards = listOf("First card", "Second card", "Third card") }
                        )
                    )
                }
                GallerySection("Data and charts") {
                    ArcSortableDataTable(
                        items = tableRows,
                        columns = tableColumns,
                        pageSize = 2,
                        rowKey = { it.id },
                        selectedItems = tableSelection,
                        onSelectionChanged = { tableSelection = it }
                    )
                    ArcBarChart(listOf(ArcBarData("A", 18f), ArcBarData("B", 34f), ArcBarData("C", 23f)))
                    ArcLineChart(listOf("Mon" to 8f, "Tue" to 14f, "Wed" to 11f, "Thu" to 19f))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GallerySection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = ArcTheme.colors.foreground)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ArcTheme.colors.surface,
            shape = ArcTheme.shapes.panel,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArcTheme.colors.borderSubtle)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
        }
    }
}
