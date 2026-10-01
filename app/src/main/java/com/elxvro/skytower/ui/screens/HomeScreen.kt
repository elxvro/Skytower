package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.RectF
import com.elxvro.skytower.game.LevelRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.AppScreen
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.UiRect

class HomeScreen(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        kit.drawLogo(canvas, canvas.width.toFloat(), 175f*s)
        kit.drawCoinCapsule(canvas, l.referenceRect(815f, 65f, 220f, 82f).rf(), progress.coins)

        val level = LevelRules.levelForXp(progress.totalXp)
        val stats = l.referenceRect(140f, 370f, 800f, 150f)
        kit.drawBlueCard(canvas, stats.rf(), 0xCC245DA8.toInt())
        kit.drawTitle(canvas, "EN İYİ ${progress.bestScore}", stats.left + 190f*s, stats.centerY + 7f*s, 31f*s, 0xFFFFFFFF.toInt())
        kit.drawTitle(canvas, "SEVİYE ${level.level}", stats.right - 185f*s, stats.centerY + 7f*s, 31f*s, 0xFFFFD54A.toInt())

        val play = l.referenceRect(210f, 565f, 660f, 165f)
        kit.drawButton(canvas, play.rf(), "OYNA", green = true)
        hits += play to ScreenAction.Open(AppScreen.GAMEPLAY)

        val items = listOf(
            Triple("TEMA MAĞAZASI", AppScreen.THEME_SHOP, 0xFF7054D8.toInt()),
            Triple("GÜNLÜK GÖREVLER", AppScreen.DAILY_MISSIONS, 0xFF2C78D8.toInt()),
            Triple("SEVİYE SİSTEMİ", AppScreen.LEVELS, 0xFF6650CC.toInt()),
            Triple("BLOK SKİNLERİ", AppScreen.BLOCK_SKINS, 0xFF2875C9.toInt()),
            Triple("GÜÇLENDİRMELER", AppScreen.POWER_UPS, 0xFF7050BF.toInt()),
        )
        items.forEachIndexed { index, item ->
            val y = 800f + index * 145f
            val rect = l.referenceRect(170f, y, 740f, 115f)
            kit.drawBlueCard(canvas, rect.rf(), item.third)
            kit.drawTitle(canvas, item.first, rect.centerX, rect.centerY + 10f*s, 31f*s, 0xFFFFFFFF.toInt())
            hits += rect to ScreenAction.Open(item.second)
        }

        val sound = l.referenceRect(180f, 1550f, 330f, 95f)
        val vibration = l.referenceRect(570f, 1550f, 330f, 95f)
        kit.drawButton(canvas, sound.rf(), if (progress.soundEnabled) "SES AÇIK" else "SES KAPALI", green = progress.soundEnabled)
        kit.drawButton(canvas, vibration.rf(), if (progress.vibrationEnabled) "TİTREŞİM AÇIK" else "TİTREŞİM KAPALI", green = progress.vibrationEnabled)
        hits += sound to ScreenAction.ToggleSound
        hits += vibration to ScreenAction.ToggleVibration
        kit.drawTitle(canvas, "v0.5", canvas.width*.5f, 1740f*s, 25f*s, 0xDDFFFFFF.toInt())
    }

    fun actionAt(x: Float, y: Float): ScreenAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None
    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
