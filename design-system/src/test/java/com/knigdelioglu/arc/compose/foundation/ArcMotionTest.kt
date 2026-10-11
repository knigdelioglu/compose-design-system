package com.knigdelioglu.arc.compose.foundation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcMotionTest {

    @Test
    fun reducedMotionDefaultsToFalse() {
        val motion = ArcMotionTokens()
        assertFalse(motion.reducedMotion)
        assertEquals(240, motion.durationStandard)
        assertEquals(160, motion.durationFast)
    }

    @Test
    fun reducedMotionSnapsSpringAndTweenTransitions() {
        val motion = ArcMotionTokens(reducedMotion = true)
        assertTrue(motion.reducedMotion)

        val responsiveSpec = motion.springResponsive<Float>()
        assertTrue(responsiveSpec is androidx.compose.animation.core.SnapSpec)

        val tweenSpec = motion.standardTween<Float>()
        // When reduced motion is active, standardTween returns a SnapSpec
        assertTrue(tweenSpec is androidx.compose.animation.core.SnapSpec)
        assertEquals(0, motion.staggerDelay(5))
    }

    @Test
    fun regularMotionExposesArcSpringAndStaggerTokens() {
        val motion = ArcMotionTokens()
        assertEquals(580, motion.durationSpring)
        assertEquals(160, motion.staggerDelay(4))
        assertTrue(motion.springResponsive<Float>() !is androidx.compose.animation.core.SnapSpec)
    }
}
