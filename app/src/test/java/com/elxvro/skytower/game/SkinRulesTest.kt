package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkinRulesTest {
    @Test fun classicAndCrystalAreOwnedByDefault() {
        assertTrue(SkinRules.isOwned(BlockSkin.CLASSIC, SkinRules.DEFAULT_UNLOCKED_MASK))
        assertTrue(SkinRules.isOwned(BlockSkin.CRYSTAL, SkinRules.DEFAULT_UNLOCKED_MASK))
    }

    @Test fun skinPricesMatchReferenceValues() {
        assertEquals(0, SkinRules.price(BlockSkin.CLASSIC))
        assertEquals(0, SkinRules.price(BlockSkin.CRYSTAL))
        assertEquals(650, SkinRules.price(BlockSkin.MARBLE))
        assertEquals(900, SkinRules.price(BlockSkin.GOLD))
        assertEquals(800, SkinRules.price(BlockSkin.ICE))
        assertEquals(1200, SkinRules.price(BlockSkin.NEON))
    }

    @Test fun insufficientCoinsDoNotUnlockSkin() {
        val result = SkinRules.purchase(BlockSkin.GOLD, coins = 899, unlockedMask = SkinRules.DEFAULT_UNLOCKED_MASK, selected = BlockSkin.CLASSIC)
        assertFalse(result.purchased)
        assertEquals(899, result.coins)
        assertFalse(SkinRules.isOwned(BlockSkin.GOLD, result.unlockedMask))
    }

    @Test fun purchasedSkinUnlocksAndSelectsImmediately() {
        val result = SkinRules.purchase(BlockSkin.MARBLE, coins = 700, unlockedMask = SkinRules.DEFAULT_UNLOCKED_MASK, selected = BlockSkin.CLASSIC)
        assertTrue(result.purchased)
        assertEquals(50, result.coins)
        assertTrue(SkinRules.isOwned(BlockSkin.MARBLE, result.unlockedMask))
        assertEquals(BlockSkin.MARBLE, result.selected)
    }
}
