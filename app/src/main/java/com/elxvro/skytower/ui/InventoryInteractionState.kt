package com.elxvro.skytower.ui

import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.PowerUpInventory
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.game.SkinRules

enum class PowerActionState { BUY, EQUIP, EQUIPPED }

object InventoryInteractionState {
    fun skin(skin: BlockSkin, selected: BlockSkin, unlockedMask: Int, coins: Int): ItemActionState = when {
        skin == selected && SkinRules.isOwned(skin, unlockedMask) -> ItemActionState.SELECTED
        SkinRules.isOwned(skin, unlockedMask) -> ItemActionState.SELECT
        coins.coerceAtLeast(0) >= SkinRules.price(skin) -> ItemActionState.BUY
        else -> ItemActionState.LOCKED
    }

    fun power(type: PowerUpType, inventory: PowerUpInventory, equipped: Set<PowerUpType>): PowerActionState = when {
        type in equipped -> PowerActionState.EQUIPPED
        inventory.count(type) > 0 -> PowerActionState.EQUIP
        else -> PowerActionState.BUY
    }
}
