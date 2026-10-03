package com.elxvro.skytower.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceStackLayoutTest {
    @Test
    fun homeStackMatchesApprovedSixBlockReference() {
        val spec = ReferenceStackLayout.home()
        assertEquals(6, spec.blocks.size)
        assertEquals(
            listOf(
                0xFF2D9EF2.toInt(),
                0xFF92562E.toInt(),
                0xFF58C936.toInt(),
                0xFFFFC62D.toInt(),
                0xFFF24C70.toInt(),
                0xFFF2EFE8.toInt(),
            ),
            spec.blocks.map { it.color },
        )
        assertTrue(spec.platformGrassHeight > 0f)
        assertTrue(spec.platformRockDepth > spec.platformGrassHeight)
    }

    @Test
    fun previewStackUsesSameBlockOrderAsGameplay() {
        val palette = ThemePalette.get(0).blocks
        val preview = ReferenceStackLayout.preview(palette)
        assertEquals(palette.toList(), preview.blocks.map { it.color })
        assertEquals(6, preview.blocks.size)
        assertTrue(preview.blocks.zipWithNext().all { (lower, upper) -> upper.width <= lower.width })
    }
}
