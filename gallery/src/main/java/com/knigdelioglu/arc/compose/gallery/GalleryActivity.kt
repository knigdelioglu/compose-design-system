package com.knigdelioglu.arc.compose.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.advanced.ArcAdvancedItem
import com.knigdelioglu.arc.compose.advanced.ArcCoverFlow
import com.knigdelioglu.arc.compose.advanced.ArcCoverItem
import com.knigdelioglu.arc.compose.advanced.ArcDock
import com.knigdelioglu.arc.compose.advanced.ArcGlassTabBar
import com.knigdelioglu.arc.compose.advanced.ArcMorphNavigation
import com.knigdelioglu.arc.compose.advanced.ArcWalletCard
import com.knigdelioglu.arc.compose.advanced.ArcWalletStack
import com.knigdelioglu.arc.compose.controls.ArcBillingPeriod
import com.knigdelioglu.arc.compose.controls.ArcBillingPrice
import com.knigdelioglu.arc.compose.controls.ArcBillingToggle
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
import com.knigdelioglu.arc.compose.controls.ArcIconMorph
import com.knigdelioglu.arc.compose.controls.ArcInput
import com.knigdelioglu.arc.compose.controls.ArcMentionCandidate
import com.knigdelioglu.arc.compose.controls.ArcMentionInput
import com.knigdelioglu.arc.compose.controls.ArcMoneyInput
import com.knigdelioglu.arc.compose.controls.ArcMorphSelect
import com.knigdelioglu.arc.compose.controls.ArcMultiSelect
import com.knigdelioglu.arc.compose.controls.ArcOtpInput
import com.knigdelioglu.arc.compose.controls.ArcPasswordField
import com.knigdelioglu.arc.compose.controls.ArcPasswordStrength
import com.knigdelioglu.arc.compose.controls.ArcPhoneCountry
import com.knigdelioglu.arc.compose.controls.ArcPhoneInput
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
import com.knigdelioglu.arc.compose.datavis.ArcBrushChart
import com.knigdelioglu.arc.compose.datavis.ArcBrushPoint
import com.knigdelioglu.arc.compose.datavis.ArcLineChart
import com.knigdelioglu.arc.compose.datavis.ArcSortableDataTable
import com.knigdelioglu.arc.compose.datavis.ArcTableColumn
import com.knigdelioglu.arc.compose.display.ArcCountdown
import com.knigdelioglu.arc.compose.foundation.ArcAccent
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import com.knigdelioglu.arc.compose.navigation.ArcCardStack
import com.knigdelioglu.arc.compose.navigation.ArcCarousel
import com.knigdelioglu.arc.compose.navigation.ArcCommandAction
import com.knigdelioglu.arc.compose.navigation.ArcCommandPalette
import com.knigdelioglu.arc.compose.navigation.ArcResizablePanels
import java.util.Locale

private data class GalleryRow(val id: Int, val name: String, val amount: Int)

private enum class GalleryPage(val title: String) {
    Existing("Existing"),
    Free("Free"),
    Advanced("Advanced"),
    Charts("Charts")
}

class GalleryActivity : ComponentActivity() {
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
    var page by remember { mutableStateOf(GalleryPage.Existing) }

