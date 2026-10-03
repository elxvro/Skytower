package com.elxvro.skytower.game

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DailyMission {
    BLOCKS,
    PERFECTS,
    SCORE;

    val bit: Int get() = 1 shl ordinal
}

data class DailyMissionState(
    val dateKey: String = "",
    val placements: Int = 0,
    val perfects: Int = 0,
    val bestScore: Int = 0,
    val claimedMask: Int = 0,
    val allCompleteClaimed: Boolean = false,
    val streakCount: Int = 0,
    val lastStreakClaimDate: String = "",
)

data class DailyClaimResult(
    val state: DailyMissionState,
    val coinsAwarded: Int = 0,
    val streakBonusAwarded: Int = 0,
)

object DailyMissionRules {
    const val BLOCKS_TARGET = 10
    const val PERFECTS_TARGET = 3
    const val SCORE_TARGET = 50
    const val BLOCKS_REWARD = 20
    const val PERFECTS_REWARD = 25
    const val SCORE_REWARD = 40
    const val ALL_COMPLETE_REWARD = 100
    const val DAY_FIVE_STREAK_REWARD = 150
    val ALL_MISSIONS_MASK: Int = DailyMission.entries.fold(0) { mask, mission -> mask or mission.bit }

    fun update(
        state: DailyMissionState,
        dateKey: String,
        placements: Int,
        perfects: Int,
        score: Int,
    ): DailyMissionState {
        val incoming = parseDate(dateKey) ?: return state
        val current = parseDate(state.dateKey)
        if (current != null && incoming.isBefore(current)) return state

        val safePlacements = placements.coerceAtLeast(0)
        val safePerfects = perfects.coerceAtLeast(0)
        val safeScore = score.coerceAtLeast(0)

        return if (state.dateKey == dateKey) {
            state.copy(
                placements = state.placements.coerceAtLeast(0) + safePlacements,
                perfects = state.perfects.coerceAtLeast(0) + safePerfects,
                bestScore = maxOf(state.bestScore.coerceAtLeast(0), safeScore),
            )
        } else {
            state.copy(
                dateKey = dateKey,
                placements = safePlacements,
                perfects = safePerfects,
                bestScore = safeScore,
                claimedMask = 0,
                allCompleteClaimed = false,
            )
        }
    }

    fun claimMission(state: DailyMissionState, dateKey: String, mission: DailyMission): DailyClaimResult {
        if (state.dateKey != dateKey || state.claimedMask and mission.bit != 0 || !isComplete(state, mission)) {
            return DailyClaimResult(state)
        }

        val baseReward = rewardFor(mission)
        val streak = advanceStreak(state, dateKey)
        val updated = state.copy(
            claimedMask = state.claimedMask or mission.bit,
            streakCount = streak.count,
            lastStreakClaimDate = streak.lastClaimDate,
        )
        return DailyClaimResult(
            state = updated,
            coinsAwarded = baseReward,
            streakBonusAwarded = streak.bonus,
        )
    }

    fun claimAllComplete(state: DailyMissionState, dateKey: String): DailyClaimResult {
        if (state.dateKey != dateKey || state.allCompleteClaimed || state.claimedMask and ALL_MISSIONS_MASK != ALL_MISSIONS_MASK) {
            return DailyClaimResult(state)
        }
        return DailyClaimResult(
            state = state.copy(allCompleteClaimed = true),
            coinsAwarded = ALL_COMPLETE_REWARD,
        )
    }

    fun isComplete(state: DailyMissionState, mission: DailyMission): Boolean = when (mission) {
        DailyMission.BLOCKS -> state.placements >= BLOCKS_TARGET
        DailyMission.PERFECTS -> state.perfects >= PERFECTS_TARGET
        DailyMission.SCORE -> state.bestScore >= SCORE_TARGET
    }

    fun rewardFor(mission: DailyMission): Int = when (mission) {
        DailyMission.BLOCKS -> BLOCKS_REWARD
        DailyMission.PERFECTS -> PERFECTS_REWARD
        DailyMission.SCORE -> SCORE_REWARD
    }

    private data class StreakAdvance(val count: Int, val lastClaimDate: String, val bonus: Int)

    private fun advanceStreak(state: DailyMissionState, dateKey: String): StreakAdvance {
        if (state.lastStreakClaimDate == dateKey) {
            return StreakAdvance(state.streakCount.coerceIn(0, 5), state.lastStreakClaimDate, 0)
        }

        val today = parseDate(dateKey) ?: return StreakAdvance(state.streakCount.coerceIn(0, 5), state.lastStreakClaimDate, 0)
        val previous = parseDate(state.lastStreakClaimDate)
        if (previous != null && !today.isAfter(previous)) {
            return StreakAdvance(state.streakCount.coerceIn(0, 5), state.lastStreakClaimDate, 0)
        }

        val consecutive = previous != null && ChronoUnit.DAYS.between(previous, today) == 1L
        val next = when {
            !consecutive -> 1
            state.streakCount >= 5 -> 1
            else -> state.streakCount.coerceAtLeast(0) + 1
        }
        return StreakAdvance(next, dateKey, if (next == 5) DAY_FIVE_STREAK_REWARD else 0)
    }

    private fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()
}
