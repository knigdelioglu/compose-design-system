package com.knigdelioglu.arc.compose.advanced.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.knigdelioglu.arc.compose.advanced.ArcAdvancedItem
import com.knigdelioglu.arc.compose.advanced.ArcCoverFlow
import com.knigdelioglu.arc.compose.advanced.ArcCoverItem
import com.knigdelioglu.arc.compose.advanced.ArcDock
import com.knigdelioglu.arc.compose.advanced.ArcGlassTabBar
import com.knigdelioglu.arc.compose.advanced.ArcMorphNavigation
import com.knigdelioglu.arc.compose.advanced.ArcWalletCard
import com.knigdelioglu.arc.compose.advanced.ArcWalletStack
import com.knigdelioglu.arc.compose.foundation.ArcTheme

/** A compact, state-hoisted showcase that can be copied into an app screen. */
@Composable
fun AdvancedComponentsExample(modifier: Modifier = Modifier) {
    val destinations = remember {
        listOf(
            ArcAdvancedItem("home", "Home", active = true, icon = { Icon(Icons.Default.Home, null) }),
            ArcAdvancedItem("search", "Search", icon = { Icon(Icons.Default.Search, null) }),
            ArcAdvancedItem("profile", "Profile", badgeCount = 2, icon = { Icon(Icons.Default.AccountCircle, null) }),
            ArcAdvancedItem("settings", "Settings", enabled = true, icon = { Icon(Icons.Default.Settings, null) })
        )
    }
    val tabs = remember {
        listOf(
            ArcAdvancedItem("recent", "Recent", icon = { Icon(Icons.Default.Home, null) }),
            ArcAdvancedItem("saved", "Saved", badgeCount = 4, icon = { Icon(Icons.Default.CreditCard, null) }),
            ArcAdvancedItem("media", "Media", icon = { Icon(Icons.Default.Image, null) })
        )
    }
    val coverItems = remember {
        listOf("Coast", "Archive", "Studio", "Garden", "Night")
            .mapIndexed { index, label -> ArcCoverItem("cover-$index", label, "$label photo") }
    }
    val initialCards = remember {
        listOf(
            ArcWalletCard("travel", "Travel", "Travel card ending in 2042"),
            ArcWalletCard("daily", "Daily", "Daily card ending in 7811"),
            ArcWalletCard("virtual", "Virtual", "Virtual card ending in 0904")
        )
    }

    var selected by remember { mutableStateOf("home") }
    var navExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("recent") }
    var selectedCover by remember { mutableStateOf<String?>(coverItems.firstOrNull()?.key) }
    var selectedWallet by remember { mutableStateOf("travel") }
    val walletOrder = remember { mutableStateListOf(*initialCards.map { it.key }.toTypedArray()) }
    val walletCards = walletOrder.mapNotNull { key -> initialCards.firstOrNull { it.key == key } }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Text("ArcDock", color = ArcTheme.colors.foreground)
        ArcDock(items = destinations, selectedKey = selected, onItemSelected = { selected = it })

        Text("ArcMorphNavigation", color = ArcTheme.colors.foreground)
        ArcMorphNavigation(
            destinations = destinations,
            selectedKey = selected,
            onSelectedKeyChange = { selected = it },
            expanded = navExpanded,
            onExpandedChange = { navExpanded = it }
        )

        Text("ArcGlassTabBar", color = ArcTheme.colors.foreground)
        ArcGlassTabBar(tabs = tabs, selectedKey = selectedTab, onTabSelected = { selectedTab = it })

        Text("ArcWalletStack", color = ArcTheme.colors.foreground)
        ArcWalletStack(
            cards = walletCards,
            selectedKey = selectedWallet,
            onSelectedKeyChange = { selectedWallet = it },
            onOrderChange = { keys ->
                walletOrder.clear()
                walletOrder.addAll(keys)
            },
            cardContent = { card, focused ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(card.label, color = ArcTheme.colors.foreground)
                    Text(if (focused) "Selected ·••• 2042" else "•••• 2042", color = ArcTheme.colors.textSecondary)
                }
            }
        )

        Text("ArcCoverFlow", color = ArcTheme.colors.foreground)
        ArcCoverFlow(
            items = coverItems,
            selectedKey = selectedCover,
            onSelectedKeyChange = { selectedCover = it },
            itemContent = { item, focused ->
                Surface(color = if (focused) ArcTheme.colors.surfaceRaised else ArcTheme.colors.surfaceMuted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = ArcTheme.colors.textSecondary)
                        Text(item.label, color = ArcTheme.colors.foreground)
                    }
                }
            }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Independent slots · controlled selection", color = ArcTheme.colors.textMuted)
        }
    }
}
