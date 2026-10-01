package com.elxvro.skytower.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenLayoutTest {
    @Test fun referenceScaleComesFromViewportWidth() {
        assertEquals(1f, ScreenLayout(1080f, 1920f).scale, 0.0001f)
        assertEquals(0.5f, ScreenLayout(540f, 1200f).scale, 0.0001f)
    }

    @Test fun tallScreensDoNotStretchReferenceCards() {
        val normal = ScreenLayout(1080f, 1920f)
        val tall = ScreenLayout(1080f, 2400f)
        assertEquals(normal.cardHeight(300f), tall.cardHeight(300f), 0.0001f)
        assertTrue(tall.backgroundExtra > normal.backgroundExtra)
    }

    @Test fun touchTargetsHaveAtLeastReferenceMinimum() {
        val layout = ScreenLayout(540f, 1200f)
        val rect = layout.touchRect(centerX = 270f, centerY = 600f, width = 20f, height = 10f)
        assertTrue(rect.width >= layout.minTouch)
        assertTrue(rect.height >= layout.minTouch)
    }

    @Test fun cardAspectRatioIsPreservedAcrossWidths() {
        val large = ScreenLayout(1080f, 1920f).referenceRect(90f, 400f, 900f, 300f)
        val small = ScreenLayout(540f, 1000f).referenceRect(90f, 400f, 900f, 300f)
        assertEquals(large.width / large.height, small.width / small.height, 0.0001f)
    }
}
