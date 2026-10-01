package com.elxvro.skytower.game

data class RunRewardBreakdown(
    val runCoins: Int,
    val missionCoins: Int,
    val streakCoins: Int,
    val levelCoins: Int,
    val totalCoins: Int,
)

object RunRewardRules {
    fun calculate(
        runCoins: Int,
        missionCoins: Int,
        streakCoins: Int,
        levelCoins: Int,
        coinMultiplier: Boolean,
    ): RunRewardBreakdown {
        val safeRun = runCoins.coerceAtLeast(0)
        val adjustedRun = if (coinMultiplier) safeRun * 2 else safeRun
        val safeMission = missionCoins.coerceAtLeast(0)
        val safeStreak = streakCoins.coerceAtLeast(0)
        val safeLevel = levelCoins.coerceAtLeast(0)
        return RunRewardBreakdown(
            runCoins = adjustedRun,
            missionCoins = safeMission,
            streakCoins = safeStreak,
            levelCoins = safeLevel,
            totalCoins = adjustedRun + safeMission + safeStreak + safeLevel,
        )
    }
}
