package com.elxvro.skytower.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
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
    val placements: Int,
    val combo: Int,
    val perfectIntensity: Float,
    val runPowerUps: RunPowerUpState,
    val equipped: Set<PowerUpType>,
)

class GameplayHudRenderer(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, GameplayHudAction>>()

    fun draw(canvas: Canvas, state: GameplayHudState) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        val scoreCard = l.referenceRect(315f, 45f, 450f, 170f)
        kit.drawBlueCard(canvas, scoreCard.rf(), 0xCC184F9E.toInt())
        kit.drawTitle(canvas, "SKOR", scoreCard.centerX, scoreCard.top + 48f*s, 29f*s, 0xFFDFF3FF.toInt())
        kit.drawTitle(canvas, state.score.toString(), scoreCard.centerX, scoreCard.top + 118f*s, 62f*s, Color.WHITE)
        kit.drawTitle(canvas, "EN İYİ ${state.bestScore}", scoreCard.centerX, scoreCard.bottom - 14f*s, 21f*s, 0xFFFFD54A.toInt())

        val coinRect = l.referenceRect(795f, 55f, 210f, 75f)
        kit.drawCoinCapsule(canvas, coinRect.rf(), state.coins)
        val pause = l.referenceRect(925f, 150f, 95f, 85f)
        kit.drawButton(canvas, pause.rf(), "Ⅱ", green = false)
        hits += pause to GameplayHudAction.Pause

        drawHeightRail(canvas, l, state.placements)

        if (state.perfectIntensity > 0f) {
            val intensity = state.perfectIntensity.coerceIn(0f, 1f)
            kit.text.setShadowLayer(24f*s*intensity, 0f, 0f, 0xAAFFD54A.toInt())
            kit.drawTitle(canvas, "PERFECT!", canvas.width*.5f, 560f*s, (70f + 18f*intensity)*s, 0xFFFFDF52.toInt())
            kit.text.clearShadowLayer()
        }
        if (state.combo >= 2) {
            kit.text.setShadowLayer(18f*s, 0f, 0f, 0xAA36C7FF.toInt())
            kit.drawTitle(canvas, "x${state.combo} KOMBO", canvas.width*.5f, 640f*s, (42f + state.combo.coerceAtMost(8)*2f)*s, 0xFFFFFFFF.toInt())
            kit.text.clearShadowLayer()
        }

        var powerIndex = 0
        if (PowerUpType.SLOW_TIME in state.equipped) {
            val rect = l.referenceRect(800f, 270f + powerIndex*105f, 220f, 82f)
            val label = if (state.runPowerUps.slowTimeRemaining > 0f) "YAVAŞ ${state.runPowerUps.slowTimeRemaining.toInt()+1}s" else "YAVAŞ ZAMAN"
            kit.drawButton(canvas, rect.rf(), label, green = state.runPowerUps.slowTimeRemaining <= 0f)
            hits += rect to GameplayHudAction.ActivateSlowTime
            powerIndex++
        }
        if (PowerUpType.WIDE_PERFECT in state.equipped) {
            val rect = l.referenceRect(800f, 270f + powerIndex*105f, 220f, 82f)
            val label = if (state.runPowerUps.widePerfectRemaining > 0f) "PERFECT ${state.runPowerUps.widePerfectRemaining.toInt()+1}s" else "GENİŞ PERFECT"
            kit.drawButton(canvas, rect.rf(), label, green = state.runPowerUps.widePerfectRemaining <= 0f)
            hits += rect to GameplayHudAction.ActivateWidePerfect
        }
    }

    fun actionAt(x: Float, y: Float): GameplayHudAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: GameplayHudAction.None

    private fun drawHeightRail(canvas: Canvas, l: ScreenLayout, placements: Int) {
        val s = l.scale
        val rail = l.referenceRect(45f, 420f, 85f, 850f)
        val track = RectF(rail.centerX - 9f*s, rail.top + 55f*s, rail.centerX + 9f*s, rail.bottom - 55f*s)
        kit.paint.color = 0x99315791.toInt()
        canvas.drawRoundRect(track, 9f*s, 9f*s, kit.paint)
        val fraction = (placements.coerceAtLeast(0) / 50f).coerceIn(0f, 1f)
        val fill = RectF(track.left, track.bottom - track.height()*fraction, track.right, track.bottom)
        kit.paint.color = 0xFFFFD54A.toInt()
        canvas.drawRoundRect(fill, 9f*s, 9f*s, kit.paint)
        val milestones = listOf(10, 25, 50)
        milestones.forEach { milestone ->
            val f = milestone / 50f
            val cy = track.bottom - track.height()*f
            kit.paint.color = if (placements >= milestone) 0xFFFFD54A.toInt() else 0xFF8DA1BE.toInt()
            canvas.drawCircle(rail.centerX, cy, 27f*s, kit.paint)
            kit.drawTitle(canvas, "★", rail.centerX, cy + 10f*s, 22f*s, 0xFF17345F.toInt())
        }
        kit.drawTitle(canvas, placements.coerceAtLeast(0).toString(), rail.centerX, rail.bottom + 28f*s, 22f*s, Color.WHITE)
    }

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
