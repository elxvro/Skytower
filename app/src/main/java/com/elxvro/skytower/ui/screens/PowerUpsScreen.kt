package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.elxvro.skytower.game.PowerUpRules
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.InventoryInteractionState
import com.elxvro.skytower.ui.PowerActionState
import com.elxvro.skytower.ui.ReferenceDesignTokens
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.UiRect

class PowerUpsScreen(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        val back = l.referenceRect(32f, 42f, 95f, 86f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(770f, 44f, 275f, 82f).rf(), progress.coins)
        kit.drawRibbon(
            canvas,
            l.referenceRect(145f, 175f, 790f, 125f).rf(),
            "GÜÇLENDİRMELER",
            ReferenceDesignTokens.ORANGE,
            0xFFB85B00.toInt(),
        )

        val types = PowerUpType.entries
        types.forEachIndexed { index, type ->
            val y = 355f + index * 300f
            val card = l.referenceRect(55f, y, 970f, 260f)
            kit.drawBlueCard(canvas, card.rf(), cardColor(type))
            val iconX = card.left + 150f*s
            val iconY = card.centerY
            drawIcon(canvas, type, iconX, iconY, s)

            kit.text.textAlign = android.graphics.Paint.Align.LEFT
            kit.drawOutlinedTitle(canvas, title(type), card.left + 285f*s, card.top + 70f*s, 35f*s, Color.WHITE)
            kit.drawTitle(canvas, description(type), card.left + 285f*s, card.top + 120f*s, 23f*s, 0xFF163D78.toInt())
            kit.text.textAlign = android.graphics.Paint.Align.CENTER

            val stock = l.referenceRect(720f, y + 22f, 245f, 70f)
            kit.drawBlueCard(canvas, stock.rf(), 0xC924477B.toInt())
            kit.drawTitle(canvas, "x${progress.powerUpInventory.count(type)}", stock.centerX, stock.centerY + 10f*s, 28f*s, Color.WHITE)

            val actionRect = l.referenceRect(685f, y + 135f, 285f, 92f)
            when (InventoryInteractionState.power(type, progress.powerUpInventory, progress.equippedPowerUps)) {
                PowerActionState.BUY -> {
                    kit.drawButton(canvas, actionRect.rf(), "${PowerUpRules.price(type)}  SATIN AL", green = true)
                    hits += actionRect to ScreenAction.BuyPowerUp(type)
                }
                PowerActionState.EQUIP -> {
                    kit.drawButton(canvas, actionRect.rf(), "KUŞAN", green = true)
                    hits += actionRect to ScreenAction.TogglePowerUp(type)
                }
                PowerActionState.EQUIPPED -> {
                    kit.drawButton(canvas, actionRect.rf(), "KUŞANILI", green = false)
                    hits += actionRect to ScreenAction.TogglePowerUp(type)
                }
            }
        }

        kit.drawTitle(canvas, "KUŞANILANLAR  ${progress.equippedPowerUps.size}/3", canvas.width*.5f, 1585f*s, 28f*s, ReferenceDesignTokens.TEXT)
        val strip = l.referenceRect(180f, 1620f, 720f, 92f)
        kit.drawBlueCard(canvas, strip.rf(), ReferenceDesignTokens.NAVY)
        val equipped = progress.equippedPowerUps.toList()
        repeat(3) { slot ->
            val cx = strip.left + (120f + slot*240f)*s
            val type = equipped.getOrNull(slot)
            kit.paint.color = if (type == null) 0xFF60799B.toInt() else ReferenceDesignTokens.GOLD
            canvas.drawCircle(cx, strip.centerY, 31f*s, kit.paint)
            if (type == null) kit.drawTitle(canvas, "+", cx, strip.centerY + 10f*s, 26f*s, Color.WHITE)
            else kit.drawTitle(canvas, shortIcon(type), cx, strip.centerY + 9f*s, 22f*s, ReferenceDesignTokens.NAVY_DARK)
        }
    }

    fun actionAt(x: Float, y: Float): ScreenAction =
        hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun cardColor(type: PowerUpType): Int = when (type) {
        PowerUpType.SECOND_CHANCE -> 0xFFFFD04B.toInt()
        PowerUpType.SLOW_TIME -> 0xFFAD67F2.toInt()
        PowerUpType.WIDE_PERFECT -> 0xFF40A9F5.toInt()
        PowerUpType.COIN_MULTIPLIER -> 0xFFF75BA0.toInt()
    }

    private fun title(type: PowerUpType): String = when (type) {
        PowerUpType.SECOND_CHANCE -> "İKİNCİ ŞANS"
        PowerUpType.SLOW_TIME -> "YAVAŞ ZAMAN"
        PowerUpType.WIDE_PERFECT -> "GENİŞ PERFECT"
        PowerUpType.COIN_MULTIPLIER -> "COIN ÇARPANI"
    }

    private fun description(type: PowerUpType): String = when (type) {
        PowerUpType.SECOND_CHANCE -> "Düştüğünde kaldığın yerden devam et"
        PowerUpType.SLOW_TIME -> "8 saniye boyunca hareketi yavaşlat"
        PowerUpType.WIDE_PERFECT -> "PERFECT alanını geçici olarak genişlet"
        PowerUpType.COIN_MULTIPLIER -> "Koşu coin ödülünü 2 katına çıkar"
    }

    private fun drawIcon(canvas: Canvas, type: PowerUpType, cx: Float, cy: Float, s: Float) {
        when (type) {
            PowerUpType.SECOND_CHANCE -> {
                kit.paint.color = 0xFFFF5362.toInt()
                canvas.drawCircle(cx, cy, 64f*s, kit.paint)
                kit.drawOutlinedTitle(canvas, "∞", cx, cy+20f*s, 58f*s, Color.WHITE, 0xFFA62A34.toInt())
            }
            PowerUpType.SLOW_TIME -> {
                kit.paint.color = Color.WHITE
                canvas.drawCircle(cx, cy, 63f*s, kit.paint)
                kit.paint.style = android.graphics.Paint.Style.STROKE
                kit.paint.strokeWidth = 8f*s
                kit.paint.color = ReferenceDesignTokens.BLUE_DARK
                canvas.drawCircle(cx, cy, 51f*s, kit.paint)
                canvas.drawLine(cx, cy, cx, cy-28f*s, kit.paint)
                canvas.drawLine(cx, cy, cx+23f*s, cy+13f*s, kit.paint)
                kit.paint.style = android.graphics.Paint.Style.FILL
            }
            PowerUpType.WIDE_PERFECT -> {
                kit.paint.color = 0xFFF14E65.toInt()
                canvas.drawRoundRect(RectF(cx-70f*s, cy-28f*s, cx+70f*s, cy+28f*s), 14f*s, 14f*s, kit.paint)
                kit.drawOutlinedTitle(canvas, "↔", cx, cy+17f*s, 56f*s, Color.WHITE)
            }
            PowerUpType.COIN_MULTIPLIER -> {
                kit.drawCoin(canvas, cx-32f*s, cy+10f*s, 38f*s)
                kit.drawCoin(canvas, cx+23f*s, cy-22f*s, 40f*s)
                kit.drawOutlinedTitle(canvas, "×2", cx+15f*s, cy+62f*s, 44f*s, ReferenceDesignTokens.GOLD)
            }
        }
    }

    private fun shortIcon(type: PowerUpType): String = when (type) {
        PowerUpType.SECOND_CHANCE -> "∞"
        PowerUpType.SLOW_TIME -> "◷"
        PowerUpType.WIDE_PERFECT -> "↔"
        PowerUpType.COIN_MULTIPLIER -> "×2"
    }

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
