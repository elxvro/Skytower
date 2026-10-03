package com.elxvro.skytower.game

enum class BlockSkin {
    CLASSIC,
    CRYSTAL,
    MARBLE,
    GOLD,
    ICE,
    NEON,
}

data class SkinPurchaseResult(
    val purchased: Boolean,
    val coins: Int,
    val unlockedMask: Int,
    val selected: BlockSkin,
)

object SkinRules {
    val DEFAULT_UNLOCKED_MASK: Int = (1 shl BlockSkin.CLASSIC.ordinal) or (1 shl BlockSkin.CRYSTAL.ordinal)

    fun price(skin: BlockSkin): Int = when (skin) {
        BlockSkin.CLASSIC, BlockSkin.CRYSTAL -> 0
        BlockSkin.MARBLE -> 650
        BlockSkin.GOLD -> 900
        BlockSkin.ICE -> 800
        BlockSkin.NEON -> 1200
    }

    fun isOwned(skin: BlockSkin, unlockedMask: Int): Boolean =
        unlockedMask and (1 shl skin.ordinal) != 0

    fun purchase(skin: BlockSkin, coins: Int, unlockedMask: Int, selected: BlockSkin): SkinPurchaseResult {
        val safeCoins = coins.coerceAtLeast(0)
        if (isOwned(skin, unlockedMask)) {
            return SkinPurchaseResult(false, safeCoins, unlockedMask, skin)
        }
        val cost = price(skin)
        if (safeCoins < cost) {
            return SkinPurchaseResult(false, safeCoins, unlockedMask, selected)
        }
        return SkinPurchaseResult(
            purchased = true,
            coins = safeCoins - cost,
            unlockedMask = unlockedMask or (1 shl skin.ordinal),
            selected = skin,
        )
    }

    fun select(skin: BlockSkin, unlockedMask: Int, selected: BlockSkin): BlockSkin =
        if (isOwned(skin, unlockedMask)) skin else selected
}
