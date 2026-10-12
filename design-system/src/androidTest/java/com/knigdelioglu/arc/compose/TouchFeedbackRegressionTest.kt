package com.knigdelioglu.arc.compose

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.knigdelioglu.arc.compose.controls.ArcBillingPeriod
import com.knigdelioglu.arc.compose.controls.ArcBillingToggle
import com.knigdelioglu.arc.compose.controls.ArcButton
import com.knigdelioglu.arc.compose.controls.ArcIconMorph
import com.knigdelioglu.arc.compose.controls.ArcRadioCards
import com.knigdelioglu.arc.compose.controls.ArcSegmentedControl
import com.knigdelioglu.arc.compose.foundation.ArcTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/** Regression coverage for controls whose visual feedback shape differs from their touch target. */
@RunWith(AndroidJUnit4::class)
class TouchFeedbackRegressionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun roundedButtonsAndSelectionsStillRespondToTaps() {
        var buttonClicks = 0
        var selectedSegment by mutableStateOf(0)
        var selectedCard by mutableStateOf("First")
        compose.setContent {
            ArcTheme {
                androidx.compose.foundation.layout.Column {
                    ArcButton(text = "Save", onClick = { buttonClicks++ })
                    ArcSegmentedControl(
                        items = listOf("Choice A", "Choice B"),
                        selectedIndex = selectedSegment,
                        onSelectIndex = { selectedSegment = it }
                    )
                    ArcRadioCards(
                        options = listOf("First", "Second"),
                        selectedOption = selectedCard,
                        onOptionSelected = { selectedCard = it },
                        titleProvider = { it }
                    )
                }
            }
        }
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Choice B").performClick()
        compose.onNodeWithText("Second").performClick()
        compose.runOnIdle {
            assertEquals(1, buttonClicks)
            assertEquals(1, selectedSegment)
            assertEquals("Second", selectedCard)
        }
    }

    @Test fun iconAndBillingTargetsKeepActionsAfterRippleChange() {
        var expanded by mutableStateOf(false)
        var selectedPeriod by mutableStateOf(ArcBillingPeriod.Monthly)
        compose.setContent {
            ArcTheme {
                androidx.compose.foundation.layout.Column {
                    ArcIconMorph(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        contentDescription = "Menu"
                    )
                    ArcBillingToggle(
                        selectedPeriod = selectedPeriod,
                        onPeriodChange = { selectedPeriod = it }
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("Menu").performClick()
        compose.onNodeWithText("Yearly").performClick()
        compose.runOnIdle {
            assertTrue(expanded)
            assertEquals(ArcBillingPeriod.Yearly, selectedPeriod)
        }
    }
}
