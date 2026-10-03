package com.elxvro.skytower.ui.assets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UiBitmapRendererMathTest {
    private fun assertRect(expected: FloatRect, actual: FloatRect?) {
        requireNotNull(actual)
        assertEquals(expected.left, actual.left, 0.001f)
        assertEquals(expected.top, actual.top, 0.001f)
        assertEquals(expected.right, actual.right, 0.001f)
        assertEquals(expected.bottom, actual.bottom, 0.001f)
    }

    @Test
    fun aspectFitWideSourceCentersVertically() {
        val result = UiBitmapRenderer.aspectFit(
            SourceSize(1000f, 500f),
            FloatRect(0f, 0f, 300f, 300f),
        )
        assertRect(FloatRect(0f, 75f, 300f, 225f), result)
    }

    @Test
    fun aspectFitTallSourceCentersHorizontally() {
        val result = UiBitmapRenderer.aspectFit(
            SourceSize(500f, 1000f),
            FloatRect(0f, 0f, 300f, 300f),
        )
        assertRect(FloatRect(75f, 0f, 225f, 300f), result)
    }

    @Test
    fun centerCropSourceUsesTargetAspectAndStaysInsideSource() {
        val result = UiBitmapRenderer.centerCropSource(
            SourceSize(1000f, 500f),
            FloatRect(0f, 0f, 300f, 600f),
        )
        assertRect(FloatRect(375f, 0f, 625f, 500f), result)
    }

    @Test
    fun invalidSourceDimensionsReturnNull() {
        assertNull(UiBitmapRenderer.aspectFit(SourceSize(0f, 100f), FloatRect(0f, 0f, 300f, 300f)))
        assertNull(UiBitmapRenderer.centerCropSource(SourceSize(100f, -1f), FloatRect(0f, 0f, 300f, 300f)))
    }
}
