package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerUpRulesTest {
    @Test fun powerUpPricesMatchReferenceValues() {
        assertEquals(300, PowerUpRules.price(PowerUpType.SECOND_CHANCE))
        assertEquals(250, PowerUpRules.price(PowerUpType.SLOW_TIME))
        assertEquals(350, PowerUpRules.price(PowerUpType.WIDE_PERFECT))
        assertEquals(400, PowerUpRules.price(PowerUpType.COIN_MULTIPLIER))
    }

    @Test fun inventoryNeverBecomesNegative() {
        val state = PowerUpShopState()
        val consumed = PowerUpRules.consume(state, PowerUpType.SLOW_TIME)
        assertFalse(consumed.consumed)
        assertEquals(0, consumed.state.inventory.count(PowerUpType.SLOW_TIME))
    }

    @Test fun cannotEquipMoreThanThreePowerUpTypes() {
        var state = PowerUpShopState(
            inventory = PowerUpInventory(secondChance = 1, slowTime = 1, widePerfect = 1, coinMultiplier = 1),
        )
        state = PowerUpRules.toggleEquip(state, PowerUpType.SECOND_CHANCE).state
        state = PowerUpRules.toggleEquip(state, PowerUpType.SLOW_TIME).state
        state = PowerUpRules.toggleEquip(state, PowerUpType.WIDE_PERFECT).state
        val fourth = PowerUpRules.toggleEquip(state, PowerUpType.COIN_MULTIPLIER)
        assertFalse(fourth.changed)
        assertEquals(3, fourth.state.equipped.size)
    }

    @Test fun duplicatePurchaseTapChargesOnlyForSuccessfulMutation() {
        val initial = PowerUpShopState(coins = 1000)
        val first = PowerUpRules.purchase(initial, PowerUpType.SECOND_CHANCE, requestToken = "tap-1")
        val duplicate = PowerUpRules.purchase(first.state, PowerUpType.SECOND_CHANCE, requestToken = "tap-1")
        assertTrue(first.purchased)
        assertFalse(duplicate.purchased)
        assertEquals(700, duplicate.state.coins)
        assertEquals(1, duplicate.state.inventory.count(PowerUpType.SECOND_CHANCE))
    }
}
