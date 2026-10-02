package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.elxvro.skytower.game.LevelRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.ItemActionState
import com.elxvro.skytower.ui.ReferenceDesignTokens
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
        val levelState = LevelRules.levelForXp(progress.totalXp)

        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        val back = l.referenceRect(32f, 42f, 95f, 86f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(185f, 44f, 275f, 82f).rf(), progress.coins)
        kit.drawLevelCapsule(canvas, l.referenceRect(770f, 44f, 275f, 82f).rf(), levelState.level, levelState.xpIntoLevel.toFloat()/levelState.xpRequired.toFloat())
        kit.drawRibbon(canvas, l.referenceRect(205f, 175f, 670f, 120f).rf(), "SEVİYE")

        drawLevelShield(canvas, l, levelState.level)

        val xpPanel = l.referenceRect(65f, 565f, 950f, 185f)
        kit.drawPanel(canvas, xpPanel.rf())
        kit.text.textAlign = Paint.Align.LEFT
        kit.drawOutlinedTitle(canvas, levelState.xpIntoLevel.toString(), xpPanel.left+42f*s, xpPanel.top+58f*s, 52f*s, ReferenceDesignTokens.GOLD)
        kit.drawTitle(canvas, "/ ${levelState.xpRequired} XP", xpPanel.left+210f*s, xpPanel.top+55f*s, 31f*s, ReferenceDesignTokens.TEXT)
        kit.text.textAlign = Paint.Align.CENTER
        val bar = RectF(xpPanel.left+35f*s, xpPanel.top+88f*s, xpPanel.right-35f*s, xpPanel.top+132f*s)
        kit.drawProgress(canvas, bar, levelState.xpIntoLevel.toFloat()/levelState.xpRequired.toFloat(), ReferenceDesignTokens.GOLD)
        val remaining = (levelState.xpRequired-levelState.xpIntoLevel).coerceAtLeast(0)
        kit.drawTitle(canvas, "Sonraki seviyeye $remaining XP kaldı!", xpPanel.centerX, xpPanel.bottom-15f*s, 23f*s, ReferenceDesignTokens.TEXT)

        val firstRewardLevel = maxOf(1, levelState.level-2)
        repeat(6) { slot ->
            val rewardLevel = firstRewardLevel+slot
            val y = 790f+slot*145f
            val row = l.referenceRect(65f, y, 950f, 125f)
            val relation = when {
                rewardLevel < levelState.level -> -1
                rewardLevel == levelState.level -> 0
                else -> 1
            }
            val claimed = rewardLevel in progress.claimedLevelRewards
            when {
                relation < 0 || claimed -> kit.drawLightCard(canvas, row.rf(), highlighted = false)
                relation == 0 -> kit.drawLightCard(canvas, row.rf(), highlighted = true)
                else -> kit.drawLightCard(canvas, row.rf(), highlighted = false)
            }

            val badgeColor = when {
                claimed -> ReferenceDesignTokens.ACTION_GREEN
                relation == 0 -> ReferenceDesignTokens.GOLD
                else -> 0xFF8196BA.toInt()
            }
            kit.paint.color = badgeColor
            canvas.drawCircle(row.left+62f*s, row.centerY, 39f*s, kit.paint)
            if (claimed) kit.drawCheck(canvas, row.left+62f*s, row.centerY, 38f*s, Color.WHITE)
            else kit.drawTitle(canvas, rewardLevel.toString(), row.left+62f*s, row.centerY+13f*s, 31f*s, if (relation==0) ReferenceDesignTokens.NAVY_DARK else Color.WHITE)

            kit.text.textAlign = Paint.Align.LEFT
            kit.drawTitle(canvas, "Sv. $rewardLevel", row.left+128f*s, row.top+48f*s, 29f*s, ReferenceDesignTokens.TEXT)
            val reward = LevelRules.rewardForLevel(rewardLevel)
            val rewardText = if (reward.coins>0) "${reward.coins} ALTIN" else reward.powerUp?.let { "+${reward.quantity} ${powerName(it.name)}" } ?: "ÖZEL ÖDÜL"
            kit.drawTitle(canvas, rewardText, row.left+360f*s, row.centerY+10f*s, 27f*s, ReferenceDesignTokens.TEXT)
            kit.text.textAlign = Paint.Align.CENTER

            val action = UiRect(row.right-230f*s, row.top+25f*s, row.right-28f*s, row.bottom-25f*s)
            when (ScreenInteractionState.level(levelState.level, rewardLevel, claimed)) {
                ItemActionState.CLAIM -> {
                    kit.drawButton(canvas, action.rf(), "ŞİMDİ AL", green = true)
                    hits += action to ScreenAction.ClaimLevel(rewardLevel)
                }
                ItemActionState.CLAIMED -> kit.drawButton(canvas, action.rf(), "ALINDI", enabled = false, green = true)
                else -> kit.drawButton(canvas, action.rf(), "KİLİTLİ", enabled = false, green = false)
            }
        }
    }

    fun actionAt(x: Float, y: Float): ScreenAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawLevelShield(canvas: Canvas, l: ScreenLayout, level: Int) {
        val s = l.scale
        val cx = l.viewportWidth*.5f
        val top = 315f*s
        val shield = Path().apply {
            moveTo(cx-130f*s, top+18f*s)
            lineTo(cx+130f*s, top+18f*s)
            lineTo(cx+112f*s, top+160f*s)
            lineTo(cx, top+235f*s)
            lineTo(cx-112f*s, top+160f*s)
            close()
        }
        kit.paint.color = ReferenceDesignTokens.GOLD
        canvas.drawPath(shield, kit.paint)
        val inner = Path().apply {
            moveTo(cx-106f*s, top+37f*s)
            lineTo(cx+106f*s, top+37f*s)
            lineTo(cx+90f*s, top+145f*s)
            lineTo(cx, top+205f*s)
            lineTo(cx-90f*s, top+145f*s)
            close()
        }
        kit.paint.color = ReferenceDesignTokens.BLUE
        canvas.drawPath(inner, kit.paint)
        kit.drawCrown(canvas, cx, top+38f*s, 39f*s)
        kit.drawTitle(canvas, "Sv.", cx, top+108f*s, 40f*s, Color.WHITE)
        kit.drawOutlinedTitle(canvas, level.toString(), cx, top+177f*s, 74f*s, ReferenceDesignTokens.GOLD)
    }

    private fun powerName(raw: String): String = when (raw) {
        "SECOND_CHANCE" -> "İKİNCİ ŞANS"
        "SLOW_TIME" -> "YAVAŞ ZAMAN"
        "WIDE_PERFECT" -> "GENİŞ PERFECT"
        "COIN_MULTIPLIER" -> "COIN ÇARPANI"
        else -> raw.replace('_',' ')
    }

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