    ArcTheme(darkTheme = dark, accent = accent, reducedMotion = reducedMotion) {
        Surface(Modifier.fillMaxSize(), color = ArcTheme.colors.background) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Arc Compose · Component Gallery", style = MaterialTheme.typography.headlineSmall, color = ArcTheme.colors.foreground)
                    Text("Explore interactive controls, data views, and navigation components.", color = ArcTheme.colors.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ArcButton(onClick = { dark = !dark }, variant = ArcButtonVariant.Outline) {
                            Text(if (dark) "Switch to light" else "Switch to dark")
                        }
                        ArcButton(onClick = { reducedMotion = !reducedMotion }, variant = ArcButtonVariant.Outline) {
                            Text(if (reducedMotion) "Reduced motion on" else "Reduced motion off")
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ArcAccent.entries.forEach { choice ->
                            ArcButton(
                                onClick = { accent = choice },
                                variant = if (accent == choice) ArcButtonVariant.Primary else ArcButtonVariant.Outline
                            ) { Text(choice.name) }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GalleryPage.entries.forEach { choice ->
                        ArcButton(
                            onClick = { page = choice },
                            variant = if (page == choice) ArcButtonVariant.Primary else ArcButtonVariant.Outline
                        ) { Text(choice.title) }
                    }
                }
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    when (page) {
                        GalleryPage.Existing -> ExistingComponentsPage()
                        GalleryPage.Free -> FreeComponentsPage()
                        GalleryPage.Advanced -> AdvancedComponentsPage()
                        GalleryPage.Charts -> ChartsPage()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExistingComponentsPage() {
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
    var search by remember { mutableStateOf("") }
    var exportSummary by remember { mutableStateOf("Draw a signature to preview its SVG export.") }
    var selectedColor by remember { mutableStateOf(Color(0xFF7747FF)) }
    var lastAction by remember { mutableStateOf("Choose a control to try its interaction.") }
    var cards by remember { mutableStateOf(listOf("First card", "Second card", "Third card", "Fourth card")) }
    var panelWeights by remember { mutableStateOf(emptyList<Float>()) }
    var tableSelection by remember { mutableStateOf(emptySet<GalleryRow>()) }
    val rows = remember { listOf(GalleryRow(1, "One", 16), GalleryRow(2, "Two", 28), GalleryRow(3, "Three", 9)) }
    val columns = remember {
        listOf(
            ArcTableColumn<GalleryRow>("Name", comparator = compareBy { it.name }) { Text(it.name) },
            ArcTableColumn<GalleryRow>("Amount", comparator = compareBy { it.amount }) { Text(it.amount.toString()) }
        )
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GallerySection("Actions and selection") {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArcButton(onClick = { lastAction = "Primary action selected." }, variant = ArcButtonVariant.Primary) { Text("Primary") }
                ArcButton(onClick = { lastAction = "Outline action selected." }, variant = ArcButtonVariant.Outline) { Text("Outline") }
            }
            ArcHoldToConfirm(
                onConfirm = { lastAction = "Hold to confirm completed." },
                label = "Hold to confirm",
                holdDurationMillis = 800
            )
            ArcCheckbox(checked, onCheckedChange = { checked = it }, label = "Checkbox state")
            ArcSwitch(switched, onCheckedChange = { switched = it }, label = "Switch state")
            ArcRadioGroup(listOf("One", "Two", "Three"), radioChoice, onOptionSelected = { radioChoice = it })
            ArcRadioCards(
                options = listOf("Compact", "Comfortable"),
                selectedOption = cardChoice,
                onOptionSelected = { cardChoice = it },
                titleProvider = { it },
                descriptionProvider = { "Selectable option card" }
            )
            ArcSegmentedControl(
                items = listOf("Overview", "Details", "History"),
                selectedIndex = selectedIndex,
                onSelectIndex = { selectedIndex = it }
            )
            Text(lastAction, color = ArcTheme.colors.textSecondary)
        }
        GallerySection("Text input and selection fields") {
            ArcInput(value = search, onValueChange = { search = it }, label = "Search", placeholder = "Type to filter")
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
                content = { ArcButton(onClick = { lastAction = "Open the context menu with a long press." }, variant = ArcButtonVariant.Outline) { Text("Context menu") } },
                actions = listOf(
                    ArcContextAction("Action one") { lastAction = "Context action one selected." },
                    ArcContextAction("Action two") { lastAction = "Context action two selected." }
                )
            )
            ArcColorPicker(selectedColor, onColorSelected = { selectedColor = it })
            Text("Selected color: #%08X".format(selectedColor.value.toLong()), color = ArcTheme.colors.textSecondary)
        }
        GallerySection("Time, dates, keyboard, and signature") {
            ArcTimePicker(time.first, time.second, onTimeChange = { hour, minute -> time = hour to minute })
            ArcDatePicker(date, onDateSelected = { date = it })
            ArcDateRangePicker(range.first, range.second, onRangeSelected = { start, end -> range = start to end })
            ArcShortcutRecorder(shortcut, onShortcutChange = { shortcut = it }, conflictingShortcuts = setOf("Ctrl+P"))
            ArcElasticSlider(sliderValue, onValueChange = { sliderValue = it }, valueRange = 0f..100f, step = 2f, label = "Elastic slider")
            ArcSignaturePad(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                onSignatureChanged = {},
                onSignature = { signature: ArcSignature ->
                    exportSummary = "${signature.strokes.size} strokes · ${signature.toSvg().length} SVG characters"
                }
            )
            Text(exportSummary, color = ArcTheme.colors.textSecondary)
        }
        GallerySection("Navigation, panels, and commands") {
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
            Text("Pane weights: ${panelWeights.joinToString { "%.2f".format(Locale.ROOT, it) }}", color = ArcTheme.colors.textSecondary)
            ArcButton(onClick = { paletteOpen = true }, variant = ArcButtonVariant.Outline) { Text("Open command palette") }
            ArcCommandPalette(
                isOpen = paletteOpen,
                onClose = { paletteOpen = false },
                actions = listOf(
                    ArcCommandAction("appearance", "Change appearance", "Theme", "Ctrl+T") { lastAction = "Appearance action selected." },
                    ArcCommandAction("reset", "Reset cards", "Gallery") { cards = listOf("First card", "Second card", "Third card") }
                )
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FreeComponentsPage() {
    var otp by remember { mutableStateOf("") }
    var completion by remember { mutableStateOf("Enter a code to try paste and completion behavior.") }
    var password by remember { mutableStateOf("") }
    var strengthPassword by remember { mutableStateOf("") }
    var money by remember { mutableStateOf<Long?>(129900L) }
    var phone by remember { mutableStateOf("") }
    var e164 by remember { mutableStateOf<String?>(null) }
    val countries = remember {
        listOf(
            ArcPhoneCountry("US", "United States", "+1"),
            ArcPhoneCountry("GB", "United Kingdom", "+44"),
            ArcPhoneCountry("TR", "Türkiye", "+90")
        )
    }
    var country by remember { mutableStateOf(countries.first()) }
    var mention by remember { mutableStateOf(TextFieldValue("")) }
    var selectedMention by remember { mutableStateOf<String?>(null) }
    val candidates = remember {
        listOf(
            ArcMentionCandidate(id = "ada", displayName = "Ada Lovelace", token = "ada"),
            ArcMentionCandidate(id = "arc-team", displayName = "Arc Team", token = "arc-team"),
            ArcMentionCandidate(id = "design", displayName = "Design", token = "design", trigger = '#')
        )
    }
    var billingPeriod by remember { mutableStateOf(ArcBillingPeriod.Monthly) }
    var menuExpanded by remember { mutableStateOf(false) }
    var deadline by remember { mutableLongStateOf(System.currentTimeMillis() + 60_000L) }
    var countdownStatus by remember { mutableStateOf("Countdown is running.") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GallerySection("Verification and password inputs") {
            ArcOtpInput(
                value = otp,
                onValueChange = { otp = it },
                length = 6,
                label = "Verification code",
                onComplete = { completion = "Code complete: $it" },
                onDone = { completion = "Code submitted: $otp" }
            )
            Text(completion, color = ArcTheme.colors.textSecondary)
            ArcPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter a password"
            )
            ArcPasswordStrength(
                value = strengthPassword,
                onValueChange = { value, _ -> strengthPassword = value },
                minimumLength = 10
            )
        }
        GallerySection("Payment and contact inputs") {
            ArcMoneyInput(
                valueMinorUnits = money,
                onValueChange = { money = it },
                currencyCode = "USD",
                locale = Locale.US,
                label = "Amount"
            )
            ArcPhoneInput(
                value = phone,
                country = country,
                countries = countries,
                onValueChange = { national, formatted -> phone = national; e164 = formatted },
                onCountryChange = { country = it },
                label = "Phone number"
            )
            Text("E.164: ${e164 ?: "—"}", color = ArcTheme.colors.textSecondary)
            ArcMentionInput(
                value = mention,
                candidates = candidates,
                onValueChange = { mention = it },
                onMentionSelected = { candidate, _ -> selectedMention = candidate.displayName },
                label = "Message with mentions",
                placeholder = "Type @ or # to find a suggestion"
            )
            Text("Selected mention: ${selectedMention ?: "none"}", color = ArcTheme.colors.textSecondary)
        }
        GallerySection("Billing, icon, and time") {
            ArcBillingToggle(
                selectedPeriod = billingPeriod,
                onPeriodChange = { billingPeriod = it },
                savingsLabel = "Save 20%"
            )
            ArcBillingPrice(
                period = billingPeriod,
                monthlyPriceMinorUnits = 1200L,
                yearlyPriceMinorUnits = 11520L,
                currencyCode = "USD",
                locale = Locale.US
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArcIconMorph(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    contentDescription = "Toggle demo menu"
                )
                Text(if (menuExpanded) "Menu open" else "Menu closed", color = ArcTheme.colors.textSecondary)
            }
            ArcCountdown(deadlineMillis = deadline, onFinished = { countdownStatus = "Countdown finished." })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArcButton(
                    onClick = {
                        deadline = System.currentTimeMillis() + 60_000L
                        countdownStatus = "Countdown reset."
                    },
                    variant = ArcButtonVariant.Outline
                ) { Text("Reset countdown") }
                Text(countdownStatus, color = ArcTheme.colors.textSecondary)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AdvancedComponentsPage() {
    val destinations = remember {
        listOf(
            ArcAdvancedItem("home", "Home", active = true, icon = { Icon(Icons.Default.Home, contentDescription = null, tint = ArcTheme.colors.foreground) }),
            ArcAdvancedItem("search", "Search", icon = { Icon(Icons.Default.Search, contentDescription = null, tint = ArcTheme.colors.foreground) }),
            ArcAdvancedItem("profile", "Profile", badgeCount = 2, icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ArcTheme.colors.foreground) }),
            ArcAdvancedItem("settings", "Settings", icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = ArcTheme.colors.foreground) })
        )
    }
    val tabs = remember {
        listOf(
            ArcAdvancedItem("recent", "Recent", icon = { Icon(Icons.Default.Home, contentDescription = null, tint = ArcTheme.colors.foreground) }),
            ArcAdvancedItem("saved", "Saved", badgeCount = 4, icon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = ArcTheme.colors.foreground) }),
            ArcAdvancedItem("media", "Media", icon = { Icon(Icons.Default.Image, contentDescription = null, tint = ArcTheme.colors.foreground) })
        )
    }
    val covers = remember {
        listOf("Coast", "Archive", "Studio", "Garden", "Night")
            .mapIndexed { index, label -> ArcCoverItem("cover-$index", label, "$label photo") }
    }
    val walletCards = remember {
        listOf(
            ArcWalletCard("travel", "Travel", "Travel card ending in 2042"),
            ArcWalletCard("daily", "Daily", "Daily card ending in 7811"),
            ArcWalletCard("virtual", "Virtual", "Virtual card ending in 0904")
        )
    }
    var demo by remember { mutableStateOf("Dock") }
    var selectedDestination by remember { mutableStateOf("home") }
    var navigationExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("recent") }
    var selectedCover by remember { mutableStateOf<String?>(covers.first().key) }
    var selectedWallet by remember { mutableStateOf("travel") }
    var walletOrder by remember { mutableStateOf(walletCards.map { it.key }) }
    val orderedWalletCards = walletOrder.mapNotNull { key -> walletCards.firstOrNull { it.key == key } }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Try each component separately; selection is controlled by the gallery.", color = ArcTheme.colors.textSecondary)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Dock", "Morph Navigation", "Glass Tab Bar", "Wallet Stack", "Cover Flow").forEach { choice ->
                ArcButton(
                    onClick = { demo = choice },
                    variant = if (demo == choice) ArcButtonVariant.Primary else ArcButtonVariant.Outline
                ) { Text(choice) }
            }
        }
        GallerySection(demo) {
            when (demo) {
                "Dock" -> {
                    Text("Move a pointer over the dock or select an item.", color = ArcTheme.colors.textSecondary)
                    ArcDock(items = destinations, selectedKey = selectedDestination, onItemSelected = { selectedDestination = it })
                    Text("Selected: $selectedDestination", color = ArcTheme.colors.textSecondary)
                }
                "Morph Navigation" -> {
                    ArcMorphNavigation(
                        destinations = destinations,
                        selectedKey = selectedDestination,
                        onSelectedKeyChange = { selectedDestination = it },
                        expanded = navigationExpanded,
                        onExpandedChange = { navigationExpanded = it }
                    )
                    Text("Selected: $selectedDestination", color = ArcTheme.colors.textSecondary)
                }
                "Glass Tab Bar" -> {
                    ArcGlassTabBar(tabs = tabs, selectedKey = selectedTab, onTabSelected = { selectedTab = it })
                    Text("Selected tab: $selectedTab", color = ArcTheme.colors.textSecondary)
                }
                "Wallet Stack" -> {
                    ArcWalletStack(
                        cards = orderedWalletCards,
                        selectedKey = selectedWallet,
                        onSelectedKeyChange = { selectedWallet = it },
                        onOrderChange = { walletOrder = it },
                        cardContent = { card, focused ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(card.label, color = ArcTheme.colors.foreground, style = MaterialTheme.typography.titleLarge)
                                Text(if (focused) "Selected ·••• 2042" else card.contentDescription, color = ArcTheme.colors.textSecondary)
                                Text("Drag horizontally, use the arrow keys, or select a card.", color = ArcTheme.colors.textMuted)
                            }
                        }
                    )
                }
                else -> {
                    ArcCoverFlow(
                        items = covers,
                        selectedKey = selectedCover,
                        onSelectedKeyChange = { selectedCover = it },
                        itemContent = { item, focused ->
                            Column(
                                Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = ArcTheme.colors.textSecondary)
                                Text(item.label, color = ArcTheme.colors.foreground)
                                if (focused) Text("Selected", color = ArcTheme.colors.accent)
                            }
                        }
                    )
                    Text("Selected cover: $selectedCover", color = ArcTheme.colors.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun ChartsPage() {
    var chartRange by remember { mutableStateOf(0f..1f) }
    var tableSelection by remember { mutableStateOf(emptySet<GalleryRow>()) }
    val rows = remember { listOf(GalleryRow(1, "One", 16), GalleryRow(2, "Two", 28), GalleryRow(3, "Three", 9)) }
    val columns = remember {
        listOf(
            ArcTableColumn<GalleryRow>("Name", comparator = compareBy { it.name }) { Text(it.name) },
            ArcTableColumn<GalleryRow>("Amount", comparator = compareBy { it.amount }) { Text(it.amount.toString()) }
        )
    }
    val series = remember {
        listOf(
            ArcBrushPoint("Mon", 8f),
            ArcBrushPoint("Tue", 14f),
            ArcBrushPoint("Wed", 11f),
            ArcBrushPoint("Thu", 19f),
            ArcBrushPoint("Fri", 16f),
            ArcBrushPoint("Sat", 23f),
            ArcBrushPoint("Sun", 20f)
        )
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GallerySection("Tables and overview charts") {
            ArcSortableDataTable(
                items = rows,
                columns = columns,
                pageSize = 2,
                rowKey = { it.id },
                selectedItems = tableSelection,
                onSelectionChanged = { tableSelection = it }
            )
            ArcBarChart(listOf(ArcBarData("A", 18f), ArcBarData("B", 34f), ArcBarData("C", 23f)))
            ArcLineChart(listOf("Mon" to 8f, "Tue" to 14f, "Wed" to 11f, "Thu" to 19f))
        }
        GallerySection("Free interactive brush chart") {
            ArcBrushChart(
                data = series,
                selection = chartRange,
                onSelectionChange = { chartRange = it },
                label = "Weekly activity",
                onSelectionChangeFinished = {}
            )
        }
        Spacer(Modifier.height(16.dp))
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
            border = BorderStroke(1.dp, ArcTheme.colors.borderSubtle)
        ) {
            Column(
                Modifier.padding(ArcTheme.spacing.space4),
                verticalArrangement = Arrangement.spacedBy(ArcTheme.spacing.space3)
            ) { content() }
        }
    }
}
