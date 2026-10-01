package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Test

class RunRewardRulesTest {
    @Test fun coinMultiplierDoublesOnlyRunCoins() {
        val result = RunRewardRules.calculate(runCoins = 20, missionCoins = 25, streakCoins = 150, levelCoins = 50, coinMultiplier = true)
        assertEquals(40, result.runCoins)
        assertEquals(25, result.missionCoins)
        assertEquals(150, result.streakCoins)
        assertEquals(50, result.levelCoins)
        assertEquals(265, result.totalCoins)
    }

    @Test fun missionStreakAndLevelRewardsAreNotMultiplied() {
        val result = RunRewardRules.calculate(runCoins = 5, missionCoins = 40, streakCoins = 150, levelCoins = 125, coinMultiplier = true)
        assertEquals(10, result.runCoins)
        assertEquals(315, result.totalCoins)
    }
}
