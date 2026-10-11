package com.knigdelioglu.arc.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knigdelioglu.arc.compose.advanced.ArcAdvancedItem
import com.knigdelioglu.arc.compose.advanced.ArcCoverFlow
import com.knigdelioglu.arc.compose.advanced.ArcCoverItem
import com.knigdelioglu.arc.compose.advanced.ArcDock
import com.knigdelioglu.arc.compose.advanced.ArcWalletCard
import com.knigdelioglu.arc.compose.advanced.ArcWalletStack
import com.knigdelioglu.arc.compose.controls.ArcOtpInput
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class IntegratedComponentsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dockSelectionRemainsControlledAndAccessible() {
        val items = listOf(
            ArcAdvancedItem(key = "home", label = "Home"),
            ArcAdvancedItem(key = "search", label = "Search")
        )
        var selected by mutableStateOf("home")
        compose.setContent {
            ArcTheme {
                ArcDock(items = items, selectedKey = selected, onItemSelected = { selected = it })
            }
        }

        compose.onNodeWithContentDescription("Search").performClick()
        compose.runOnIdle { assertEquals("search", selected) }
    }

    @Test
    fun coverFlowKeyboardNavigationSkipsDisabledItems() {
        val items = listOf(
            ArcCoverItem(key = "first", label = "First", contentDescription = "First cover"),
            ArcCoverItem(key = "blocked", label = "Blocked", contentDescription = "Blocked cover", enabled = false),
            ArcCoverItem(key = "last", label = "Last", contentDescription = "Last cover")
        )
        var selected by mutableStateOf("first")
        compose.setContent {
            ArcTheme(reducedMotion = true) {
                ArcCoverFlow(
                    items = items,
                    selectedKey = selected,
                    onSelectedKeyChange = { selected = it },
                    modifier = Modifier.size(360.dp, 280.dp),
                    itemContent = { item, _ -> Text(item.label) }
                )
            }
        }

        val coverFlow = compose.onNodeWithContentDescription("Cover flow gallery")
        coverFlow.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        coverFlow.assertIsFocused()
        coverFlow.performKeyInput { pressKey(Key.DirectionRight) }
        compose.runOnIdle { assertEquals("last", selected) }
    }

    @Test
    fun walletStackKeyboardNavigationSkipsDisabledCards() {
        val cards = listOf(
            ArcWalletCard(key = "first", label = "First", contentDescription = "First wallet"),
            ArcWalletCard(key = "blocked", label = "Blocked", contentDescription = "Blocked wallet", enabled = false),
            ArcWalletCard(key = "last", label = "Last", contentDescription = "Last wallet")
        )
        var selected by mutableStateOf("first")
        compose.setContent {
            ArcTheme {
                ArcWalletStack(
                    cards = cards,
                    selectedKey = selected,
                    onSelectedKeyChange = { selected = it },
                    onOrderChange = {},
                    modifier = Modifier.size(360.dp, 420.dp)
                ) { card, _ -> Column { Text(card.label) } }
            }
        }

        val wallet = compose.onNodeWithContentDescription("Wallet card stack")
        wallet.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        wallet.assertIsFocused()
        wallet.performKeyInput { pressKey(Key.DirectionRight) }
        compose.runOnIdle { assertEquals("last", selected) }
    }

    @Test
    fun otpInputSanitizesPastedTextAndReportsCompletion() {
        var code by mutableStateOf("")
        var completed by mutableStateOf<String?>(null)
        compose.setContent {
            ArcTheme {
                ArcOtpInput(
                    value = code,
                    onValueChange = { code = it },
                    length = 4,
                    onComplete = { completed = it }
                )
            }
        }

        compose.onNode(hasSetTextAction()).performTextInput("12a34")
        compose.runOnIdle {
            assertEquals("1234", code)
            assertEquals("1234", completed)
        }
    }
}
