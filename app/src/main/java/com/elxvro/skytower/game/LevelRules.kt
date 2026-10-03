package com.elxvro.skytower.game

import kotlin.math.min

data class LevelState(
    val level: Int,
    val xpIntoLevel: Int,
    val xpRequired: Int,
)

data class LevelReward(
    val coins: Int = 0,
    val powerUp: PowerUpType? = null,
    val quantity: Int = 0,
)

data class LevelClaimResult(
    val claimed: Boolean,
    val reward: LevelReward,
    val claimedLevels: Set<Int>,
)

object LevelRules {
    fun runXp(stats: RunStats): Int =
        stats.placements.coerceAtLeast(0) * 10 +
            stats.perfects.coerceAtLeast(0) * 5 +
            min(stats.score.coerceAtLeast(0), 100)

    fun xpRequiredForNext(level: Int): Int = 400 + 50 * level.coerceAtLeast(1)

    fun totalXpToReachLevel(level: Int): Int {
        val target = level.coerceAtLeast(1)
        var total = 0
        for (current in 1 until target) total += xpRequiredForNext(current)
        return total
    }

    fun levelForXp(totalXp: Int): LevelState {
        var remaining = totalXp.coerceAtLeast(0)
        var level = 1
        while (remaining >= xpRequiredForNext(level)) {
            remaining -= xpRequiredForNext(level)
            level += 1
        }
        return LevelState(level, remaining, xpRequiredForNext(level))
    }

    fun rewardForLevel(level: Int): LevelReward = when (level) {
        2 -> LevelReward(coins = 50)
        3 -> LevelReward(coins = 75)
        4 -> LevelReward(powerUp = PowerUpType.SLOW_TIME, quantity = 1)
        5 -> LevelReward(coins = 100)
        6 -> LevelReward(powerUp = PowerUpType.WIDE_PERFECT, quantity = 1)
        in 7..Int.MAX_VALUE -> when ((level - 7) % 4) {
            0 -> LevelReward(coins = 125)
            1 -> LevelReward(powerUp = PowerUpType.SLOW_TIME, quantity = 1)
            2 -> LevelReward(coins = 150)
            else -> LevelReward(powerUp = PowerUpType.WIDE_PERFECT, quantity = 1)
        }
        else -> LevelReward()
    }

    fun claimReward(totalXp: Int, level: Int, claimedLevels: Set<Int>): LevelClaimResult {
        if (level < 2 || level in claimedLevels || levelForXp(totalXp).level < level) {
            return LevelClaimResult(false, LevelReward(), claimedLevels)
        }
        return LevelClaimResult(true, rewardForLevel(level), claimedLevels + level)
    }
}
