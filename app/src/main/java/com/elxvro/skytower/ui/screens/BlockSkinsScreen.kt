package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.RectF
import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.SkinRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.BlockSkinRenderer
import com.elxvro.skytower.ui.InventoryInteractionState
import com.elxvro.skytower.ui.ItemActionState
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
        kit.drawLogo(canvas, canvas.width.toFloat(), 145f * s)
        val back = l.referenceRect(45f, 60f, 110f, 90f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(815f, 65f, 220f, 82f).rf(), progress.coins)
        kit.drawRibbon(canvas, l.referenceRect(250f, 285f, 580f, 105f).rf(), "SKİNLER")

        val panel = l.referenceRect(70f, 420f, 940f, 1290f)
        kit.drawPanel(canvas, panel.rf())
        val hero = l.referenceRect(120f, 470f, 840f, 300f)
        kit.drawBlueCard(canvas, hero.rf(), 0xFF2D70C7.toInt())
        kit.drawTitle(canvas, "SEÇİLİ SKİN", hero.centerX, hero.top + 50f * s, 29f * s, 0xFFDFF3FF.toInt())
        val heroBlock = RectF(hero.left + 145f*s, hero.top + 90f*s, hero.right - 145f*s, hero.bottom - 65f*s)
        renderer.draw(canvas, heroBlock, ThemePalette.get(progress.themeId).blocks[0], progress.selectedSkin)
        kit.drawTitle(canvas, skinName(progress.selectedSkin), hero.centerX, hero.bottom - 20f*s, 31f*s, 0xFFFFFFFF.toInt())

        BlockSkin.entries.forEachIndexed { index, skin ->
            val col = index % 2
            val row = index / 2
            val x = 120f + col * 420f
            val y = 820f + row * 250f
            val card = l.referenceRect(x, y, 390f, 225f)
            kit.drawBlueCard(canvas, card.rf(), 0xFF286DC2.toInt())
            val preview = RectF(card.left + 40f*s, card.top + 30f*s, card.right - 40f*s, card.top + 105f*s)
            renderer.draw(canvas, preview, ThemePalette.get(progress.themeId).blocks[index % 6], skin)
            kit.drawTitle(canvas, skinName(skin), card.centerX, card.top + 142f*s, 27f*s, 0xFFFFFFFF.toInt())
            val button = UiRect(card.left + 50f*s, card.top + 160f*s, card.right - 50f*s, card.bottom - 18f*s)
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
