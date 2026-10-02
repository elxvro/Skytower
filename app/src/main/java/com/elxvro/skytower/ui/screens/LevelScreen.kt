package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.RectF
import com.elxvro.skytower.game.LevelRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.ItemActionState
import com.elxvro.skytower.ui.ScreenInteractionState
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.UiRect

class LevelScreen(private val kit: SkyVisualKit) {
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
        kit.drawRibbon(canvas, l.referenceRect(250f, 285f, 580f, 105f).rf(), "SEVİYE")

        val panel = l.referenceRect(70f, 420f, 940f, 1290f)
        kit.drawPanel(canvas, panel.rf())
        val levelState = LevelRules.levelForXp(progress.totalXp)
        val hero = l.referenceRect(115f, 475f, 850f, 245f)
        kit.drawBlueCard(canvas, hero.rf(), 0xFF315FC1.toInt())
        kit.drawTitle(canvas, "SEVİYE", hero.centerX(), hero.top + 55f * s, 31f * s, 0xFFE5F4FF.toInt())
        kit.drawTitle(canvas, levelState.level.toString(), hero.centerX(), hero.top + 140f * s, 80f * s, 0xFFFFD54A.toInt())
        val xpRect = RectF(hero.left + 95f * s, hero.bottom - 58f * s, hero.right - 95f * s, hero.bottom - 25f * s)
        kit.drawProgress(canvas, xpRect, levelState.xpIntoLevel.toFloat() / levelState.xpRequired.toFloat(), 0xFFFFCB3D.toInt())
        kit.drawTitle(canvas, "${levelState.xpIntoLevel} / ${levelState.xpRequired} XP", xpRect.centerX(), xpRect.top - 12f * s, 23f * s, 0xFFFFFFFF.toInt())

        kit.drawTitle(canvas, "SEVİYE ÖDÜLLERİ", canvas.width * .5f, 780f * s, 34f * s)
        val firstRewardLevel = maxOf(2, levelState.level - 1)
        for (slot in 0 until 4) {
            val rewardLevel = firstRewardLevel + slot
            val y = 820f + slot * 185f
            val card = l.referenceRect(115f, y, 850f, 155f)
            kit.drawBlueCard(canvas, card.rf(), if (rewardLevel == levelState.level) 0xFF684FD1.toInt() else 0xFF2D70C7.toInt())
            kit.drawTitle(canvas, "SEVİYE $rewardLevel", card.left + 150f * s, card.centerY() + 8f * s, 28f * s, 0xFFFFFFFF.toInt())
            val reward = LevelRules.rewardForLevel(rewardLevel)
            val rewardText = if (reward.coins > 0) "+${reward.coins} COIN" else "+${reward.quantity} ${reward.powerUp?.name?.replace('_', ' ') ?: "ÖDÜL"}"
            kit.drawTitle(canvas, rewardText, card.left + 405f * s, card.centerY() + 8f * s, 25f * s, 0xFFFFD54A.toInt())
            val button = UiRect(card.right - 215f * s, card.top + 40f * s, card.right - 30f * s, card.bottom - 35f * s)
            when (ScreenInteractionState.level(levelState.level, rewardLevel, rewardLevel in progress.claimedLevelRewards)) {
                ItemActionState.CLAIM -> kit.drawButton(canvas, button.rf(), "TOPLA", green = true)
                ItemActionState.CLAIMED -> kit.drawButton(canvas, button.rf(), "ALINDI", enabled = false, green = true)
                else -> kit.drawButton(canvas, button.rf(), "KİLİTLİ", enabled = false, green = false)
            }
            hits += button to ScreenAction.ClaimLevel(rewardLevel)
        }

        val stats = l.referenceRect(115f, 1580f, 850f, 105f)
        kit.drawBlueCard(canvas, stats.rf(), 0xFF214F99.toInt())
        kit.drawTitle(canvas, "OYUN ${progress.totalRuns}", stats.left + 145f * s, stats.centerY() + 8f * s, 24f * s, 0xFFFFFFFF.toInt())
        kit.drawTitle(canvas, "REKOR ${progress.bestScore}", stats.centerX(), stats.centerY() + 8f * s, 24f * s, 0xFFFFFFFF.toInt())
        kit.drawTitle(canvas, "PERFECT ${progress.totalPerfects}", stats.right - 150f * s, stats.centerY() + 8f * s, 24f * s, 0xFFFFFFFF.toInt())
    }

    fun actionAt(x: Float, y: Float): ScreenAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None
    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
