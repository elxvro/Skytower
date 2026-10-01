package com.elxvro.skytower.platform

import android.content.Context

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

    fun updateBestScore(score: Int): Boolean {
        if (score <= bestScore) return false
        bestScore = score
        return true
    }

    companion object {
        private const val PREFS_NAME = "skytower_preferences"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_THEME = "theme_id"
        private const val KEY_TUTORIAL_SEEN = "tutorial_seen"
    }
}
