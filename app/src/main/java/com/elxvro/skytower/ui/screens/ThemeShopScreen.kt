package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import com.elxvro.skytower.game.EconomyRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.ItemActionState
import com.elxvro.skytower.ui.ReferenceDesignTokens
import com.elxvro.skytower.ui.ScreenInteractionState
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.ThemePalette
import com.elxvro.skytower.ui.UiRect

class ThemeShopScreen(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()
    private val visibleThemeIds = listOf(0, 1, 2, 5, 4)

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        val back = l.referenceRect(32f, 42f, 95f, 86f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(770f, 44f, 275f, 82f).rf(), progress.coins)
        kit.drawRibbon(canvas, l.referenceRect(190f, 175f, 700f, 120f).rf(), "TEMALAR")

        val panel = l.referenceRect(42f, 300f, 996f, 1500f)
        kit.drawPanel(canvas, panel.rf())

        visibleThemeIds.take(4).forEachIndexed { index, themeId ->
            val col = index % 2
            val row = index / 2
            val x = 75f + col * 475f
            val y = 365f + row * 515f
            drawThemeCard(canvas, l, progress, themeId, x, y, 430f, 470f)
        }
        drawThemeCard(canvas, l, progress, visibleThemeIds.last(), 75f, 1400f, 905f, 325f, wide = true)
    }

    fun actionAt(x: Float, y: Float): ScreenAction =
        hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawThemeCard(
        canvas: Canvas,
        l: ScreenLayout,
        progress: PlayerProgress,
        themeId: Int,
        x: Float,
        y: Float,
        widthRef: Float,
        heightRef: Float,
        wide: Boolean = false,
    ) {
        val s = l.scale
        val theme = ThemePalette.get(themeId)
        val card = l.referenceRect(x, y, widthRef, heightRef)
        val selected = progress.themeId == themeId || (progress.themeId == 3 && themeId == 0)
        val owned = progress.unlockedThemeMask and (1 shl themeId) != 0
        val fill = when (themeId) {
            1 -> 0xFFF4774B.toInt()
            2 -> 0xFF3531A8.toInt()
            5 -> 0xFF157BD2.toInt()
            4 -> 0xFF3B2497.toInt()
            else -> ReferenceDesignTokens.BLUE
        }
        kit.drawBlueCard(canvas, card.rf(), fill)
        if (selected) {
            kit.paint.style = android.graphics.Paint.Style.STROKE
            kit.paint.strokeWidth = 8f * s
            kit.paint.color = ReferenceDesignTokens.GOLD
            canvas.drawRoundRect(card.rf(), 30f*s, 30f*s, kit.paint)
            kit.paint.style = android.graphics.Paint.Style.FILL
        }
        kit.drawOutlinedTitle(canvas, displayName(themeId), card.centerX, card.top + 56f*s, 38f*s, Color.WHITE)

        val preview = if (wide) {
            RectF(card.left + 22f*s, card.top + 78f*s, card.right - 250f*s, card.bottom - 28f*s)
        } else {
            RectF(card.left + 20f*s, card.top + 82f*s, card.right - 20f*s, card.top + 315f*s)
        }
        drawThemePreview(canvas, preview, themeId)

        if (wide) {
            val action = UiRect(card.right - 220f*s, card.bottom - 105f*s, card.right - 28f*s, card.bottom - 28f*s)
            drawAction(canvas, action, progress, themeId, selected, owned)
        } else {
            val action = UiRect(card.left + 45f*s, card.bottom - 105f*s, card.right - 45f*s, card.bottom - 25f*s)
            drawAction(canvas, action, progress, themeId, selected, owned)
        }
        hits += card to ScreenAction.Theme(themeId)
    }

    private fun drawAction(canvas: Canvas, action: UiRect, progress: PlayerProgress, themeId: Int, selected: Boolean, owned: Boolean) {
        val cost = EconomyRules.themeCost(themeId)
        val state = when {
            selected -> ItemActionState.SELECTED
            owned -> ItemActionState.SELECT
            else -> ScreenInteractionState.theme(themeId, progress.themeId, progress.unlockedThemeMask, progress.coins, cost)
        }
        when (state) {
            ItemActionState.SELECTED -> kit.drawButton(canvas, action.rf(), "KULLANILIYOR", enabled = false, green = true)
            ItemActionState.SELECT -> kit.drawButton(canvas, action.rf(), "SEÇ", green = true)
            ItemActionState.BUY -> kit.drawButton(canvas, action.rf(), "AÇ • $cost", green = true)
            ItemActionState.LOCKED -> kit.drawPrice(canvas, action.rf(), cost, locked = true)
            else -> Unit
        }
    }

    private fun drawThemePreview(canvas: Canvas, rect: RectF, themeId: Int) {
        val theme = ThemePalette.get(themeId)
        kit.paint.shader = LinearGradient(rect.left, rect.top, rect.left, rect.bottom, theme.skyTop, theme.skyBottom, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, rect.height()*.10f, rect.height()*.10f, kit.paint)
        kit.paint.shader = null
        kit.paint.color = theme.cloud
        canvas.drawCircle(rect.left+rect.width()*.23f, rect.top+rect.height()*.27f, rect.height()*.10f, kit.paint)
        canvas.drawCircle(rect.left+rect.width()*.78f, rect.top+rect.height()*.22f, rect.height()*.075f, kit.paint)
        val blockW = rect.width()*.35f
        val blockH = rect.height()*.115f
        val left = rect.centerX()-blockW/2f
        for (i in 0 until 5) {
            val bottom = rect.bottom-rect.height()*.08f-i*blockH
            kit.paint.color = theme.blocks[i % theme.blocks.size]
            canvas.drawRoundRect(RectF(left, bottom-blockH+2f, left+blockW, bottom), blockH*.15f, blockH*.15f, kit.paint)
        }
    }

    private fun displayName(themeId: Int): String = when (themeId) {
        0 -> "KLASİK"
        1 -> "GÜN BATIMI"
        2 -> "GECE"
        5 -> "AURORA"
        4 -> "UZAY"
        else -> ThemePalette.get(themeId).name.uppercase()
    }

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
