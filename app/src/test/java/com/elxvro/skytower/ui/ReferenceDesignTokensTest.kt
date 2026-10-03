package com.elxvro.skytower.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceDesignTokensTest {
    @Test fun paletteMatchesApprovedReferenceFamily() {
        assertEquals(0xFF4A9CF0.toInt(), ReferenceDesignTokens.SKY_TOP)
        assertEquals(0xFFE3F6FD.toInt(), ReferenceDesignTokens.SKY_BOTTOM)
        assertEquals(0xFF0D3F84.toInt(), ReferenceDesignTokens.NAVY)
        assertEquals(0xFF39D12F.toInt(), ReferenceDesignTokens.ACTION_GREEN)
        assertEquals(0xFFFFC72E.toInt(), ReferenceDesignTokens.GOLD)
        assertEquals(0xFFF24B43.toInt(), ReferenceDesignTokens.RIBBON_RED)
    }

    @Test fun flat2DStyleDoesNotUseBlurShadow() {
        assertEquals(0x00000000, ReferenceDesignTokens.SHADOW)
    }

    @Test fun sharedGeometryIsCompactAndConsistent() {
        assertTrue(ReferenceDesignTokens.CARD_RADIUS_REF in 26f..34f)
        assertTrue(ReferenceDesignTokens.PANEL_RADIUS_REF >= ReferenceDesignTokens.CARD_RADIUS_REF)
        assertEquals(32f, ReferenceDesignTokens.SCREEN_GUTTER_REF, 0.001f)
        assertEquals(18f, ReferenceDesignTokens.GRID_GAP_REF, 0.001f)
    }
}
