package com.elxvro.skytower.platform

import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.DailyMissionState
import com.elxvro.skytower.game.PowerUpInventory
import com.elxvro.skytower.game.PowerUpType

data class PlayerProgress(
    val bestScore: Int = 0,
    val coins: Int = 0,
    val themeId: Int = 0,
    val unlockedThemeMask: Int = 0b111,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val tutorialSeen: Boolean = false,
    val selectedSkin: BlockSkin = BlockSkin.CLASSIC,
    val unlockedSkinMask: Int = 0,
    val powerUpInventory: PowerUpInventory = PowerUpInventory(),
    val equippedPowerUps: Set<PowerUpType> = emptySet(),
    val totalXp: Int = 0,
    val claimedLevelRewards: Set<Int> = emptySet(),
    val totalRuns: Int = 0,
    val totalPlacements: Int = 0,
    val totalPerfects: Int = 0,
    val dailyState: DailyMissionState = DailyMissionState(),
)

data class LegacyProgressSnapshot(
    val bestScore: Int = 0,
    val coins: Int = 0,
    val themeId: Int = 0,
    val unlockedThemeMask: Int = 0b111,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val tutorialSeen: Boolean = false,
    val legacyPlacements: Int = 0,
    val legacyPerfects: Int = 0,
    val legacyMissionClaimedMask: Int = 0,
)

data class RunSettlementV05(
    val runCoinsAwarded: Int,
    val xpAwarded: Int,
    val progress: PlayerProgress,
)
