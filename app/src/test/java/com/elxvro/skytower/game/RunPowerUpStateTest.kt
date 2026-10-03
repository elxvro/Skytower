package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunPowerUpStateTest {
    @Test fun secondChanceCanRescueOnlyOncePerRun() {
        val state = RunPowerUpState(setOf(PowerUpType.SECOND_CHANCE))
        assertTrue(state.tryUseSecondChance())
        assertFalse(state.tryUseSecondChance())
    }

    @Test fun slowTimeRunsForEightSecondsOfActiveGameplay() {
        val state = RunPowerUpState(setOf(PowerUpType.SLOW_TIME))
        assertTrue(state.activate(PowerUpType.SLOW_TIME))
        assertEquals(0.65f, state.speedMultiplier, 0.0001f)
        state.update(3f, running = true)
        assertEquals(5f, state.slowTimeRemaining, 0.0001f)
        state.update(10f, running = false)
        assertEquals(5f, state.slowTimeRemaining, 0.0001f)
        state.update(5f, running = true)
        assertEquals(1f, state.speedMultiplier, 0.0001f)
    }

    @Test fun widePerfectRunsForEightSecondsAndUsesOnePointSevenFiveMultiplier() {
        val state = RunPowerUpState(setOf(PowerUpType.WIDE_PERFECT))
        assertTrue(state.activate(PowerUpType.WIDE_PERFECT))
        assertEquals(1.75f, state.perfectToleranceMultiplier, 0.0001f)
        state.update(8f, running = true)
        assertEquals(1f, state.perfectToleranceMultiplier, 0.0001f)
    }

    @Test fun coinMultiplierIsPassiveAndActiveOnlyWhenEquipped() {
        assertTrue(RunPowerUpState(setOf(PowerUpType.COIN_MULTIPLIER)).coinMultiplierActive)
        assertFalse(RunPowerUpState(emptySet()).coinMultiplierActive)
    }

    @Test fun nonEquippedTimedPowerUpCannotActivate() {
        val state = RunPowerUpState(emptySet())
        assertFalse(state.activate(PowerUpType.SLOW_TIME))
        assertEquals(1f, state.speedMultiplier, 0.0001f)
    }
}
