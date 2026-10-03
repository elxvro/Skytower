package com.elxvro.skytower.platform

import android.content.Context
import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.DailyClaimResult
import com.elxvro.skytower.game.DailyMission
import com.elxvro.skytower.game.DailyMissionRules
import com.elxvro.skytower.game.DailyMissionState
import com.elxvro.skytower.game.EconomyRules
import com.elxvro.skytower.game.LevelClaimResult
import com.elxvro.skytower.game.LevelRules
import com.elxvro.skytower.game.MissionProgress
import com.elxvro.skytower.game.PowerUpEquipResult
import com.elxvro.skytower.game.PowerUpInventory
import com.elxvro.skytower.game.PowerUpPurchaseResult
import com.elxvro.skytower.game.PowerUpRules
import com.elxvro.skytower.game.PowerUpShopState
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.game.RunRewardRules
import com.elxvro.skytower.game.RunStats
import com.elxvro.skytower.game.SettlementResult
import com.elxvro.skytower.game.SkinPurchaseResult
import com.elxvro.skytower.game.SkinRules
import com.elxvro.skytower.game.ThemeUnlockResult
import java.time.LocalDate

class GamePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var bestScore: Int
        get() = preferences.getInt(KEY_BEST_SCORE, 0).coerceAtLeast(0)
        private set(value) = preferences.edit().putInt(KEY_BEST_SCORE, value.coerceAtLeast(0)).apply()

    var soundEnabled: Boolean
        get() = preferences.getBoolean(KEY_SOUND, true)
        set(value) = preferences.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = preferences.getBoolean(KEY_VIBRATION, true)
        set(value) = preferences.edit().putBoolean(KEY_VIBRATION, value).apply()

    var themeId: Int
        get() = preferences.getInt(KEY_THEME, 0).coerceIn(0, 5)
        set(value) = preferences.edit().putInt(KEY_THEME, value.coerceIn(0, 5)).apply()

    var tutorialSeen: Boolean
        get() = preferences.getBoolean(KEY_TUTORIAL_SEEN, false)
        set(value) = preferences.edit().putBoolean(KEY_TUTORIAL_SEEN, value).apply()

    var coins: Int
        get() = preferences.getInt(KEY_COINS, 0).coerceAtLeast(0)
        private set(value) = preferences.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    var unlockedThemeMask: Int
        get() = preferences.getInt(KEY_UNLOCKED_THEME_MASK, DEFAULT_UNLOCKED_THEME_MASK).let { if (it == 0) DEFAULT_UNLOCKED_THEME_MASK else it }
        private set(value) = preferences.edit().putInt(KEY_UNLOCKED_THEME_MASK, if (value == 0) DEFAULT_UNLOCKED_THEME_MASK else value).apply()

    val missionProgress: MissionProgress
        get() = MissionProgress(
            totalPlacements = preferences.getInt(KEY_MISSION_PLACEMENTS, 0).coerceAtLeast(0),
            totalPerfects = preferences.getInt(KEY_MISSION_PERFECTS, 0).coerceAtLeast(0),
            bestScore = preferences.getInt(KEY_MISSION_BEST_SCORE, 0).coerceAtLeast(0),
            claimedMask = preferences.getInt(KEY_MISSION_CLAIMED_MASK, 0),
        )

    fun updateBestScore(score: Int): Boolean {
        if (score <= bestScore) return false
        bestScore = score
        return true
    }

    /** Legacy v0.4 settlement kept until all callers move to settleRunV05. */
    fun settleRun(stats: RunStats): SettlementResult {
        val result = EconomyRules.settle(stats, missionProgress)
        val newCoins = (coins + result.totalCoinsAwarded).coerceAtLeast(0)
        preferences.edit()
            .putInt(KEY_COINS, newCoins)
            .putInt(KEY_MISSION_PLACEMENTS, result.progress.totalPlacements)
            .putInt(KEY_MISSION_PERFECTS, result.progress.totalPerfects)
            .putInt(KEY_MISSION_BEST_SCORE, result.progress.bestScore)
            .putInt(KEY_MISSION_CLAIMED_MASK, result.progress.claimedMask)
            .apply()
        return result
    }

    fun tryUnlockTheme(themeId: Int): ThemeUnlockResult {
        val result = EconomyRules.unlockTheme(themeId, coins, unlockedThemeMask)
        if (result.unlocked) {
            preferences.edit()
                .putInt(KEY_COINS, result.coins)
                .putInt(KEY_UNLOCKED_THEME_MASK, result.unlockedMask)
                .putInt(KEY_THEME, themeId.coerceIn(0, 5))
                .apply()
        }
        return result
    }

    fun isThemeUnlocked(themeId: Int): Boolean {
        if (themeId !in 0..30) return false
        return unlockedThemeMask and (1 shl themeId) != 0
    }

    @Synchronized
    fun playerProgress(currentDate: String = todayKey()): PlayerProgress {
        ensureV05Migration(currentDate)
        val raw = readV05Progress()
        val refreshedDaily = DailyMissionRules.update(raw.dailyState, currentDate, 0, 0, 0)
        val sanitized = raw.copy(dailyState = refreshedDaily)
        if (sanitized != raw) writeV05Progress(sanitized)
        return sanitized
    }

    @Synchronized
    fun settleRunV05(
        stats: RunStats,
        currentDate: String = todayKey(),
        coinMultiplier: Boolean = false,
    ): RunSettlementV05 {
        val current = playerProgress(currentDate)
        val runCoins = RunRewardRules.calculate(
            runCoins = EconomyRules.runCoins(stats),
            missionCoins = 0,
            streakCoins = 0,
            levelCoins = 0,
            coinMultiplier = coinMultiplier,
        ).runCoins
        val xp = LevelRules.runXp(stats)
        val daily = DailyMissionRules.update(
            current.dailyState,
            currentDate,
            stats.placements,
            stats.perfects,
            stats.score,
        )
        val updated = current.copy(
            bestScore = maxOf(current.bestScore, stats.score.coerceAtLeast(0)),
            coins = current.coins + runCoins,
            totalXp = current.totalXp + xp,
            totalRuns = current.totalRuns + 1,
            totalPlacements = current.totalPlacements + stats.placements.coerceAtLeast(0),
            totalPerfects = current.totalPerfects + stats.perfects.coerceAtLeast(0),
            dailyState = daily,
        )
        writeV05Progress(updated)
        return RunSettlementV05(runCoins, xp, updated)
    }

    @Synchronized
    fun claimDailyMission(mission: DailyMission, currentDate: String = todayKey()): DailyClaimResult {
        val current = playerProgress(currentDate)
        val result = DailyMissionRules.claimMission(current.dailyState, currentDate, mission)
        if (result.state != current.dailyState || result.coinsAwarded != 0 || result.streakBonusAwarded != 0) {
            writeV05Progress(
                current.copy(
                    coins = current.coins + result.coinsAwarded + result.streakBonusAwarded,
                    dailyState = result.state,
                ),
            )
        }
        return result
    }

    @Synchronized
    fun claimDailyAll(currentDate: String = todayKey()): DailyClaimResult {
        val current = playerProgress(currentDate)
        val result = DailyMissionRules.claimAllComplete(current.dailyState, currentDate)
        if (result.state != current.dailyState || result.coinsAwarded != 0) {
            writeV05Progress(current.copy(coins = current.coins + result.coinsAwarded, dailyState = result.state))
        }
        return result
    }

    @Synchronized
    fun claimLevelReward(level: Int, currentDate: String = todayKey()): LevelClaimResult {
        val current = playerProgress(currentDate)
        val result = LevelRules.claimReward(current.totalXp, level, current.claimedLevelRewards)
        if (!result.claimed) return result
        var inventory = current.powerUpInventory
        result.reward.powerUp?.let { type ->
            inventory = inventory.withCount(type, inventory.count(type) + result.reward.quantity.coerceAtLeast(0))
        }
        writeV05Progress(
            current.copy(
                coins = current.coins + result.reward.coins.coerceAtLeast(0),
                powerUpInventory = inventory,
                claimedLevelRewards = result.claimedLevels,
            ),
        )
        return result
    }

    @Synchronized
    fun purchaseSkin(skin: BlockSkin, currentDate: String = todayKey()): SkinPurchaseResult {
        val current = playerProgress(currentDate)
        val result = SkinRules.purchase(skin, current.coins, current.unlockedSkinMask, current.selectedSkin)
        if (result.purchased || result.selected != current.selectedSkin) {
            writeV05Progress(
                current.copy(
                    coins = result.coins,
                    unlockedSkinMask = result.unlockedMask,
                    selectedSkin = result.selected,
                ),
            )
        }
        return result
    }

    @Synchronized
    fun selectSkin(skin: BlockSkin, currentDate: String = todayKey()): BlockSkin {
        val current = playerProgress(currentDate)
        val selected = SkinRules.select(skin, current.unlockedSkinMask, current.selectedSkin)
        if (selected != current.selectedSkin) writeV05Progress(current.copy(selectedSkin = selected))
        return selected
    }

    @Synchronized
    fun purchasePowerUp(
        type: PowerUpType,
        requestToken: String,
        currentDate: String = todayKey(),
    ): PowerUpPurchaseResult {
        val current = playerProgress(currentDate)
        val state = PowerUpShopState(
            coins = current.coins,
            inventory = current.powerUpInventory,
            equipped = current.equippedPowerUps,
            processedPurchaseTokens = preferences.getStringSet(KEY_POWER_PROCESSED_TOKENS, emptySet()).orEmpty(),
        )
        val result = PowerUpRules.purchase(state, type, requestToken)
        if (result.purchased) {
            writeV05Progress(
                current.copy(
                    coins = result.state.coins,
                    powerUpInventory = result.state.inventory,
                    equippedPowerUps = result.state.equipped,
                ),
            )
            preferences.edit().putStringSet(KEY_POWER_PROCESSED_TOKENS, result.state.processedPurchaseTokens.takeLast(20).toSet()).apply()
        }
        return result
    }

    @Synchronized
    fun toggleEquipPowerUp(type: PowerUpType, currentDate: String = todayKey()): PowerUpEquipResult {
        val current = playerProgress(currentDate)
        val result = PowerUpRules.toggleEquip(
            PowerUpShopState(current.coins, current.powerUpInventory, current.equippedPowerUps),
            type,
        )
        if (result.changed) writeV05Progress(current.copy(equippedPowerUps = result.state.equipped))
        return result
    }

    @Synchronized
    fun consumePowerUp(type: PowerUpType, currentDate: String = todayKey()): Boolean {
        val current = playerProgress(currentDate)
        val result = PowerUpRules.consume(
            PowerUpShopState(current.coins, current.powerUpInventory, current.equippedPowerUps),
            type,
        )
        if (!result.consumed) return false
        writeV05Progress(
            current.copy(
                powerUpInventory = result.state.inventory,
                equippedPowerUps = result.state.equipped,
            ),
        )
        return true
    }

    private fun ensureV05Migration(currentDate: String) {
        if (preferences.getInt(KEY_SCHEMA_VERSION, 4) >= SCHEMA_V05) return
        val legacy = LegacyProgressSnapshot(
            bestScore = preferences.getInt(KEY_BEST_SCORE, 0),
            coins = preferences.getInt(KEY_COINS, 0),
            themeId = preferences.getInt(KEY_THEME, 0),
            unlockedThemeMask = preferences.getInt(KEY_UNLOCKED_THEME_MASK, DEFAULT_UNLOCKED_THEME_MASK),
            soundEnabled = preferences.getBoolean(KEY_SOUND, true),
            vibrationEnabled = preferences.getBoolean(KEY_VIBRATION, true),
            tutorialSeen = preferences.getBoolean(KEY_TUTORIAL_SEEN, false),
            legacyPlacements = preferences.getInt(KEY_MISSION_PLACEMENTS, 0),
            legacyPerfects = preferences.getInt(KEY_MISSION_PERFECTS, 0),
            legacyMissionClaimedMask = preferences.getInt(KEY_MISSION_CLAIMED_MASK, 0),
        )
        writeV05Progress(ProgressMigration.migrate(legacy, currentDate))
        preferences.edit().putInt(KEY_SCHEMA_VERSION, SCHEMA_V05).apply()
    }

    private fun readV05Progress(): PlayerProgress {
        val inventory = PowerUpInventory(
            secondChance = preferences.getInt(KEY_POWER_SECOND, 0).coerceAtLeast(0),
            slowTime = preferences.getInt(KEY_POWER_SLOW, 0).coerceAtLeast(0),
            widePerfect = preferences.getInt(KEY_POWER_WIDE, 0).coerceAtLeast(0),
            coinMultiplier = preferences.getInt(KEY_POWER_MULTIPLIER, 0).coerceAtLeast(0),
        )
        val equipped = preferences.getStringSet(KEY_POWER_EQUIPPED, emptySet()).orEmpty()
            .mapNotNull { runCatching { PowerUpType.valueOf(it) }.getOrNull() }
            .filter { inventory.count(it) > 0 }
            .take(3)
            .toSet()
        val unlockedSkinMask = preferences.getInt(KEY_SKIN_MASK, SkinRules.DEFAULT_UNLOCKED_MASK) or SkinRules.DEFAULT_UNLOCKED_MASK
        val selectedSkin = runCatching {
            BlockSkin.valueOf(preferences.getString(KEY_SKIN_SELECTED, BlockSkin.CLASSIC.name) ?: BlockSkin.CLASSIC.name)
        }.getOrDefault(BlockSkin.CLASSIC).let {
            if (SkinRules.isOwned(it, unlockedSkinMask)) it else BlockSkin.CLASSIC
        }
        val claimedLevels = preferences.getStringSet(KEY_LEVEL_CLAIMS, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it >= 2 }.toSet()
        return PlayerProgress(
            bestScore = bestScore,
            coins = coins,
            themeId = themeId,
            unlockedThemeMask = unlockedThemeMask,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            tutorialSeen = tutorialSeen,
            selectedSkin = selectedSkin,
            unlockedSkinMask = unlockedSkinMask,
            powerUpInventory = inventory,
            equippedPowerUps = equipped,
            totalXp = preferences.getInt(KEY_TOTAL_XP, 0).coerceAtLeast(0),
            claimedLevelRewards = claimedLevels,
            totalRuns = preferences.getInt(KEY_TOTAL_RUNS, 0).coerceAtLeast(0),
            totalPlacements = preferences.getInt(KEY_TOTAL_PLACEMENTS, 0).coerceAtLeast(0),
            totalPerfects = preferences.getInt(KEY_TOTAL_PERFECTS, 0).coerceAtLeast(0),
            dailyState = DailyMissionState(
                dateKey = preferences.getString(KEY_DAILY_DATE, "").orEmpty(),
                placements = preferences.getInt(KEY_DAILY_PLACEMENTS, 0).coerceAtLeast(0),
                perfects = preferences.getInt(KEY_DAILY_PERFECTS, 0).coerceAtLeast(0),
                bestScore = preferences.getInt(KEY_DAILY_BEST, 0).coerceAtLeast(0),
                claimedMask = preferences.getInt(KEY_DAILY_CLAIMS, 0),
                allCompleteClaimed = preferences.getBoolean(KEY_DAILY_ALL_CLAIMED, false),
                streakCount = preferences.getInt(KEY_STREAK_COUNT, 0).coerceIn(0, 5),
                lastStreakClaimDate = preferences.getString(KEY_STREAK_LAST_DATE, "").orEmpty(),
            ),
        )
    }

    private fun writeV05Progress(progress: PlayerProgress) {
        preferences.edit()
            .putInt(KEY_BEST_SCORE, progress.bestScore.coerceAtLeast(0))
            .putInt(KEY_COINS, progress.coins.coerceAtLeast(0))
            .putInt(KEY_THEME, progress.themeId.coerceIn(0, 5))
            .putInt(KEY_UNLOCKED_THEME_MASK, if (progress.unlockedThemeMask == 0) DEFAULT_UNLOCKED_THEME_MASK else progress.unlockedThemeMask)
            .putBoolean(KEY_SOUND, progress.soundEnabled)
            .putBoolean(KEY_VIBRATION, progress.vibrationEnabled)
            .putBoolean(KEY_TUTORIAL_SEEN, progress.tutorialSeen)
            .putString(KEY_SKIN_SELECTED, progress.selectedSkin.name)
            .putInt(KEY_SKIN_MASK, progress.unlockedSkinMask or SkinRules.DEFAULT_UNLOCKED_MASK)
            .putInt(KEY_POWER_SECOND, progress.powerUpInventory.count(PowerUpType.SECOND_CHANCE))
            .putInt(KEY_POWER_SLOW, progress.powerUpInventory.count(PowerUpType.SLOW_TIME))
            .putInt(KEY_POWER_WIDE, progress.powerUpInventory.count(PowerUpType.WIDE_PERFECT))
            .putInt(KEY_POWER_MULTIPLIER, progress.powerUpInventory.count(PowerUpType.COIN_MULTIPLIER))
            .putStringSet(KEY_POWER_EQUIPPED, progress.equippedPowerUps.take(3).map { it.name }.toSet())
            .putInt(KEY_TOTAL_XP, progress.totalXp.coerceAtLeast(0))
            .putStringSet(KEY_LEVEL_CLAIMS, progress.claimedLevelRewards.filter { it >= 2 }.map { it.toString() }.toSet())
            .putInt(KEY_TOTAL_RUNS, progress.totalRuns.coerceAtLeast(0))
            .putInt(KEY_TOTAL_PLACEMENTS, progress.totalPlacements.coerceAtLeast(0))
            .putInt(KEY_TOTAL_PERFECTS, progress.totalPerfects.coerceAtLeast(0))
            .putString(KEY_DAILY_DATE, progress.dailyState.dateKey)
            .putInt(KEY_DAILY_PLACEMENTS, progress.dailyState.placements.coerceAtLeast(0))
            .putInt(KEY_DAILY_PERFECTS, progress.dailyState.perfects.coerceAtLeast(0))
            .putInt(KEY_DAILY_BEST, progress.dailyState.bestScore.coerceAtLeast(0))
            .putInt(KEY_DAILY_CLAIMS, progress.dailyState.claimedMask)
            .putBoolean(KEY_DAILY_ALL_CLAIMED, progress.dailyState.allCompleteClaimed)
            .putInt(KEY_STREAK_COUNT, progress.dailyState.streakCount.coerceIn(0, 5))
            .putString(KEY_STREAK_LAST_DATE, progress.dailyState.lastStreakClaimDate)
            .apply()
    }

    private fun todayKey(): String = LocalDate.now().toString()

    companion object {
        private const val PREFS_NAME = "skytower_preferences"
        private const val SCHEMA_V05 = 5
        private const val KEY_SCHEMA_VERSION = "schema_version"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_THEME = "theme_id"
        private const val KEY_TUTORIAL_SEEN = "tutorial_seen"
        private const val KEY_COINS = "coins"
        private const val KEY_UNLOCKED_THEME_MASK = "unlocked_theme_mask"
        private const val KEY_MISSION_PLACEMENTS = "mission_placements"
        private const val KEY_MISSION_PERFECTS = "mission_perfects"
        private const val KEY_MISSION_BEST_SCORE = "mission_best_score"
        private const val KEY_MISSION_CLAIMED_MASK = "mission_claimed_mask"
        private const val DEFAULT_UNLOCKED_THEME_MASK = 0b111
        private const val KEY_SKIN_SELECTED = "skin_selected"
        private const val KEY_SKIN_MASK = "skin_mask"
        private const val KEY_POWER_SECOND = "power_second"
        private const val KEY_POWER_SLOW = "power_slow"
        private const val KEY_POWER_WIDE = "power_wide"
        private const val KEY_POWER_MULTIPLIER = "power_multiplier"
        private const val KEY_POWER_EQUIPPED = "power_equipped"
        private const val KEY_POWER_PROCESSED_TOKENS = "power_purchase_tokens"
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_LEVEL_CLAIMS = "level_claims"
        private const val KEY_TOTAL_RUNS = "total_runs"
        private const val KEY_TOTAL_PLACEMENTS = "total_placements"
        private const val KEY_TOTAL_PERFECTS = "total_perfects"
        private const val KEY_DAILY_DATE = "daily_date"
        private const val KEY_DAILY_PLACEMENTS = "daily_placements"
        private const val KEY_DAILY_PERFECTS = "daily_perfects"
        private const val KEY_DAILY_BEST = "daily_best"
        private const val KEY_DAILY_CLAIMS = "daily_claims"
        private const val KEY_DAILY_ALL_CLAIMED = "daily_all_claimed"
        private const val KEY_STREAK_COUNT = "streak_count"
        private const val KEY_STREAK_LAST_DATE = "streak_last_date"
    }
}
