package com.elxvro.skytower.ui

import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.PowerUpInventory
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.game.SkinRules
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryInteractionStateTest {
    @Test fun skinStateShowsSelectedOwnedAffordableOrLocked() {
        assertEquals(ItemActionState.SELECTED, InventoryInteractionState.skin(BlockSkin.CLASSIC, BlockSkin.CLASSIC, SkinRules.DEFAULT_UNLOCKED_MASK, 0))
        assertEquals(ItemActionState.SELECT, InventoryInteractionState.skin(BlockSkin.CRYSTAL, BlockSkin.CLASSIC, SkinRules.DEFAULT_UNLOCKED_MASK, 0))
        assertEquals(ItemActionState.BUY, InventoryInteractionState.skin(BlockSkin.MARBLE, BlockSkin.CLASSIC, SkinRules.DEFAULT_UNLOCKED_MASK, 700))
        assertEquals(ItemActionState.LOCKED, InventoryInteractionState.skin(BlockSkin.NEON, BlockSkin.CLASSIC, SkinRules.DEFAULT_UNLOCKED_MASK, 200))
    }

    @Test fun powerUpStateShowsBuyEquipAndEquipped() {
        val inventory = PowerUpInventory(slowTime = 1)
        assertEquals(PowerActionState.BUY, InventoryInteractionState.power(PowerUpType.SECOND_CHANCE, inventory, emptySet()))
        assertEquals(PowerActionState.EQUIP, InventoryInteractionState.power(PowerUpType.SLOW_TIME, inventory, emptySet()))
        assertEquals(PowerActionState.EQUIPPED, InventoryInteractionState.power(PowerUpType.SLOW_TIME, inventory, setOf(PowerUpType.SLOW_TIME)))
    }
}
