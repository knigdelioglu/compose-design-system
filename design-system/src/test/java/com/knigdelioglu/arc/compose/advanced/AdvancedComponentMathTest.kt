package com.knigdelioglu.arc.compose.advanced

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AdvancedComponentMathTest {
    @Test
    fun dockInfluenceIsSmoothBoundedAndFallsOffAtRadius() {
        assertEquals(1f, arcDockInfluence(0f, 100f), 0f)
        assertEquals(0f, arcDockInfluence(100f, 100f), 0f)
        assertEquals(0f, arcDockInfluence(0f, 0f), 0f)
        val neighbor = arcDockInfluence(50f, 100f)
        assertEquals(0.5f, neighbor, 0.0001f)
        assertEquals(neighbor, arcDockInfluence(-50f, 100f), 0f)
    }

    @Test
    fun coverDepthIsOneAtCenterAndZeroBeyondAdjacentCard() {
        assertEquals(1f, arcCoverDepth(0f), 0f)
        assertEquals(0.5f, arcCoverDepth(-0.5f), 0f)
        assertEquals(0f, arcCoverDepth(1f), 0f)
        assertEquals(0f, arcCoverDepth(4f), 0f)
        assertEquals(-140, arcCoverCenterScrollOffset(300, 580))
        assertEquals(0, arcCoverCenterScrollOffset(300, 300))
    }

    @Test
    fun walletReorderingHandlesEmptySingleAndBothSwipeDirections() {
        val cards = listOf("a", "b", "c")
        assertEquals(emptyList<String>(), reorderArcStack(emptyList<String>(), { it }, -1))
        assertSame(cards, reorderArcStack(cards, { it }, 0))
        assertEquals(listOf("b", "c", "a"), reorderArcStack(cards, { it }, -1))
        assertEquals(listOf("c", "a", "b"), reorderArcStack(cards, { it }, 1))
        assertEquals(listOf("c", "a", "b"), moveArcItemToFront(cards, { it }, "c"))
        assertEquals(cards, moveArcItemToFront(cards, { it }, "missing"))
    }

    @Test
    fun keyboardNavigationFindsEnabledItemsWithoutWrapping() {
        val items = listOf("first", "disabled", "last")
        val enabled = { item: String -> item != "disabled" }

        assertEquals(2, nextEnabledArcIndex(items, 0, 1, enabled))
        assertEquals(0, nextEnabledArcIndex(items, 2, -1, enabled))
        assertEquals(-1, nextEnabledArcIndex(items, 0, -1, enabled))
        assertEquals(-1, nextEnabledArcIndex(items, 2, 1, enabled))
        assertEquals(0, nextEnabledArcIndex(items, -1, 1, enabled))
    }
}
