package com.elxvro.skytower.platform

import android.content.Context
import com.elxvro.skytower.game.EconomyRules
import com.elxvro.skytower.game.MissionProgress
import com.elxvro.skytower.game.RunStats
import com.elxvro.skytower.game.SettlementResult
import com.elxvro.skytower.game.ThemeUnlockResult

class GamePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var bestScore: Int
        get() = preferences.getInt(KEY_BEST_SCORE, 0)
        private set(value) = preferences.edit().putInt(KEY_BEST_SCORE, value).apply()

    var soundEnabled: Boolean
        get() = preferences.getBoolean(KEY_SOUND, true)
        set(value) = preferences.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = preferences.getBoolean(KEY_VIBRATION, true)
        set(value) = preferences.edit().putBoolean(KEY_VIBRATION, value).apply()

    var themeId: Int
        get() = preferences.getInt(KEY_THEME, 0)
        set(value) = preferences.edit().putInt(KEY_THEME, value.coerceAtLeast(0)).apply()

    var tutorialSeen: Boolean
        get() = preferences.getBoolean(KEY_TUTORIAL_SEEN, false)
        set(value) = preferences.edit().putBoolean(KEY_TUTORIAL_SEEN, value).apply()

    var coins: Int
        get() = preferences.getInt(KEY_COINS, 0)
        private set(value) = preferences.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    var unlockedThemeMask: Int
        get() = preferences.getInt(KEY_UNLOCKED_THEME_MASK, DEFAULT_UNLOCKED_THEME_MASK)
        private set(value) = preferences.edit().putInt(KEY_UNLOCKED_THEME_MASK, value).apply()

    val missionProgress: MissionProgress
        get() = MissionProgress(
            totalPlacements = preferences.getInt(KEY_MISSION_PLACEMENTS, 0),
            totalPerfects = preferences.getInt(KEY_MISSION_PERFECTS, 0),
            bestScore = preferences.getInt(KEY_MISSION_BEST_SCORE, 0),
            claimedMask = preferences.getInt(KEY_MISSION_CLAIMED_MASK, 0),
        )

    fun updateBestScore(score: Int): Boolean {
        if (score <= bestScore) return false
        bestScore = score
        return true
    }

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
                .apply()
        }
        return result
    }

    fun isThemeUnlocked(themeId: Int): Boolean {
        if (themeId !in 0..30) return false
        return unlockedThemeMask and (1 shl themeId) != 0
    }

    companion object {
        private const val PREFS_NAME = "skytower_preferences"
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
    }
}
