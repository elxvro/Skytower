package com.elxvro.skytower.ui

import com.elxvro.skytower.game.DailyMission
import com.elxvro.skytower.game.DailyMissionRules
import com.elxvro.skytower.game.DailyMissionState

enum class ItemActionState {
    SELECTED,
    SELECT,
    BUY,
    CLAIM,
    CLAIMED,
    LOCKED,
}

object ScreenInteractionState {
    fun theme(themeId: Int, selectedThemeId: Int, unlockedMask: Int, coins: Int, cost: Int): ItemActionState = when {
        themeId == selectedThemeId && unlockedMask and (1 shl themeId) != 0 -> ItemActionState.SELECTED
        unlockedMask and (1 shl themeId) != 0 -> ItemActionState.SELECT
        coins.coerceAtLeast(0) >= cost.coerceAtLeast(0) -> ItemActionState.BUY
        else -> ItemActionState.LOCKED
    }

    fun daily(state: DailyMissionState, mission: DailyMission): ItemActionState = when {
        state.claimedMask and mission.bit != 0 -> ItemActionState.CLAIMED
        DailyMissionRules.isComplete(state, mission) -> ItemActionState.CLAIM
        else -> ItemActionState.LOCKED
    }

    fun level(currentLevel: Int, rewardLevel: Int, claimed: Boolean): ItemActionState = when {
        claimed -> ItemActionState.CLAIMED
        currentLevel >= rewardLevel -> ItemActionState.CLAIM
        else -> ItemActionState.LOCKED
    }
}
