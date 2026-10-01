package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelRulesTest {
    @Test fun runXpUsesPlacementsPerfectsAndScoreCap() {
        assertEquals(10 * 4 + 5 * 2 + 100, LevelRules.runXp(RunStats(score = 180, placements = 4, perfects = 2)))
    }

    @Test fun level12Needs1000XpForLevel13() {
        assertEquals(1000, LevelRules.xpRequiredForNext(12))
    }

    @Test fun excessXpCarriesAcrossMultipleLevels() {
        val total = LevelRules.totalXpToReachLevel(3) + 123
        val state = LevelRules.levelForXp(total)
        assertEquals(3, state.level)
        assertEquals(123, state.xpIntoLevel)
        assertEquals(LevelRules.xpRequiredForNext(3), state.xpRequired)
    }

    @Test fun levelRewardCannotBeClaimedTwice() {
        val totalXp = LevelRules.totalXpToReachLevel(5)
        val first = LevelRules.claimReward(totalXp, 3, emptySet())
        val second = LevelRules.claimReward(totalXp, 3, first.claimedLevels)
        assertTrue(first.claimed)
        assertEquals(75, first.reward.coins)
        assertFalse(second.claimed)
        assertEquals(0, second.reward.coins)
    }

    @Test fun levelSevenPlusRewardsFollowFourLevelCycle() {
        assertEquals(125, LevelRules.rewardForLevel(7).coins)
        assertEquals(PowerUpType.SLOW_TIME, LevelRules.rewardForLevel(8).powerUp)
        assertEquals(150, LevelRules.rewardForLevel(9).coins)
        assertEquals(PowerUpType.WIDE_PERFECT, LevelRules.rewardForLevel(10).powerUp)
        assertEquals(125, LevelRules.rewardForLevel(11).coins)
    }
}
