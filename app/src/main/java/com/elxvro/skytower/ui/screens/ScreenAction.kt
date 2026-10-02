package com.elxvro.skytower.ui.screens

import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.DailyMission
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.ui.AppScreen

sealed interface ScreenAction {
    data object None : ScreenAction
    data object Back : ScreenAction
    data object ToggleSound : ScreenAction
    data object ToggleVibration : ScreenAction
    data object ToggleTutorial : ScreenAction
    data object ResetData : ScreenAction
    data class Open(val screen: AppScreen) : ScreenAction
    data class Theme(val themeId: Int) : ScreenAction
    data class ClaimDaily(val mission: DailyMission) : ScreenAction
    data object ClaimDailyAll : ScreenAction
    data class ClaimLevel(val level: Int) : ScreenAction
    data class Skin(val skin: BlockSkin) : ScreenAction
    data class BuyPowerUp(val type: PowerUpType) : ScreenAction
    data class TogglePowerUp(val type: PowerUpType) : ScreenAction
}
