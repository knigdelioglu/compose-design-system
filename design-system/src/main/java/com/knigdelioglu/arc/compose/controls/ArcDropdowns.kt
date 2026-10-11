package com.knigdelioglu.arc.compose.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.focusable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import com.knigdelioglu.arc.compose.foundation.ArcMotion

/**
 * Arc Select: standard selection trigger with dropdown.
 */
@Composable
fun <T> ArcSelect(
    selectedItem: T?,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seçiniz...",
    labelProvider: (T) -> String = { it?.toString() ?: "" }
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = ArcTheme.colors

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = ArcTheme.shapes.control,
            color = colors.surface,
            border = BorderStroke(1.dp, if (expanded) colors.accent else colors.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedItem != null) labelProvider(selectedItem) else placeholder,
                    fontSize = 15.sp,
                    color = if (selectedItem != null) colors.foreground else colors.textMuted
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = colors.textMuted
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                DropdownMenuItem(
                    text = {
                        Text(
                            text = labelProvider(item),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = colors.foreground
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    trailingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = colors.accent) }
                    } else null
                )
            }
        }
    }
}

/**
 * Arc Combobox: searchable selection list for large option sets.
 */
@Composable
fun <T> ArcCombobox(
    selectedItem: T?,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Arayarak seçiniz...",
    searchQueryFilter: (T, String) -> Boolean = { item, query -> item.toString().contains(query, ignoreCase = true) },
    labelProvider: (T) -> String = { it?.toString() ?: "" },
    enabled: Boolean = true
) {
    var isOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val colors = ArcTheme.colors
    val motion = ArcMotion.current

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { query = ""; isOpen = true },
            shape = ArcTheme.shapes.control,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedItem != null) labelProvider(selectedItem) else placeholder,
                    fontSize = 15.sp,
                    color = if (selectedItem != null) colors.foreground else colors.textMuted
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.textMuted)
            }
        }

        if (isOpen) {
            val filtered = remember(query, items, searchQueryFilter) {
                if (query.isBlank()) items else items.filter { searchQueryFilter(it, query) }
            }
            // Caller-owned lists are often rebuilt during recomposition. Key navigation state
            // by the displayed labels so an arrow press is not reset by a fresh list instance.
            val filteredLabels = filtered.map(labelProvider)
            val selectedLabel = selectedItem?.let(labelProvider)
            var activeIndex by remember(query, filteredLabels, selectedLabel) {
                mutableStateOf(filtered.indexOfFirst { labelProvider(it) == selectedLabel }.coerceAtLeast(0))
            }
            val searchFocusRequester = remember { FocusRequester() }
            val listState = rememberLazyListState()
            androidx.compose.runtime.LaunchedEffect(activeIndex, filtered.size) {
                if (filtered.isNotEmpty()) {
                    val index = activeIndex.coerceIn(filtered.indices)
                    if (motion.reducedMotion) listState.scrollToItem(index) else listState.animateScrollToItem(index)
                }
            }
            val searchInputModifier = Modifier
                .focusRequester(searchFocusRequester)
                .onPreviewKeyEvent { event ->
                    if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionDown -> {
                            if (filtered.isNotEmpty()) activeIndex = (activeIndex + 1).coerceAtMost(filtered.lastIndex)
                            true
                        }
                        Key.DirectionUp -> {
                            if (filtered.isNotEmpty()) activeIndex = (activeIndex - 1).coerceAtLeast(0)
                            true
                        }
                        Key.Enter, Key.NumPadEnter -> {
                            filtered.getOrNull(activeIndex.coerceIn(0, filtered.lastIndex.coerceAtLeast(0)))?.let {
                                onItemSelected(it)
                                isOpen = false
                            }
                            true
                        }
                        Key.Escape -> { isOpen = false; true }
                        else -> false
                    }
                }
            Dialog(onDismissRequest = { isOpen = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .heightIn(max = 480.dp)
                        .onPreviewKeyEvent { event ->
                            if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                            when (event.key) {
                                Key.DirectionDown -> {
                                    if (filtered.isNotEmpty()) activeIndex = (activeIndex + 1).coerceAtMost(filtered.lastIndex)
                                    true
                                }
                                Key.DirectionUp -> {
                                    if (filtered.isNotEmpty()) activeIndex = (activeIndex - 1).coerceAtLeast(0)
                                    true
                                }
                                Key.Enter, Key.NumPadEnter -> {
                                    filtered.getOrNull(activeIndex.coerceIn(0, filtered.lastIndex.coerceAtLeast(0)))?.let {
                                        onItemSelected(it)
                                        isOpen = false
                                    }
                                    true
                                }
                                Key.Escape -> { isOpen = false; true }
                                else -> false
                            }
                        },
                    shape = ArcTheme.shapes.panel,
                    color = colors.surfaceRaised,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        androidx.compose.runtime.LaunchedEffect(searchFocusRequester) {
                            searchFocusRequester.requestFocus()
                        }
                        ArcSearchField(
                            query = query,
                            onQueryChange = { query = it },
                            inputModifier = searchInputModifier,
                            placeholder = "Ara..."
                        )
                        Spacer(Modifier.height(12.dp))
                        val safeActiveIndex = activeIndex.coerceIn(0, filtered.lastIndex.coerceAtLeast(0))
                        LazyColumn(state = listState, modifier = Modifier.weight(1f, fill = false)) {
                            itemsIndexed(filtered) { index, item ->
                                val isSelected = item == selectedItem
                                val isActive = index == safeActiveIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(ArcTheme.shapes.control)
                                        .background(if (isActive) colors.accentSubtle else Color.Transparent)
                                        .clickable(enabled = enabled) {
                                            onItemSelected(item)
                                            isOpen = false
                                        }
                                        .semantics {
                                            selected = isSelected
                                            if (isActive) stateDescription = "Etkin sonuç"
                                        }
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = labelProvider(item),
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = colors.foreground
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = colors.accent)
                                    }
                                }
                            }
                            if (filtered.isEmpty()) {
                                item {
                                    Text("Sonuç bulunamadı", color = colors.textMuted, modifier = Modifier.padding(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Arc Multi-Select: selection of multiple options, filters, or groups.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ArcMultiSelect(
    selectedItems: Set<T>,
    items: List<T>,
    onSelectionChanged: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String = { it.toString() },
    searchable: Boolean = items.size > 8,
    searchPlaceholder: String = "Seçenek ara...",
    searchFilter: (T, String) -> Boolean = { item, query -> labelProvider(item).contains(query, ignoreCase = true) },
    enabled: Boolean = true
) {
    var query by remember { mutableStateOf("") }
    val filteredItems = remember(items, query, searchFilter) {
        if (query.isBlank()) items else items.filter { searchFilter(it, query) }
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (searchable) {
            ArcSearchField(query = query, onQueryChange = { query = it }, placeholder = searchPlaceholder)
        }
        if (filteredItems.isEmpty()) {
            Text("Eşleşen seçenek yok", color = ArcTheme.colors.textMuted, modifier = Modifier.padding(8.dp))
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredItems.forEach { item ->
                    val isSelected = selectedItems.contains(item)
                    val colors = ArcTheme.colors
                    Surface(
                        modifier = Modifier
                            .clip(ArcTheme.shapes.pill)
                            .toggleable(value = isSelected, enabled = enabled, role = Role.Checkbox) { checked ->
                                onSelectionChanged(if (checked) selectedItems + item else selectedItems - item)
                            },
                        shape = ArcTheme.shapes.pill,
                        color = if (isSelected) colors.accent else colors.surfaceRaised,
                        border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = colors.accentForeground, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = labelProvider(item),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) colors.accentForeground else colors.foreground
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Arc Morph Select: interactive dropdown variant with fluid expanded card container.
 */
@Composable
fun <T> ArcMorphSelect(
    selectedItem: T?,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String = { it?.toString() ?: "" },
    placeholder: String = "Seçiniz...",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var activeIndex by remember(items, selectedItem) {
        mutableStateOf(items.indexOf(selectedItem).takeIf { it >= 0 } ?: 0)
    }
    var typeAhead by remember { mutableStateOf("") }
    var lastTypeAheadTime by remember { mutableStateOf(0L) }
    val focusRequester = remember { FocusRequester() }
    val colors = ArcTheme.colors
    val motion = ArcMotion.current

    androidx.compose.runtime.LaunchedEffect(expanded) {
        if (expanded && enabled) focusRequester.requestFocus()
        if (!expanded) typeAhead = ""
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(motion.springMorph())
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (!enabled || event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionDown -> {
                        if (!expanded) expanded = true
                        else if (items.isNotEmpty()) activeIndex = (activeIndex + 1).coerceAtMost(items.lastIndex)
                        true
                    }
                    Key.DirectionUp -> {
                        if (!expanded) expanded = true
                        else if (items.isNotEmpty()) activeIndex = (activeIndex - 1).coerceAtLeast(0)
                        true
                    }
                    Key.Enter, Key.NumPadEnter -> {
                        if (!expanded) expanded = true
                        else items.getOrNull(activeIndex)?.let {
                            onItemSelected(it)
                            expanded = false
                        }
                        true
                    }
                    Key.Escape -> {
                        expanded = false
                        true
                    }
                    else -> {
                        val native = event.nativeKeyEvent
                        if (expanded && event.type == KeyEventType.KeyDown && native.unicodeChar != 0) {
                            val char = native.unicodeChar.toChar()
                            if (!char.isISOControl()) {
                                typeAhead = if (native.eventTime - lastTypeAheadTime > 700L) char.toString()
                                else typeAhead + char
                                lastTypeAheadTime = native.eventTime
                                val match = items.indexOfFirst { labelProvider(it).startsWith(typeAhead, ignoreCase = true) }
                                if (match >= 0) activeIndex = match
                                true
                            } else false
                        } else false
                    }
                }
            }
            .semantics {
                contentDescription = selectedItem?.let(labelProvider) ?: placeholder
                stateDescription = if (expanded) "Açık, ${items.size} seçenek" else "Kapalı"
            },
        shape = if (expanded) ArcTheme.shapes.panel else ArcTheme.shapes.control,
        color = colors.surfaceRaised,
        border = BorderStroke(1.dp, if (expanded) colors.accent else colors.border)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        if (expanded) expanded = false
                        else {
                            activeIndex = items.indexOf(selectedItem).takeIf { it >= 0 } ?: 0
                            expanded = true
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedItem?.let(labelProvider) ?: placeholder,
                    fontSize = 15.sp,
                    fontWeight = if (selectedItem == null) FontWeight.Normal else FontWeight.Medium,
                    color = if (selectedItem == null) colors.textMuted else colors.foreground
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.textMuted)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = motion.standardTween()) + fadeIn(animationSpec = motion.fastTween()),
                exit = shrinkVertically(animationSpec = motion.standardTween()) + fadeOut(animationSpec = motion.fastTween())
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        val selected = item == selectedItem
                        val active = index == activeIndex
                        val itemColor by animateColorAsState(
                            targetValue = when {
                                selected -> colors.accentSubtle
                                active -> colors.surfaceMuted
                                else -> Color.Transparent
                            },
                            animationSpec = motion.fastTween(),
                            label = "arcMorphOption"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ArcTheme.shapes.control)
                                .background(itemColor)
                                .clickable(enabled = enabled) {
                                    onItemSelected(item)
                                    expanded = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = labelProvider(item),
                                color = colors.foreground,
                                fontWeight = if (selected || active) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (selected) Icon(Icons.Default.Check, contentDescription = "Seçili", tint = colors.accent)
                        }
                    }
                    if (items.isEmpty()) {
                        item {
                            Text("Seçenek yok", color = colors.textMuted, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Arc Dropdown Menu wrapper: custom styled contextual popover menu.
 */
@Composable
fun ArcDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(ArcTheme.colors.surfaceRaised)
    ) {
        content()
    }
}

/** Context actions are real callbacks; clicking one closes the menu before it runs. */
data class ArcContextAction(val label: String, val onSelect: () -> Unit)

/**
 * Arc Context Menu: long press an item on tablets to access its real actions.
 * The original composable content API remains available for the gallery.
 */
@Composable
fun ArcContextMenu(
    menuContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    actions: List<ArcContextAction> = emptyList()
) {
    var expanded by remember { mutableStateOf(false) }
    val actionLabels = actions.map(ArcContextAction::label)
    var activeIndex by remember(actionLabels) { mutableStateOf(0) }
    val menuFocusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val native = event.nativeKeyEvent
                if (event.key == Key.Menu || (event.key == Key.F10 && native.isShiftPressed)) {
                    activeIndex = 0
                    expanded = true
                    true
                } else false
            }
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Bağlam menüsünü aç") {
                        expanded = true
                        true
                    }
                )
            }
            .pointerInput(Unit) {
            detectTapGestures(onLongPress = { expanded = true })
            }
    ) {
        content()
        ArcDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier
                    .semantics { contentDescription = "Context menu actions" }
                    .focusRequester(menuFocusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.DirectionDown -> {
                                if (actions.isEmpty()) false else {
                                    activeIndex = (activeIndex + 1).coerceAtMost(actions.lastIndex)
                                    true
                                }
                            }
                            Key.DirectionUp -> {
                                if (actions.isEmpty()) false else {
                                    activeIndex = (activeIndex - 1).coerceAtLeast(0)
                                    true
                                }
                            }
                            Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                                actions.getOrNull(activeIndex)?.let { action ->
                                    expanded = false
                                    action.onSelect()
                                }
                                actions.isNotEmpty()
                            }
                            Key.Escape -> {
                                expanded = false
                                true
                            }
                            else -> false
                        }
                    }
            ) {
                androidx.compose.runtime.LaunchedEffect(menuFocusRequester) {
                    menuFocusRequester.requestFocus()
                }
                actions.forEachIndexed { index, action ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .background(if (activeIndex == index) ArcTheme.colors.accentSubtle else Color.Transparent)
                            .semantics { selected = activeIndex == index },
                        text = { Text(action.label) },
                        onClick = {
                            expanded = false
                            action.onSelect()
                        }
                    )
                }
                menuContent()
            }
        }
    }
}
