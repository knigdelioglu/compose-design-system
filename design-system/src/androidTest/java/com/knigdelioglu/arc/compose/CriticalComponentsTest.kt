package com.knigdelioglu.arc.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import com.knigdelioglu.arc.compose.controls.ArcElasticSlider
import com.knigdelioglu.arc.compose.controls.ArcCheckbox
import com.knigdelioglu.arc.compose.controls.ArcCombobox
import com.knigdelioglu.arc.compose.controls.ArcContextAction
import com.knigdelioglu.arc.compose.controls.ArcContextMenu
import com.knigdelioglu.arc.compose.controls.ArcDateRangePicker
import com.knigdelioglu.arc.compose.controls.ArcHoldToConfirm
import com.knigdelioglu.arc.compose.controls.ArcMorphSelect
import com.knigdelioglu.arc.compose.controls.ArcMultiSelect
import com.knigdelioglu.arc.compose.controls.ArcRadioCards
import com.knigdelioglu.arc.compose.controls.ArcRadioGroup
import com.knigdelioglu.arc.compose.controls.ArcSignature
import com.knigdelioglu.arc.compose.controls.ArcSignaturePad
import com.knigdelioglu.arc.compose.controls.ArcSegmentedControl
import com.knigdelioglu.arc.compose.controls.ArcShortcutRecorder
import com.knigdelioglu.arc.compose.controls.ArcSwitch
import com.knigdelioglu.arc.compose.controls.ArcTimePicker
import com.knigdelioglu.arc.compose.controls.ArcColorPicker
import com.knigdelioglu.arc.compose.controls.ArcMoneyInput
import com.knigdelioglu.arc.compose.navigation.ArcCarousel
import com.knigdelioglu.arc.compose.datavis.ArcDonutChart
import com.knigdelioglu.arc.compose.datavis.ArcDonutSegment
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import com.knigdelioglu.arc.compose.navigation.ArcCardStack
import com.knigdelioglu.arc.compose.navigation.ArcResizablePanels
import com.knigdelioglu.arc.compose.navigation.ArcCommandAction
import com.knigdelioglu.arc.compose.navigation.ArcCommandPalette
import com.knigdelioglu.arc.compose.datavis.ArcSortableDataTable
import com.knigdelioglu.arc.compose.datavis.ArcTableColumn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.activity.ComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class CriticalComponentsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun timePickerChangesHourAndMinuteAndRespectsBounds() {
        var time by mutableStateOf(23 to 58)
        compose.setContent {
            ArcTheme {
                ArcTimePicker(time.first, time.second, onTimeChange = { hour, minute -> time = hour to minute })
            }
        }

        compose.onNodeWithContentDescription("Dakika artır").performClick()
        compose.onNodeWithContentDescription("Saat azalt").performClick()
        compose.runOnIdle { assertEquals(22 to 59, time) }
        compose.onNodeWithContentDescription("Saat artır").assertExists()
    }

    @Test
    fun shortcutRecorderCapturesAndCancelsKeyboardInput() {
        var recorded by mutableStateOf("Ctrl+K")
        compose.setContent {
            ArcTheme {
                ArcShortcutRecorder(recorded, onShortcutChange = { recorded = it }, modifier = Modifier.testTag("shortcut-recorder"))
            }
        }
        compose.onNodeWithTag("shortcut-recorder").performClick()
        compose.onNodeWithTag("shortcut-recorder").performKeyInput { pressKey(Key.Escape) }
        compose.runOnIdle { assertEquals("Ctrl+K", recorded) }

        compose.onNodeWithTag("shortcut-recorder").performClick()
        compose.onNodeWithTag("shortcut-recorder").performKeyInput { pressKey(Key.A) }
        compose.runOnIdle { assertEquals("A", recorded) }

        compose.onNodeWithTag("shortcut-recorder").performClick()
        compose.onNodeWithTag("shortcut-recorder").performKeyInput {
            withKeyDown(Key.CtrlLeft) { pressKey(Key.K) }
        }
        compose.runOnIdle { assertEquals("Ctrl+K", recorded) }
    }

    @Test
    fun shortcutConflictKeepsTheCurrentBinding() {
        var recorded by mutableStateOf("Ctrl+K")
        compose.setContent {
            ArcTheme {
                ArcShortcutRecorder(
                    shortcut = recorded,
                    onShortcutChange = { recorded = it },
                    conflictingShortcuts = setOf("A"),
                    modifier = Modifier.testTag("shortcut-recorder")
                )
            }
        }
        compose.onNodeWithTag("shortcut-recorder").performClick()
        compose.onNodeWithTag("shortcut-recorder").performKeyInput { pressKey(Key.A) }
        compose.runOnIdle { assertEquals("Ctrl+K", recorded) }
        compose.onNodeWithText("Bu kısayol kullanımda. Başka bir tuş deneyin.").assertExists()
    }

    @Test
    fun morphSelectOpensInPlaceAndReturnsTheChosenItem() {
        var selection by mutableStateOf<String?>(null)
        compose.setContent {
            ArcTheme {
                ArcMorphSelect(
                    selectedItem = selection,
                    items = listOf("Alpha", "Beta", "Gamma"),
                    onItemSelected = { selection = it }
                )
            }
        }
        compose.onNodeWithText("Seçiniz...").performClick()
        compose.onNodeWithText("Gamma").performClick()
        compose.runOnIdle { assertEquals("Gamma", selection) }
    }

    @Test
    fun morphSelectKeepsFocusAndSupportsKeyboardSelectionAndCancel() {
        var selection by mutableStateOf<String?>(null)
        compose.setContent {
            ArcTheme {
                ArcMorphSelect(
                    selectedItem = selection,
                    items = listOf("Alpha", "Beta", "Gamma"),
                    onItemSelected = { selection = it }
                )
            }
        }
        val select = compose.onNodeWithContentDescription("Seçiniz...")
        select.performClick()
        select.assertIsFocused()
        select.performKeyInput { pressKey(Key.Escape) }
        compose.runOnIdle { assertEquals(null, selection) }

        compose.onNodeWithContentDescription("Seçiniz...").performClick()
        compose.onNodeWithContentDescription("Seçiniz...").performKeyInput {
            pressKey(Key.DirectionDown)
            pressKey(Key.Enter)
        }
        compose.runOnIdle { assertEquals("Beta", selection) }
    }

    @Test
    fun comboboxSearchRetainsKeyboardSelectionSemantics() {
        var selection by mutableStateOf<String?>(null)
        compose.setContent {
            ArcTheme {
                ArcCombobox(
                    selectedItem = selection,
                    items = listOf("Alpha", "Beta", "Gamma"),
                    onItemSelected = { selection = it }
                )
            }
        }
        compose.onNodeWithText("Arayarak seçiniz...").performClick()
        compose.onNode(hasSetTextAction()).assertIsFocused()
        compose.onNode(hasSetTextAction()).performKeyInput {
            pressKey(Key.DirectionDown)
            pressKey(Key.Enter)
        }
        compose.runOnIdle { assertEquals("Beta", selection) }

        compose.onNodeWithText("Beta").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Gam")
        compose.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals("Gamma", selection) }
    }

    @Test
    fun contextMenuLongPressRunsItsAction() {
        var selected = false
        compose.setContent {
            ArcTheme {
                ArcContextMenu(
                    content = { Text("Hold for actions") },
                    actions = listOf(ArcContextAction("Run action") { selected = true })
                )
            }
        }
        compose.onNodeWithText("Hold for actions").performTouchInput { longClick() }
        compose.onNodeWithText("Run action").performClick()
        compose.runOnIdle { assertTrue(selected) }
    }

    @Test
    fun contextMenuSupportsKeyboardInvocationAndArrowSelection() {
        val selected = mutableListOf<String>()
        compose.setContent {
            ArcTheme {
                ArcContextMenu(
                    modifier = Modifier.testTag("context-menu-target"),
                    content = { Text("Keyboard context target") },
                    actions = listOf(
                        ArcContextAction("First context action") { selected += "first" },
                        ArcContextAction("Second context action") { selected += "second" }
                    )
                )
            }
        }
        compose.onNodeWithTag("context-menu-target").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithTag("context-menu-target").performKeyInput { pressKey(Key.Menu) }
        compose.onNodeWithText("First context action").assertExists()
        compose.onNodeWithContentDescription("Context menu actions").assertIsFocused()
        compose.onNodeWithContentDescription("Context menu actions").performKeyInput {
            pressKey(Key.DirectionDown)
            pressKey(Key.Enter)
        }
        compose.runOnIdle { assertEquals(listOf("second"), selected) }

        compose.onNodeWithTag("context-menu-target").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithTag("context-menu-target").performKeyInput { withKeyDown(Key.ShiftLeft) { pressKey(Key.F10) } }
        compose.onNodeWithContentDescription("Context menu actions").assertExists()
        compose.onNodeWithContentDescription("Context menu actions").performKeyInput { pressKey(Key.Escape) }
        compose.onNodeWithContentDescription("Context menu actions").assertDoesNotExist()
    }

    @Test
    fun sliderSupportsAccessibilityValueSetAndKeyboardStep() {
        var value by mutableStateOf(20f)
        var committed by mutableStateOf(Float.NaN)
        compose.setContent {
            ArcTheme {
                ArcElasticSlider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..100f,
                    step = 10f,
                    label = "Slider test",
                    onValueCommit = { committed = it }
                )
            }
        }
        compose.onNodeWithContentDescription("Slider test").performSemanticsAction(SemanticsActions.SetProgress) { it(50f) }
        compose.runOnIdle {
            assertEquals(50f, value)
            assertEquals(50f, committed)
        }
        compose.onNodeWithContentDescription("Slider test").performTouchInput { click() }
        compose.onNodeWithContentDescription("Slider test").performKeyInput { pressKey(Key.DirectionRight) }
        compose.runOnIdle {
            assertEquals(60f, value)
            assertEquals(60f, committed)
        }
        val sliderWidthPx = compose.onNodeWithContentDescription("Slider test").fetchSemanticsNode().size.width.toFloat()
        compose.onNodeWithContentDescription("Slider test").performTouchInput {
            down(Offset(12f, center.y))
            moveTo(Offset(sliderWidthPx * 0.8f, center.y))
            up()
        }
        compose.runOnIdle {
            assertEquals(value, committed)
            assertTrue(value > 60f)
        }
        compose.onNodeWithContentDescription("Slider test").performTouchInput {
            down(Offset(sliderWidthPx * 0.8f, center.y))
            moveTo(Offset(sliderWidthPx * 1.3f, center.y))
            up()
        }
        compose.runOnIdle {
            assertEquals(100f, value)
            assertEquals(100f, committed)
        }
    }

    @Test
    fun selectionControlsReportAndUpdateTheirControlledSelection() {
        var checked by mutableStateOf(false)
        var switched by mutableStateOf(false)
        var radio by mutableStateOf("One")
        var radioCard by mutableStateOf("Card A")
        var multi by mutableStateOf(setOf("Multi A"))
        var segment by mutableStateOf(0)
        compose.setContent {
            ArcTheme {
                androidx.compose.foundation.layout.Column {
                    ArcCheckbox(checked, { checked = it }, label = "Checkbox")
                    ArcSwitch(switched, { switched = it }, label = "Switch")
                    ArcRadioGroup(listOf("One", "Two"), radio, { radio = it })
                    ArcRadioCards(
                        options = listOf("Card A", "Card B"),
                        selectedOption = radioCard,
                        onOptionSelected = { radioCard = it },
                        titleProvider = { it }
                    )
                    ArcMultiSelect(
                        selectedItems = multi,
                        items = listOf("Multi A", "Multi B"),
                        onSelectionChanged = { multi = it },
                        searchable = true,
                        searchPlaceholder = "Ara çoklu seçim"
                    )
                    ArcSegmentedControl(listOf("First", "Second"), segment, { segment = it })
                }
            }
        }
        compose.onNodeWithText("Checkbox").performClick()
        compose.onNodeWithText("Switch").performClick()
        compose.onNodeWithText("Two").performClick()
        compose.onNodeWithText("Card B").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("B")
        compose.onNodeWithText("Multi B").performClick()
        compose.onNodeWithText("Second").performClick()
        compose.runOnIdle {
            assertTrue(checked)
            assertTrue(switched)
            assertEquals("Two", radio)
            assertEquals("Card B", radioCard)
            assertEquals(setOf("Multi A", "Multi B"), multi)
            assertEquals(1, segment)
        }
    }

    @Test
    fun dateRangeQuickPresetReturnsAnOrderedInclusiveRange() {
        var selected: Pair<String, String>? = null
        compose.setContent {
            ArcTheme {
                ArcDateRangePicker("", "", onRangeSelected = { start, end -> selected = start to end })
            }
        }
        compose.onNodeWithText("Tarih aralığı seçin").performClick()
        compose.onNodeWithText("Son 7 gün").performClick()
        compose.runOnIdle {
            val (startText, endText) = selected ?: error("Expected a selected date range")
            val start = LocalDate.parse(startText)
            val end = LocalDate.parse(endText)
            assertEquals(6L, java.time.temporal.ChronoUnit.DAYS.between(start, end))
            assertTrue(!end.isBefore(start))
        }
    }

    @Test
    fun signatureStrokeSupportsUndoRedoAndExport() {
        var signature: ArcSignature? = null
        var changed = false
        compose.setContent {
            ArcTheme {
                ArcSignaturePad(
                    onSignatureChanged = { changed = it },
                    modifier = Modifier.size(320.dp, 180.dp),
                    onSignature = { signature = it }
                )
            }
        }
        compose.onNodeWithContentDescription("İmza alanı. Kalem veya parmakla çizin; imzayı geri alın veya yineleyin.")
            .performTouchInput {
                down(Offset(30f, 40f))
                moveTo(Offset(150f, 100f))
                up()
            }
        compose.runOnIdle {
            assertTrue(changed)
            assertEquals(1, signature?.strokes?.size)
            assertTrue(signature!!.strokes.first().points.size >= 2)
            assertTrue(signature!!.strokes.first().points.all { it.x in 0f..1f && it.y in 0f..1f })
        }
        compose.onNodeWithContentDescription("Geri al").performClick()
        compose.runOnIdle {
            assertTrue(!changed)
            assertTrue(signature!!.isEmpty)
        }
        compose.onNodeWithContentDescription("Yinele").performClick()
        compose.runOnIdle {
            assertTrue(changed)
            assertEquals(1, signature?.strokes?.size)
        }
        compose.onNodeWithContentDescription("İmzayı dışa aktar").performClick()
        compose.runOnIdle {
            assertTrue(signature!!.toSvg().contains("<path"))
            assertTrue(signature!!.toPngBytes().isNotEmpty())
        }
    }

    @Test
    fun commandPaletteRunsAnActionAndCloses() {
        var open by mutableStateOf(true)
        var ran = false
        compose.setContent {
            ArcTheme {
                ArcCommandPalette(
                    isOpen = open,
                    onClose = { open = false },
                    actions = listOf(ArcCommandAction("run", "Run action", "Test") { ran = true })
                )
            }
        }
        compose.onNodeWithText("Run action").performClick()
        compose.runOnIdle {
            assertTrue(ran)
            assertTrue(!open)
        }
    }

    @Test
    fun commandPaletteKeyboardRunsTheActiveActionAndEscapeCloses() {
        var open by mutableStateOf(true)
        val executed = mutableListOf<String>()
        compose.setContent {
            ArcTheme {
                ArcCommandPalette(
                    isOpen = open,
                    onClose = { open = false },
                    actions = listOf(
                        ArcCommandAction("first", "First action", "Test") { executed += "first" },
                        ArcCommandAction("second", "Second action", "Test") { executed += "second" }
                    )
                )
            }
        }
        compose.onNode(hasSetTextAction()).assertIsFocused()
        compose.onNode(hasSetTextAction()).performKeyInput {
            pressKey(Key.DirectionDown)
            pressKey(Key.Enter)
        }
        compose.runOnIdle {
            assertEquals(listOf("second"), executed)
            assertTrue(!open)
            open = true
        }
        compose.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Escape) }
        compose.runOnIdle { assertTrue(!open) }
    }

    @Test
    fun commandPaletteRestoresFocusToItsInvokerAfterEscape() {
        val requester = FocusRequester()
        var open by mutableStateOf(false)
        compose.setContent {
            ArcTheme {
                Box(
                    modifier = Modifier
                        .testTag("palette-invoker")
                        .focusRequester(requester)
                        .focusable()
                        .clickable { open = true }
                ) { Text("Open command palette") }
                ArcCommandPalette(
                    isOpen = open,
                    onClose = { open = false },
                    actions = listOf(ArcCommandAction("noop", "No operation", "Test") {}),
                    focusRestoreRequester = requester
                )
            }
        }
        compose.runOnIdle { requester.requestFocus() }
        compose.onNodeWithTag("palette-invoker").assertIsFocused()
        compose.onNodeWithTag("palette-invoker").performClick()
        compose.onNode(hasSetTextAction()).assertIsFocused()
        compose.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Escape) }
        compose.runOnIdle { assertTrue(!open) }
        compose.onNodeWithTag("palette-invoker").assertIsFocused()
    }

    @Test
    fun sortableTableKeepsControlledSelectionAcrossPages() {
        data class Row(val id: Int, val name: String)
        val rows = listOf(Row(1, "Zulu"), Row(2, "Amy"), Row(3, "Beta"))
        var selected by mutableStateOf(emptySet<Row>())
        compose.setContent {
            ArcTheme {
                ArcSortableDataTable(
                    items = rows,
                    columns = listOf(ArcTableColumn("Name", comparator = compareBy { it.name }) { Text(it.name) }),
                    pageSize = 2,
                    rowKey = { it.id },
                    selectedItems = selected,
                    onSelectionChanged = { selected = it }
                )
            }
        }
        compose.onNodeWithText("Name").performClick()
        val amyTop = compose.onNodeWithText("Amy").fetchSemanticsNode().boundsInRoot.top
        val betaTop = compose.onNodeWithText("Beta").fetchSemanticsNode().boundsInRoot.top
        assertTrue(amyTop < betaTop)
        compose.onNodeWithContentDescription("2 satırını seç").performClick()
        compose.runOnIdle { assertTrue(selected.any { it.id == 2 }) }
        compose.onNodeWithContentDescription("Sayfadaki tüm satırları seç").performClick()
        compose.runOnIdle { assertEquals(setOf(2, 3), selected.map { it.id }.toSet()) }
        compose.onNodeWithContentDescription("Sonraki Sayfa").performClick()
        compose.onNodeWithText("Zulu").assertExists()
        compose.runOnIdle { assertEquals(setOf(2, 3), selected.map { it.id }.toSet()) }
    }

    @Test
    fun sortableTableDropsSortWhenTheSortedColumnIsRemovedFromDynamicSchema() {
        data class Row(val id: Int, val name: String)
        val rows = listOf(Row(3, "Zulu"), Row(1, "Amy"), Row(2, "Beta"))
        var columns by mutableStateOf<List<ArcTableColumn<Row>>>(
            listOf(ArcTableColumn("Name", comparator = compareBy { it.name }) { Text(it.name) })
        )
        compose.setContent {
            ArcTheme {
                ArcSortableDataTable(
                    items = rows,
                    columns = columns,
                    rowKey = { it.id }
                )
            }
        }
        compose.onNodeWithText("Name").performClick()
        assertTrue(
            compose.onNodeWithText("Amy").fetchSemanticsNode().boundsInRoot.top <
                compose.onNodeWithText("Beta").fetchSemanticsNode().boundsInRoot.top
        )
        compose.runOnIdle {
            columns = listOf(ArcTableColumn("ID", comparator = compareBy { it.id }) { Text(it.id.toString()) })
        }
        assertTrue(
            compose.onNodeWithText("3").fetchSemanticsNode().boundsInRoot.top <
                compose.onNodeWithText("1").fetchSemanticsNode().boundsInRoot.top
        )
    }

    @Test
    fun panelCollapseAndRestorePreservesCombinedWeight() {
        var weights by mutableStateOf(emptyList<Float>())
        compose.setContent {
            ArcTheme {
                ArcResizablePanels(
                    modifier = Modifier.size(520.dp, 320.dp),
                    firstPane = { Box(Modifier) },
                    secondPane = { Box(Modifier) },
                    collapsiblePanes = setOf(1),
                    onWeightsChanged = { weights = it }
                )
            }
        }
        val divider = compose.onNodeWithContentDescription("Panel boyutlandırma")
        compose.onRoot().performKeyInput { pressKey(Key.Tab) }
        divider.performKeyInput { pressKey(Key.Spacebar) }
        compose.waitForIdle()
        compose.runOnIdle {
            assertTrue(weights[1] <= 0.05f)
            assertEquals(1f, weights.sum(), 0.0001f)
        }
        divider.performKeyInput { pressKey(Key.Spacebar) }
        compose.waitForIdle()
        compose.runOnIdle {
            assertTrue(weights[1] >= 0.18f)
            assertEquals(1f, weights.sum(), 0.0001f)
        }
    }

    @Test
    fun holdControlDoesNotConfirmOnShortTapAndAccessibleConfirmationRunsOnce() {
        var confirmations = 0
        compose.setContent {
            ArcTheme(reducedMotion = true) {
                ArcHoldToConfirm(onConfirm = { confirmations++ }, label = "Hold to confirm", holdDurationMillis = 300)
            }
        }
        compose.onNodeWithText("Hold to confirm").performTouchInput { click() }
        compose.runOnIdle { assertEquals(0, confirmations) }

        compose.onNodeWithContentDescription("Hold to confirm").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeBy(350)
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, confirmations) }
        compose.onNodeWithContentDescription("Tamamlandı").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.runOnIdle { assertEquals(1, confirmations) }
    }

    @Test
    fun holdControlCancelsShortKeyboardPressAndCompletesAFullKeyboardHold() {
        var confirmations = 0
        compose.setContent {
            ArcTheme(reducedMotion = true) {
                ArcHoldToConfirm(onConfirm = { confirmations++ }, label = "Keyboard hold", holdDurationMillis = 300)
            }
        }
        val control = compose.onNodeWithContentDescription("Keyboard hold")
        control.performTouchInput { click() }
        control.performKeyInput {
            keyDown(Key.Enter)
            pressKey(Key.Escape)
            keyUp(Key.Enter)
        }
        compose.mainClock.advanceTimeBy(350)
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(0, confirmations) }

        control.performKeyInput {
            withKeyDown(Key.Spacebar) {
                compose.mainClock.advanceTimeBy(350)
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, confirmations) }
    }

    @Test
    fun panelKeyboardResizePreservesTotalWeight() {
        var weights by mutableStateOf(emptyList<Float>())
        compose.setContent {
            ArcTheme {
                ArcResizablePanels(
                    modifier = Modifier.size(520.dp, 340.dp),
                    firstPane = { Box(Modifier) },
                    secondPane = { Box(Modifier) },
                    onWeightsChanged = { weights = it }
                )
            }
        }
        compose.onRoot().performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithContentDescription("Panel boyutlandırma").performKeyInput { pressKey(Key.DirectionDown) }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(2, weights.size)
            assertEquals(1f, weights.sum(), 0.0001f)
            assertTrue(weights[0] > 0.4f)
        }
    }

    @Test
    fun aSingleCardCanBeDismissedByDraggingItAway() {
        var dismissed: String? = null
        compose.setContent {
            ArcTheme {
                ArcCardStack(
                    items = listOf("Only card"),
                    modifier = Modifier.size(240.dp, 160.dp),
                    onDismissTop = { dismissed = it }
                ) { Text(it) }
            }
        }
        compose.onNodeWithContentDescription("Üst kart: Only card").performTouchInput { swipeRight() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals("Only card", dismissed)
        }
    }

    @Test
    fun stackHandlesEmptyAndMoreThanThreeCards() {
        var dismissed: String? = null
        var populated by mutableStateOf(false)
        compose.setContent {
            ArcTheme {
                ArcCardStack(
                    items = if (populated) listOf("top", "second", "third", "not visible") else emptyList(),
                    modifier = Modifier.size(240.dp, 160.dp),
                    onDismissTop = { dismissed = it }
                ) { Text(it) }
            }
        }
        compose.onNodeWithText("anything").assertDoesNotExist()
        compose.runOnIdle { populated = true }
        compose.onNodeWithText("top").assertExists()
        compose.onNodeWithText("second").assertExists()
        compose.onNodeWithText("third").assertExists()
        compose.onNodeWithText("not visible").assertDoesNotExist()
        compose.onNodeWithContentDescription("Üst kart: top").performTouchInput { swipeRight() }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals("top", dismissed) }
    }
    @Test
    fun malformedCustomSignatureExportsDoNotContainNonFiniteCoordinates() {
        val signature = ArcSignature(
            strokes = listOf(
                com.knigdelioglu.arc.compose.controls.ArcSignatureStroke(
                    points = listOf(
                        com.knigdelioglu.arc.compose.controls.ArcSignaturePoint(
                            Float.NaN, Float.POSITIVE_INFINITY, pressure = Float.NaN
                        ),
                        com.knigdelioglu.arc.compose.controls.ArcSignaturePoint(
                            0.5f, 0.75f, pressure = Float.NEGATIVE_INFINITY
                        )
                    ),
                    color = Color.Blue,
                    widthPx = 8f
                )
            ),
            canvasWidthPx = 200,
            canvasHeightPx = 200
        )
        val svg = signature.toSvg()
        assertTrue(!svg.contains("NaN"))
        assertTrue(!svg.contains("Infinity"))
        assertTrue(signature.toPngBytes().isNotEmpty())
    }

    @Test
    fun signatureSvgRetainsPressureVariationAndOpacity() {
        val signature = ArcSignature(
            strokes = listOf(
                com.knigdelioglu.arc.compose.controls.ArcSignatureStroke(
                    points = listOf(
                        com.knigdelioglu.arc.compose.controls.ArcSignaturePoint(0f, 0f, pressure = 0.25f),
                        com.knigdelioglu.arc.compose.controls.ArcSignaturePoint(0.5f, 0.5f, pressure = 0.25f),
                        com.knigdelioglu.arc.compose.controls.ArcSignaturePoint(1f, 1f, pressure = 1f)
                    ),
                    color = Color.Red.copy(alpha = 0.5f),
                    widthPx = 8f
                )
            ),
            canvasWidthPx = 200,
            canvasHeightPx = 200
        )
        val svg = signature.toSvg()
        val paths = svg.split("<path").drop(1)
        assertEquals(2, paths.size)
        fun attribute(path: String, name: String): Float =
            path.substringAfter("$name=\"").substringBefore("\"").toFloat()
        assertEquals(2f, attribute(paths[0], "stroke-width"), 0.01f)
        assertEquals(5f, attribute(paths[1], "stroke-width"), 0.01f)
        assertEquals(0.5f, attribute(paths[0], "stroke-opacity"), 0.01f)
    }

    @Test
    fun donutChartSanitizesInvalidValuesWithoutCrashingOrShowingGarbage() {
        compose.setContent {
            ArcTheme {
                ArcDonutChart(
                    listOf(
                        ArcDonutSegment("Nan", Float.NaN, Color.Red),
                        ArcDonutSegment("Infinite", Float.POSITIVE_INFINITY, Color.Yellow),
                        ArcDonutSegment("Negative", -5f, Color.Blue),
                        ArcDonutSegment("Valid", 12f, Color.Green)
                    )
                )
            }
        }
        compose.onNodeWithText("Nan: 0").assertExists()
        compose.onNodeWithText("Infinite: 0").assertExists()
        compose.onNodeWithText("Negative: 0").assertExists()
        compose.onNodeWithText("Valid: 12").assertExists()
    }

    @Test
    fun colorPickerExposesSelectableAccessibleColorTargets() {
        var color by mutableStateOf(Color.Red)
        compose.setContent {
            ArcTheme {
                ArcColorPicker(
                    selectedColor = color,
                    colors = listOf(Color.Red, Color.Blue),
                    onColorSelected = { color = it }
                )
            }
        }
        compose.onNodeWithContentDescription("Renk 2").performClick()
        compose.runOnIdle { assertEquals(Color.Blue, color) }
    }

    @Test
    fun invalidMoneyPasteDoesNotOverwriteTheOwnedAmount() {
        var amount by mutableStateOf<Long?>(123450L)
        compose.setContent {
            ArcTheme {
                ArcMoneyInput(
                    valueMinorUnits = amount,
                    onValueChange = { amount = it },
                    label = "Amount",
                    locale = java.util.Locale.US
                )
            }
        }
        compose.onNodeWithContentDescription("Amount").performTextReplacement("12abc34")
        compose.runOnIdle { assertEquals(123450L, amount) }
        compose.onNodeWithText("Enter a valid amount with up to 2 decimal places").assertExists()
        compose.onNodeWithContentDescription("Amount").performTextReplacement("12.34")
        compose.runOnIdle { assertEquals(1234L, amount) }
        compose.onNodeWithText("Enter a valid amount with up to 2 decimal places").assertDoesNotExist()
    }

    @Test
    fun carouselRestoresSelectedPageAfterSavedStateRecreation() {
        val restorer = StateRestorationTester(compose)
        restorer.setContent {
            ArcTheme {
                ArcCarousel(items = listOf("Bir", "İki", "Üç")) { Text(it) }
            }
        }
        compose.onNodeWithContentDescription("Sonraki").performClick()
        compose.onNodeWithText("İki").assertExists()
        restorer.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("İki").assertExists()
    }

    @Test
    fun resizedPanelWeightsSurviveSaveAndRestore() {
        val restorer = StateRestorationTester(compose)
        var weights by mutableStateOf(emptyList<Float>())
        restorer.setContent {
            ArcTheme {
                ArcResizablePanels(
                    modifier = Modifier.size(520.dp, 340.dp),
                    firstPane = { Box(Modifier) },
                    secondPane = { Box(Modifier) },
                    onWeightsChanged = { weights = it }
                )
            }
        }
        compose.onRoot().performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithContentDescription("Panel boyutlandırma")
            .performKeyInput { pressKey(Key.DirectionDown) }
        compose.waitForIdle()
        val adjusted = weights.first()
        assertTrue(adjusted > 0.4f)
        restorer.emulateSavedInstanceStateRestore()
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(adjusted, weights.first(), 0.0001f) }
    }

    @Test
    fun carouselNavigationRemainsFunctionalWithChangedItemCount() {
        var items by mutableStateOf(listOf("Bir", "İki", "Üç"))
        compose.setContent {
            ArcTheme {
                ArcCarousel(items = items) { Text(it) }
            }
        }
        compose.onNodeWithContentDescription("Sonraki").performClick()
        compose.onNodeWithContentDescription("Sonraki").performClick()
        compose.onNodeWithText("Üç").assertExists()
        compose.runOnIdle { items = listOf("Bir") }
        compose.onNodeWithText("Bir").assertExists()
    }

}
