package com.elxvro.skytower.ui.assets

import org.junit.Assert.assertEquals
import org.junit.Test

class UiAssetCatalogTest {
    @Test
    fun homePathsAreStable() {
        assertEquals("ui/home/background.webp", UiAssetCatalog.HOME_BACKGROUND)
        assertEquals("ui/home/logo.webp", UiAssetCatalog.HOME_LOGO)
        assertEquals("ui/home/tower_platform.webp", UiAssetCatalog.HOME_TOWER_PLATFORM)
        assertEquals("ui/home/button_play.webp", UiAssetCatalog.HOME_BUTTON_PLAY)
        assertEquals("ui/home/card_daily.webp", UiAssetCatalog.HOME_CARD_DAILY)
        assertEquals("ui/home/card_best_score.webp", UiAssetCatalog.HOME_CARD_BEST_SCORE)
        assertEquals("ui/home/card_themes.webp", UiAssetCatalog.HOME_CARD_THEMES)
        assertEquals("ui/home/card_tasks.webp", UiAssetCatalog.HOME_CARD_TASKS)
        assertEquals("ui/home/card_level.webp", UiAssetCatalog.HOME_CARD_LEVEL)
        assertEquals("ui/home/card_skins.webp", UiAssetCatalog.HOME_CARD_SKINS)
        assertEquals("ui/home/card_powerups.webp", UiAssetCatalog.HOME_CARD_POWERUPS)
        assertEquals("ui/home/card_settings.webp", UiAssetCatalog.HOME_CARD_SETTINGS)
    }

    @Test
    fun iconPathsAreStable() {
        assertEquals("ui/icons/coin.webp", UiAssetCatalog.ICON_COIN)
        assertEquals("ui/icons/crown.webp", UiAssetCatalog.ICON_CROWN)
        assertEquals("ui/icons/gift.webp", UiAssetCatalog.ICON_GIFT)
        assertEquals("ui/icons/trophy.webp", UiAssetCatalog.ICON_TROPHY)
        assertEquals("ui/icons/themes.webp", UiAssetCatalog.ICON_THEMES)
        assertEquals("ui/icons/tasks.webp", UiAssetCatalog.ICON_TASKS)
        assertEquals("ui/icons/level.webp", UiAssetCatalog.ICON_LEVEL)
        assertEquals("ui/icons/skins.webp", UiAssetCatalog.ICON_SKINS)
        assertEquals("ui/icons/powerups.webp", UiAssetCatalog.ICON_POWERUPS)
        assertEquals("ui/icons/settings.webp", UiAssetCatalog.ICON_SETTINGS)
    }
}
