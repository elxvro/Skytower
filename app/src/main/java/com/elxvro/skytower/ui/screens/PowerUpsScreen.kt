package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.RectF
import com.elxvro.skytower.game.PowerUpRules
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.InventoryInteractionState
import com.elxvro.skytower.ui.PowerActionState
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
        kit.drawLogo(canvas, canvas.width.toFloat(), 145f * s)
        val back = l.referenceRect(45f, 60f, 110f, 90f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(815f, 65f, 220f, 82f).rf(), progress.coins)
        kit.drawRibbon(canvas, l.referenceRect(250f, 285f, 580f, 105f).rf(), "GÜÇLENDİRMELER")

        val panel = l.referenceRect(70f, 420f, 940f, 1290f)
        kit.drawPanel(canvas, panel.rf())
        val types = PowerUpType.entries
        types.forEachIndexed { index, type ->
            val y = 485f + index * 260f
            val card = l.referenceRect(115f, y, 850f, 225f)
            kit.drawBlueCard(canvas, card.rf(), when (type) {
                PowerUpType.SECOND_CHANCE -> 0xFF3566C7.toInt()
                PowerUpType.SLOW_TIME -> 0xFF2B75C9.toInt()
                PowerUpType.WIDE_PERFECT -> 0xFF6951C9.toInt()
                PowerUpType.COIN_MULTIPLIER -> 0xFF836018.toInt()
            })
            val iconX = card.left + 105f*s
            val iconY = card.centerY
            kit.paint.color = 0xFFFFD54A.toInt()
            canvas.drawCircle(iconX, iconY, 55f*s, kit.paint)
            kit.drawTitle(canvas, icon(type), iconX, iconY + 16f*s, 42f*s, 0xFF193A70.toInt())
            kit.drawTitle(canvas, title(type), card.left + 345f*s, card.top + 65f*s, 31f*s, 0xFFFFFFFF.toInt())
            kit.drawTitle(canvas, description(type), card.left + 345f*s, card.top + 108f*s, 22f*s, 0xFFDCEEFF.toInt())
            kit.drawTitle(canvas, "STOK ${progress.powerUpInventory.count(type)}", card.left + 300f*s, card.bottom - 37f*s, 22f*s, 0xFFFFE37B.toInt())

            val actionRect = UiRect(card.right - 250f*s, card.top + 128f*s, card.right - 28f*s, card.bottom - 28f*s)
            when (InventoryInteractionState.power(type, progress.powerUpInventory, progress.equippedPowerUps)) {
                PowerActionState.BUY -> {
                    kit.drawPrice(canvas, actionRect.rf(), PowerUpRules.price(type), locked = false)
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

        kit.drawTitle(canvas, "KUŞANILANLAR  ${progress.equippedPowerUps.size}/3", canvas.width*.5f, 1570f*s, 29f*s)
        val strip = l.referenceRect(180f, 1600f, 720f, 90f)
        kit.drawBlueCard(canvas, strip.rf(), 0xFF193F7D.toInt())
        val equipped = progress.equippedPowerUps.toList()
        for (slot in 0 until 3) {
            val cx = strip.left + (120f + slot*240f)*s
            val type = equipped.getOrNull(slot)
            kit.paint.color = if (type == null) 0xFF58749D.toInt() else 0xFFFFD54A.toInt()
            canvas.drawCircle(cx, strip.centerY, 32f*s, kit.paint)
            kit.drawTitle(canvas, type?.let(::icon) ?: "+", cx, strip.centerY + 10f*s, 25f*s, 0xFF17345F.toInt())
        }
    }

    fun actionAt(x: Float, y: Float): ScreenAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun title(type: PowerUpType): String = when (type) {
        PowerUpType.SECOND_CHANCE -> "İKİNCİ ŞANS"
        PowerUpType.SLOW_TIME -> "YAVAŞ ZAMAN"
        PowerUpType.WIDE_PERFECT -> "GENİŞ PERFECT"
        PowerUpType.COIN_MULTIPLIER -> "COIN ÇARPANI"
    }

    private fun description(type: PowerUpType): String = when (type) {
        PowerUpType.SECOND_CHANCE -> "İlk hatada otomatik kurtarır"
        PowerUpType.SLOW_TIME -> "8 sn boyunca hızı %35 azaltır"
        PowerUpType.WIDE_PERFECT -> "8 sn PERFECT alanını 1.75x yapar"
        PowerUpType.COIN_MULTIPLIER -> "Koşu coin ödülünü 2x yapar"
    }

    private fun icon(type: PowerUpType): String = when (type) {
        PowerUpType.SECOND_CHANCE -> "↻"
        PowerUpType.SLOW_TIME -> "◷"
        PowerUpType.WIDE_PERFECT -> "★"
        PowerUpType.COIN_MULTIPLIER -> "×2"
    }

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
