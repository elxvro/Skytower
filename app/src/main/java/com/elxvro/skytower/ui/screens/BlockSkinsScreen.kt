package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.SkinRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.BlockSkinRenderer
import com.elxvro.skytower.ui.InventoryInteractionState
import com.elxvro.skytower.ui.ItemActionState
import com.elxvro.skytower.ui.ReferenceDesignTokens
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.ThemePalette
import com.elxvro.skytower.ui.UiRect

class BlockSkinsScreen(
    private val kit: SkyVisualKit,
    private val renderer: BlockSkinRenderer,
) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        val back = l.referenceRect(32f, 42f, 95f, 86f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(770f, 44f, 275f, 82f).rf(), progress.coins)
        kit.drawRibbon(canvas, l.referenceRect(200f, 180f, 680f, 125f).rf(), "SKİNLER")

        BlockSkin.entries.forEachIndexed { index, skin ->
            val col = index % 3
            val row = index / 3
            val x = 45f + col * 345f
            val y = 370f + row * 650f
            val card = l.referenceRect(x, y, 300f, 590f)
            val selected = progress.selectedSkin == skin
            kit.drawBlueCard(canvas, card.rf(), 0xFF1776C9.toInt())
            if (selected) {
                kit.paint.style = Paint.Style.STROKE
                kit.paint.strokeWidth = 8f*s
                kit.paint.color = ReferenceDesignTokens.ACTION_GREEN
                canvas.drawRoundRect(card.rf(), 34f*s, 34f*s, kit.paint)
                kit.paint.style = Paint.Style.FILL
            }
            kit.drawOutlinedTitle(canvas, skinName(skin), card.centerX, card.top+65f*s, 35f*s, Color.WHITE)

            val previewArea = RectF(card.left+35f*s, card.top+105f*s, card.right-35f*s, card.top+405f*s)
            drawSkinTower(canvas, previewArea, progress.themeId, skin)

            val button = UiRect(card.left+30f*s, card.bottom-105f*s, card.right-30f*s, card.bottom-25f*s)
            when (InventoryInteractionState.skin(skin, progress.selectedSkin, progress.unlockedSkinMask, progress.coins)) {
                ItemActionState.SELECTED -> kit.drawButton(canvas, button.rf(), "KULLANILIYOR", enabled = false, green = true)
                ItemActionState.SELECT -> kit.drawButton(canvas, button.rf(), "SEÇ", green = true)
                ItemActionState.BUY -> kit.drawButton(canvas, button.rf(), "AÇ • ${SkinRules.price(skin)}", green = true)
                ItemActionState.LOCKED -> kit.drawPrice(canvas, button.rf(), SkinRules.price(skin), locked = true)
                else -> Unit
            }
            hits += card to ScreenAction.Skin(skin)
        }
    }

    fun actionAt(x: Float, y: Float): ScreenAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawSkinTower(canvas: Canvas, area: RectF, themeId: Int, skin: BlockSkin) {
        val palette = ThemePalette.get(themeId).blocks
        val blocks = 6
        val blockH = area.height()/blocks
        for (i in 0 until blocks) {
            val inset = (i%2)*area.width()*.025f
            val bottom = area.bottom-i*blockH
            val rect = RectF(area.left+inset, bottom-blockH+3f, area.right-inset, bottom)
            renderer.draw(canvas, rect, palette[i%palette.size], skin)
        }
    }

    private fun skinName(skin: BlockSkin): String = when (skin) {
        BlockSkin.CLASSIC -> "KLASİK"
        BlockSkin.CRYSTAL -> "KRİSTAL"
        BlockSkin.MARBLE -> "MERMER"
        BlockSkin.GOLD -> "ALTIN"
        BlockSkin.ICE -> "BUZ"
        BlockSkin.NEON -> "NEON"
    }

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
