package com.elxvro.skytower.game

data class RunStats(
    val score: Int,
    val placements: Int,
    val perfects: Int,
)

data class MissionProgress(
    val totalPlacements: Int = 0,
    val totalPerfects: Int = 0,
    val bestScore: Int = 0,
    val claimedMask: Int = 0,
) {
    fun isClaimed(missionBit: Int): Boolean = claimedMask and missionBit != 0
}

data class SettlementResult(
    val runCoins: Int,
    val missionCoins: Int,
    val totalCoinsAwarded: Int,
    val progress: MissionProgress,
)

data class ThemeUnlockResult(
    val unlocked: Boolean,
    val coins: Int,
    val unlockedMask: Int,
) {
    fun isThemeUnlocked(themeId: Int): Boolean = unlockedMask and (1 shl themeId) != 0
}

object EconomyRules {
    const val MISSION_BLOCKS = 1
    const val MISSION_PERFECTS = 1 shl 1
    const val MISSION_SCORE = 1 shl 2
    const val ALL_MISSIONS_MASK = MISSION_BLOCKS or MISSION_PERFECTS or MISSION_SCORE

    private const val BLOCKS_TARGET = 10
    private const val PERFECTS_TARGET = 3
    private const val SCORE_TARGET = 50

    private const val BLOCKS_REWARD = 20
    private const val PERFECTS_REWARD = 25
    private const val SCORE_REWARD = 40

    fun runCoins(stats: RunStats): Int {
        if (stats.score <= 0 && stats.placements <= 0) return 0
        return maxOf(1, stats.score.coerceAtLeast(0) / 5) + stats.perfects.coerceAtLeast(0)
    }

    fun settle(stats: RunStats, progress: MissionProgress): SettlementResult {
        val updatedPlacements = progress.totalPlacements + stats.placements.coerceAtLeast(0)
        val updatedPerfects = progress.totalPerfects + stats.perfects.coerceAtLeast(0)
        val updatedBestScore = maxOf(progress.bestScore, stats.score.coerceAtLeast(0))

        var claimedMask = progress.claimedMask
        var missionCoins = 0

        if (updatedPlacements >= BLOCKS_TARGET && claimedMask and MISSION_BLOCKS == 0) {
            claimedMask = claimedMask or MISSION_BLOCKS
            missionCoins += BLOCKS_REWARD
        }
        if (updatedPerfects >= PERFECTS_TARGET && claimedMask and MISSION_PERFECTS == 0) {
            claimedMask = claimedMask or MISSION_PERFECTS
            missionCoins += PERFECTS_REWARD
        }
        if (updatedBestScore >= SCORE_TARGET && claimedMask and MISSION_SCORE == 0) {
            claimedMask = claimedMask or MISSION_SCORE
            missionCoins += SCORE_REWARD
        }

        val runCoins = runCoins(stats)
        val updatedProgress = MissionProgress(
            totalPlacements = updatedPlacements,
            totalPerfects = updatedPerfects,
            bestScore = updatedBestScore,
            claimedMask = claimedMask,
        )
        return SettlementResult(
            runCoins = runCoins,
            missionCoins = missionCoins,
            totalCoinsAwarded = runCoins + missionCoins,
            progress = updatedProgress,
        )
    }

    fun themeCost(themeId: Int): Int = when (themeId) {
        3 -> 60
        4 -> 120
        5 -> 200
        else -> 0
    }

    fun unlockTheme(themeId: Int, coins: Int, unlockedMask: Int): ThemeUnlockResult {
        val bit = 1 shl themeId
        if (unlockedMask and bit != 0) {
            return ThemeUnlockResult(true, coins.coerceAtLeast(0), unlockedMask)
        }

        val cost = themeCost(themeId)
        val safeCoins = coins.coerceAtLeast(0)
        if (cost <= 0) {
            return ThemeUnlockResult(true, safeCoins, unlockedMask or bit)
        }
        if (safeCoins < cost) {
            return ThemeUnlockResult(false, safeCoins, unlockedMask)
        }
        return ThemeUnlockResult(true, safeCoins - cost, unlockedMask or bit)
    }
}
