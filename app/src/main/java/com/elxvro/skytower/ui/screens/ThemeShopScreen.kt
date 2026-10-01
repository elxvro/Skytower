package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import com.elxvro.skytower.game.EconomyRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.ItemActionState
import com.elxvro.skytower.ui.ScreenInteractionState
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.ThemePalette
import com.elxvro.skytower.ui.UiRect

class ThemeShopScreen(
    private val kit: SkyVisualKit,
) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        kit.drawLogo(canvas, canvas.width.toFloat(), 145f * s)
        val back = l.referenceRect(45f, 60f, 110f, 90f)
        kit.drawBack(canvas, back.rf())
        hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(815f, 65f, 220f, 82f).rf(), progress.coins)
        kit.drawRibbon(canvas, l.referenceRect(250f, 285f, 580f, 105f).rf(), "TEMA MAĞAZASI")

        val panel = l.referenceRect(70f, 420f, 940f, 1325f)
        kit.drawPanel(canvas, panel.rf())
        val selectedTheme = ThemePalette.get(progress.themeId)
        val hero = l.referenceRect(120f, 470f, 840f, 350f)
        drawThemePreview(canvas, hero.rf(), progress.themeId)
        kit.drawTitle(canvas, selectedTheme.name.uppercase(), hero.left + hero.width / 2f, hero.bottom - 65f * s, 54f * s, 0xFFFFFFFF.toInt())
        kit.drawTitle(canvas, "Gökyüzünü renklendir", hero.left + hero.width / 2f, hero.bottom - 22f * s, 30f * s, 0xFFE8F6FF.toInt())

        ThemePalette.themes.forEachIndexed { index, theme ->
            val col = index % 2
            val row = index / 2
            val x = 120f + col * 420f
            val y = 860f + row * 245f
            val card = l.referenceRect(x, y, 390f, 215f)
            kit.drawBlueCard(canvas, card.rf(), 0xFF236EC7.toInt())
            val preview = RectF(card.left + 14f * s, card.top + 14f * s, card.right - 14f * s, card.top + 108f * s)
            drawThemePreview(canvas, preview, index)
            kit.drawTitle(canvas, theme.name.uppercase(), card.left + card.width / 2f, card.top + 142f * s, 28f * s, 0xFFFFFFFF.toInt())

            val cost = EconomyRules.themeCost(index)
            val state = ScreenInteractionState.theme(index, progress.themeId, progress.unlockedThemeMask, progress.coins, cost)
            val action = l.referenceRect(x + 50f, y + 158f, 290f, 45f)
            when (state) {
                ItemActionState.SELECTED -> kit.drawButton(canvas, action.rf(), "SEÇİLİ", enabled = false, green = false)
                ItemActionState.SELECT -> kit.drawButton(canvas, action.rf(), "SEÇ", green = true)
                ItemActionState.BUY -> kit.drawButton(canvas, action.rf(), "${cost} COIN", green = true)
                ItemActionState.LOCKED -> kit.drawPrice(canvas, action.rf(), cost, locked = true)
                else -> Unit
            }
            hits += card to ScreenAction.Theme(index)
        }
    }

    fun actionAt(x: Float, y: Float): ScreenAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawThemePreview(canvas: Canvas, rect: RectF, themeId: Int) {
        val theme = ThemePalette.get(themeId)
        kit.paint.shader = LinearGradient(rect.left, rect.top, rect.left, rect.bottom, theme.skyTop, theme.skyBottom, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, rect.height() * .14f, rect.height() * .14f, kit.paint)
        kit.paint.shader = null
        kit.paint.color = theme.cloud
        canvas.drawCircle(rect.left + rect.width() * .25f, rect.top + rect.height() * .35f, rect.height() * .10f, kit.paint)
        canvas.drawCircle(rect.left + rect.width() * .73f, rect.top + rect.height() * .23f, rect.height() * .075f, kit.paint)
    }

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
