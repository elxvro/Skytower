package com.elxvro.skytower.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.elxvro.skytower.game.PowerUpInventory
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.game.RunPowerUpState

sealed interface GameplayHudAction {
    data object None : GameplayHudAction
    data object Pause : GameplayHudAction
    data object ActivateSlowTime : GameplayHudAction
    data object ActivateWidePerfect : GameplayHudAction
}

data class GameplayHudState(
    val score: Int,
    val bestScore: Int,
    val coins: Int,
    val level: Int,
    val levelProgress: Float,
    val placements: Int,
    val combo: Int,
    val perfectIntensity: Float,
    val runPowerUps: RunPowerUpState,
    val equipped: Set<PowerUpType>,
    val inventory: PowerUpInventory,
)

class GameplayHudRenderer(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, GameplayHudAction>>()

    fun draw(canvas: Canvas, state: GameplayHudState) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale

        val pause = l.referenceRect(28f, 42f, 92f, 86f)
        kit.drawButton(canvas, pause.rf(), "Ⅱ", green = false)
        hits += pause to GameplayHudAction.Pause

        kit.drawCoinCapsule(canvas, l.referenceRect(145f, 45f, 270f, 80f).rf(), state.coins)
        kit.drawLevelCapsule(canvas, l.referenceRect(445f, 45f, 270f, 80f).rf(), state.level, state.levelProgress)

        val scoreCard = l.referenceRect(790f, 35f, 245f, 185f)
        kit.drawBlueCard(canvas, scoreCard.rf(), 0xE51763AF.toInt())
        kit.drawTitle(canvas, "SKOR", scoreCard.centerX, scoreCard.top + 38f*s, 25f*s, Color.WHITE)
        kit.drawOutlinedTitle(canvas, state.score.toString(), scoreCard.centerX, scoreCard.top + 98f*s, 60f*s, ReferenceDesignTokens.GOLD)
        kit.paint.color = 0x99FFFFFF.toInt()
        canvas.drawRoundRect(RectF(scoreCard.left + 35f*s, scoreCard.top + 118f*s, scoreCard.right - 35f*s, scoreCard.top + 122f*s), 2f*s, 2f*s, kit.paint)
        kit.drawTitle(canvas, "EN İYİ ${state.bestScore}", scoreCard.centerX, scoreCard.bottom - 19f*s, 21f*s, Color.WHITE)

        drawHeightRail(canvas, l, state.placements)

        if (state.perfectIntensity > 0f) {
            val intensity = state.perfectIntensity.coerceIn(0f, 1f)
            kit.drawOutlinedTitle(
                canvas,
                "PERFECT!",
                790f*s,
                470f*s,
                (58f + 14f*intensity)*s,
                ReferenceDesignTokens.GOLD,
            )
        }
        if (state.combo >= 2) {
            val comboBox = l.referenceRect(685f, 520f, 315f, 145f)
            kit.drawBlueCard(canvas, comboBox.rf(), ReferenceDesignTokens.PURPLE)
            kit.drawTitle(canvas, "KOMBO", comboBox.centerX, comboBox.top + 48f*s, 34f*s, Color.WHITE)
            kit.drawOutlinedTitle(canvas, "X${state.combo}", comboBox.centerX, comboBox.bottom - 24f*s, 58f*s, ReferenceDesignTokens.GOLD)
        }

        drawPowerDock(canvas, l, state)
    }

    fun actionAt(x: Float, y: Float): GameplayHudAction =
        hits.lastOrNull { it.first.contains(x, y) }?.second ?: GameplayHudAction.None

    private fun drawHeightRail(canvas: Canvas, l: ScreenLayout, placements: Int) {
        val s = l.scale
        val rail = l.referenceRect(42f, 250f, 80f, 930f)
        val track = RectF(rail.centerX - 10f*s, rail.top + 50f*s, rail.centerX + 10f*s, rail.bottom - 35f*s)
        kit.paint.color = 0xCC245886.toInt()
        canvas.drawRoundRect(track, 10f*s, 10f*s, kit.paint)
        val fraction = (placements.coerceAtLeast(0) / 50f).coerceIn(0f, 1f)
        val fill = RectF(track.left + 2f*s, track.bottom - track.height()*fraction, track.right - 2f*s, track.bottom - 2f*s)
        kit.paint.color = 0xFF55E455.toInt()
        canvas.drawRoundRect(fill, 8f*s, 8f*s, kit.paint)
        listOf(10, 25, 50).forEach { milestone ->
            val cy = track.bottom - track.height()*(milestone/50f)
            kit.paint.color = if (placements >= milestone) ReferenceDesignTokens.GOLD else 0xFF9AA9BC.toInt()
            canvas.drawCircle(rail.centerX, cy, 20f*s, kit.paint)
        }
    }

    private fun drawPowerDock(canvas: Canvas, l: ScreenLayout, state: GameplayHudState) {
        val s = l.scale
        val entries = listOf(
            PowerDock(PowerUpType.SECOND_CHANCE, "İKİNCİ\nŞANS", ReferenceDesignTokens.PINK, null),
            PowerDock(PowerUpType.SLOW_TIME, "ZAMANI\nYAVAŞLAT", ReferenceDesignTokens.BLUE, GameplayHudAction.ActivateSlowTime),
            PowerDock(PowerUpType.WIDE_PERFECT, "GENİŞ\nPERFECT", ReferenceDesignTokens.PURPLE, GameplayHudAction.ActivateWidePerfect),
            PowerDock(PowerUpType.COIN_MULTIPLIER, "DAHA FAZLA\nALTIN", ReferenceDesignTokens.ORANGE, null),
        )
        entries.forEachIndexed { index, item ->
            val x = 44f + index*255f
            val card = l.referenceRect(x, 1640f, 225f, 210f)
            kit.drawBlueCard(canvas, card.rf(), item.color)
            val count = state.inventory.count(item.type)
            val badge = RectF(card.right - 52f*s, card.top - 18f*s, card.right + 8f*s, card.top + 42f*s)
            kit.drawBlueCard(canvas, badge, ReferenceDesignTokens.NAVY)
            kit.drawTitle(canvas, count.toString(), badge.centerX(), badge.centerY()+10f*s, 27f*s, Color.WHITE)
            drawPowerIcon(canvas, item.type, card.centerX, card.top + 72f*s, s)
            val lines = item.label.split("\n")
            lines.forEachIndexed { lineIndex, line ->
                kit.drawTitle(canvas, line, card.centerX, card.top + (145f + lineIndex*30f)*s, 23f*s, Color.WHITE)
            }
            val usable = item.type in state.equipped
            if (usable && item.action != null) {
                val active = when (item.type) {
                    PowerUpType.SLOW_TIME -> state.runPowerUps.slowTimeRemaining > 0f
                    PowerUpType.WIDE_PERFECT -> state.runPowerUps.widePerfectRemaining > 0f
                    else -> false
                }
                if (!active) hits += card to item.action
                else {
                    kit.paint.color = 0xAAFFFFFF.toInt()
                    canvas.drawRoundRect(RectF(card.left+20f*s, card.top+112f*s, card.right-20f*s, card.top+132f*s), 10f*s, 10f*s, kit.paint)
                    val remaining = if (item.type == PowerUpType.SLOW_TIME) state.runPowerUps.slowTimeRemaining else state.runPowerUps.widePerfectRemaining
                    val progress = (remaining/8f).coerceIn(0f,1f)
                    kit.paint.color = ReferenceDesignTokens.GOLD
                    canvas.drawRoundRect(RectF(card.left+20f*s, card.top+112f*s, card.left+20f*s+(card.width-40f*s)*progress, card.top+132f*s), 10f*s, 10f*s, kit.paint)
                }
            }
        }
    }

    private fun drawPowerIcon(canvas: Canvas, type: PowerUpType, cx: Float, cy: Float, s: Float) {
        when (type) {
            PowerUpType.SECOND_CHANCE -> {
                kit.paint.color = 0xFFFFF4F6.toInt()
                canvas.drawCircle(cx, cy, 34f*s, kit.paint)
                kit.paint.style = Paint.Style.STROKE
                kit.paint.strokeWidth = 8f*s
                kit.paint.color = 0xFFFF5C6E.toInt()
                canvas.drawCircle(cx, cy, 24f*s, kit.paint)
                kit.paint.style = Paint.Style.FILL
            }
            PowerUpType.SLOW_TIME -> {
                kit.paint.color = Color.WHITE
                canvas.drawCircle(cx, cy, 35f*s, kit.paint)
                kit.paint.style = Paint.Style.STROKE
                kit.paint.strokeWidth = 6f*s
                kit.paint.color = ReferenceDesignTokens.NAVY
                canvas.drawCircle(cx, cy, 28f*s, kit.paint)
                canvas.drawLine(cx, cy, cx, cy-16f*s, kit.paint)
                canvas.drawLine(cx, cy, cx+13f*s, cy+7f*s, kit.paint)
                kit.paint.style = Paint.Style.FILL
            }
            PowerUpType.WIDE_PERFECT -> {
                kit.paint.color = ReferenceDesignTokens.GOLD
                canvas.drawRoundRect(RectF(cx-42f*s, cy-16f*s, cx+42f*s, cy+16f*s), 8f*s, 8f*s, kit.paint)
                kit.drawTitle(canvas, "↔", cx, cy+11f*s, 42f*s, Color.WHITE)
            }
            PowerUpType.COIN_MULTIPLIER -> {
                kit.drawCoin(canvas, cx-18f*s, cy+8f*s, 24f*s)
                kit.drawCoin(canvas, cx+18f*s, cy-8f*s, 24f*s)
                kit.drawOutlinedTitle(canvas, "×2", cx, cy+52f*s, 30f*s, Color.WHITE)
            }
        }
    }

    private data class PowerDock(val type: PowerUpType, val label: String, val color: Int, val action: GameplayHudAction?)
    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
