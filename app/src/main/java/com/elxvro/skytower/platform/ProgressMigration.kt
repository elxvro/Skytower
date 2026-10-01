package com.elxvro.skytower.platform

import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.DailyMissionState
import com.elxvro.skytower.game.SkinRules

object ProgressMigration {
    fun migrate(
        legacy: LegacyProgressSnapshot,
        currentDate: String,
        existing: PlayerProgress? = null,
    ): PlayerProgress {
        val safeLegacyThemeMask = legacy.unlockedThemeMask.takeIf { it != 0 } ?: 0b111
        val safeThemeId = legacy.themeId.takeIf { it in 0..5 && safeLegacyThemeMask and (1 shl it) != 0 } ?: 0
        val previous = existing
        val existingSkinMask = previous?.unlockedSkinMask ?: 0
        val unlockedSkinMask = existingSkinMask or SkinRules.DEFAULT_UNLOCKED_MASK
        val selectedSkin = previous?.selectedSkin
            ?.takeIf { SkinRules.isOwned(it, unlockedSkinMask) }
            ?: BlockSkin.CLASSIC

        return PlayerProgress(
            bestScore = maxOf(legacy.bestScore.coerceAtLeast(0), previous?.bestScore?.coerceAtLeast(0) ?: 0),
            coins = if (previous == null) legacy.coins.coerceAtLeast(0) else previous.coins.coerceAtLeast(0),
            themeId = if (previous == null) safeThemeId else previous.themeId.takeIf {
                it in 0..5 && previous.unlockedThemeMask and (1 shl it) != 0
            } ?: 0,
            unlockedThemeMask = if (previous == null) safeLegacyThemeMask else previous.unlockedThemeMask.takeIf { it != 0 } ?: 0b111,
            soundEnabled = previous?.soundEnabled ?: legacy.soundEnabled,
            vibrationEnabled = previous?.vibrationEnabled ?: legacy.vibrationEnabled,
            tutorialSeen = previous?.tutorialSeen ?: legacy.tutorialSeen,
            selectedSkin = selectedSkin,
            unlockedSkinMask = unlockedSkinMask,
            powerUpInventory = previous?.powerUpInventory ?: com.elxvro.skytower.game.PowerUpInventory(),
            equippedPowerUps = previous?.equippedPowerUps.orEmpty().filterTo(linkedSetOf()) {
                (previous?.powerUpInventory?.count(it) ?: 0) > 0
            }.take(3).toSet(),
            totalXp = previous?.totalXp?.coerceAtLeast(0) ?: 0,
            claimedLevelRewards = previous?.claimedLevelRewards.orEmpty().filter { it >= 2 }.toSet(),
            totalRuns = previous?.totalRuns?.coerceAtLeast(0) ?: 0,
            totalPlacements = maxOf(previous?.totalPlacements?.coerceAtLeast(0) ?: 0, legacy.legacyPlacements.coerceAtLeast(0)),
            totalPerfects = maxOf(previous?.totalPerfects?.coerceAtLeast(0) ?: 0, legacy.legacyPerfects.coerceAtLeast(0)),
            dailyState = if (previous == null) {
                DailyMissionState(dateKey = currentDate)
            } else if (previous.dailyState.dateKey == currentDate) {
                previous.dailyState.copy(
                    placements = previous.dailyState.placements.coerceAtLeast(0),
                    perfects = previous.dailyState.perfects.coerceAtLeast(0),
                    bestScore = previous.dailyState.bestScore.coerceAtLeast(0),
                )
            } else {
                previous.dailyState.copy(
                    dateKey = currentDate,
                    placements = 0,
                    perfects = 0,
                    bestScore = 0,
                    claimedMask = 0,
                    allCompleteClaimed = false,
                )
            },
        )
    }
}
