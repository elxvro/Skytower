package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EconomyRulesTest {
    @Test
    fun runRewardUsesScoreAndPerfects() {
        assertEquals(0, EconomyRules.runCoins(RunStats(score = 0, placements = 0, perfects = 0)))
        assertEquals(1, EconomyRules.runCoins(RunStats(score = 1, placements = 1, perfects = 0)))
        assertEquals(9, EconomyRules.runCoins(RunStats(score = 25, placements = 8, perfects = 4)))
    }

    @Test
    fun settlementCompletesEachMissionOnlyOnce() {
        val initial = MissionProgress()
        val first = EconomyRules.settle(
            stats = RunStats(score = 50, placements = 10, perfects = 3),
            progress = initial,
        )

        assertEquals(85, first.missionCoins)
        assertEquals(98, first.totalCoinsAwarded)
        assertEquals(10, first.progress.totalPlacements)
        assertEquals(3, first.progress.totalPerfects)
        assertEquals(50, first.progress.bestScore)
        assertEquals(EconomyRules.ALL_MISSIONS_MASK, first.progress.claimedMask)

        val second = EconomyRules.settle(
            stats = RunStats(score = 50, placements = 10, perfects = 3),
            progress = first.progress,
        )
        assertEquals(0, second.missionCoins)
        assertEquals(13, second.totalCoinsAwarded)
    }

    @Test
    fun missionsAccumulateAcrossRuns() {
        val afterFirst = EconomyRules.settle(
            RunStats(score = 12, placements = 6, perfects = 1),
            MissionProgress(),
        )
        val afterSecond = EconomyRules.settle(
            RunStats(score = 20, placements = 4, perfects = 2),
            afterFirst.progress,
        )

        assertEquals(10, afterSecond.progress.totalPlacements)
        assertEquals(3, afterSecond.progress.totalPerfects)
        assertTrue(afterSecond.progress.isClaimed(EconomyRules.MISSION_BLOCKS))
        assertTrue(afterSecond.progress.isClaimed(EconomyRules.MISSION_PERFECTS))
        assertFalse(afterSecond.progress.isClaimed(EconomyRules.MISSION_SCORE))
        assertEquals(45, afterSecond.missionCoins)
    }

    @Test
    fun premiumThemeCostsAreStable() {
        assertEquals(0, EconomyRules.themeCost(0))
        assertEquals(0, EconomyRules.themeCost(2))
        assertEquals(60, EconomyRules.themeCost(3))
        assertEquals(120, EconomyRules.themeCost(4))
        assertEquals(200, EconomyRules.themeCost(5))
    }

    @Test
    fun unlockRequiresEnoughCoinsAndLeavesBalance() {
        val denied = EconomyRules.unlockTheme(themeId = 4, coins = 119, unlockedMask = 0b111)
        assertFalse(denied.unlocked)
        assertEquals(119, denied.coins)

        val unlocked = EconomyRules.unlockTheme(themeId = 4, coins = 140, unlockedMask = 0b111)
        assertTrue(unlocked.unlocked)
        assertEquals(20, unlocked.coins)
        assertTrue(unlocked.isThemeUnlocked(4))

        val repeated = EconomyRules.unlockTheme(themeId = 4, coins = 20, unlockedMask = unlocked.unlockedMask)
        assertTrue(repeated.unlocked)
        assertEquals(20, repeated.coins)
    }
}
