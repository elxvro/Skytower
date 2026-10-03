package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.elxvro.skytower.game.DailyMission
import com.elxvro.skytower.game.DailyMissionRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.ItemActionState
import com.elxvro.skytower.ui.ReferenceDesignTokens
import com.elxvro.skytower.ui.ScreenInteractionState
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.UiRect

class DailyMissionsScreen(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        val back = l.referenceRect(32f, 42f, 95f, 86f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(770f, 44f, 275f, 82f).rf(), progress.coins)
        kit.drawRibbon(canvas, l.referenceRect(155f, 170f, 770f, 125f).rf(), "GÜNLÜK GÖREVLER")

        val rewardBanner = l.referenceRect(48f, 335f, 984f, 195f)
        kit.drawBlueCard(canvas, rewardBanner.rf(), ReferenceDesignTokens.PURPLE)
        kit.drawGift(canvas, RectF(rewardBanner.left + 38f*s, rewardBanner.top + 25f*s, rewardBanner.left + 215f*s, rewardBanner.bottom - 25f*s))
        kit.text.textAlign = android.graphics.Paint.Align.LEFT
        kit.drawOutlinedTitle(canvas, "GÜNLÜK ÖDÜL", rewardBanner.left + 245f*s, rewardBanner.top + 72f*s, 37f*s, ReferenceDesignTokens.GOLD)
        kit.drawTitle(canvas, "Bugünün görevlerini tamamla", rewardBanner.left + 245f*s, rewardBanner.top + 115f*s, 23f*s, Color.WHITE)
        kit.drawTitle(canvas, "ve özel ödülü kazan!", rewardBanner.left + 245f*s, rewardBanner.top + 150f*s, 23f*s, Color.WHITE)
        kit.text.textAlign = android.graphics.Paint.Align.CENTER
        kit.drawCoin(canvas, rewardBanner.right - 165f*s, rewardBanner.centerY - 20f*s, 36f*s)
        kit.drawTitle(canvas, "+100", rewardBanner.right - 165f*s, rewardBanner.centerY + 53f*s, 29f*s, Color.WHITE)

        val missions = listOf(
            MissionUi(DailyMission.BLOCKS, "10 blok yerleştir", progress.dailyState.placements, 10, 20, MissionIcon.BLOCKS),
            MissionUi(DailyMission.PERFECTS, "3 PERFECT yap", progress.dailyState.perfects, 3, 25, MissionIcon.PERFECT),
            MissionUi(DailyMission.SCORE, "50 skor elde et", progress.dailyState.bestScore, 50, 40, MissionIcon.TROPHY),
        )
        missions.forEachIndexed { index, item ->
            val y = 565f + index * 245f
            val card = l.referenceRect(48f, y, 984f, 205f)
            kit.drawLightCard(canvas, card.rf())
            val iconRect = l.referenceRect(78f, y + 30f, 155f, 145f)
            kit.drawBlueCard(canvas, iconRect.rf(), 0xFFCFF1FF.toInt())
            drawMissionIcon(canvas, item.icon, iconRect, s)

            kit.text.textAlign = android.graphics.Paint.Align.LEFT
            kit.drawTitle(canvas, item.title, card.left + 210f*s, card.top + 63f*s, 31f*s, ReferenceDesignTokens.TEXT)
            kit.text.textAlign = android.graphics.Paint.Align.CENTER
            val bar = RectF(card.left + 210f*s, card.top + 105f*s, card.left + 595f*s, card.top + 148f*s)
            kit.drawProgress(canvas, bar, item.value.toFloat() / item.target.toFloat())
            kit.drawTitle(canvas, "${item.value.coerceAtMost(item.target)} / ${item.target}", bar.centerX(), bar.centerY()+9f*s, 21f*s, Color.WHITE)

            val reward = l.referenceRect(680f, y + 48f, 150f, 80f)
            kit.drawCoinCapsule(canvas, reward.rf(), item.reward)
            val button = l.referenceRect(860f, y + 45f, 120f, 110f)
            val state = ScreenInteractionState.daily(progress.dailyState, item.mission)
            when (state) {
                ItemActionState.CLAIM -> kit.drawButton(canvas, button.rf(), "TOPLA", green = true)
                ItemActionState.CLAIMED -> {
                    kit.drawButton(canvas, button.rf(), "✓", enabled = false, green = true)
                }
                else -> kit.drawButton(canvas, button.rf(), ">", green = false)
            }
            hits += button to ScreenAction.ClaimDaily(item.mission)
        }

        val gift = l.referenceRect(48f, 1325f, 380f, 355f)
        kit.drawLightCard(canvas, gift.rf())
        kit.drawGift(canvas, RectF(gift.left + 75f*s, gift.top + 30f*s, gift.right - 75f*s, gift.top + 195f*s))
        kit.drawOutlinedTitle(canvas, "GÜNLÜK HEDİYE", gift.centerX, gift.top + 235f*s, 29f*s, Color.WHITE)
        val allButton = l.referenceRect(75f, 1580f, 325f, 82f)
        val allReady = progress.dailyState.claimedMask and DailyMissionRules.ALL_MISSIONS_MASK == DailyMissionRules.ALL_MISSIONS_MASK
        kit.drawButton(canvas, allButton.rf(), if (progress.dailyState.allCompleteClaimed) "ALINDI" else "TOPLA", enabled = allReady && !progress.dailyState.allCompleteClaimed, green = true)
        hits += allButton to ScreenAction.ClaimDailyAll

        val streak = l.referenceRect(455f, 1325f, 577f, 355f)
        kit.drawBlueCard(canvas, streak.rf(), 0xFF4A35A2.toInt())
        kit.drawOutlinedTitle(canvas, "7 GÜNLÜK SERİ ÖDÜLLERİ", streak.centerX, streak.top + 60f*s, 27f*s, Color.WHITE)
        for (day in 1..7) {
            val cx = streak.left + (48f + (day-1)*79f)*s
            val cy = streak.top + 145f*s
            val active = day <= progress.dailyState.streakCount
            kit.paint.color = if (active) ReferenceDesignTokens.GOLD else 0xFF7180B0.toInt()
            canvas.drawCircle(cx, cy, 27f*s, kit.paint)
            if (active) kit.drawCheck(canvas, cx, cy, 25f*s, ReferenceDesignTokens.NAVY_DARK)
            else kit.drawTitle(canvas, day.toString(), cx, cy+8f*s, 18f*s, Color.WHITE)
            kit.drawTitle(canvas, "${day*100}", cx, cy+58f*s, 17f*s, Color.WHITE)
        }
        val lineY = streak.bottom - 58f*s
        kit.paint.color = 0xFF8C92C9.toInt()
        canvas.drawRoundRect(RectF(streak.left+45f*s, lineY-5f*s, streak.right-45f*s, lineY+5f*s), 5f*s, 5f*s, kit.paint)
        val progressFraction = (progress.dailyState.streakCount / 7f).coerceIn(0f,1f)
        kit.paint.color = ReferenceDesignTokens.ACTION_GREEN
        canvas.drawRoundRect(RectF(streak.left+45f*s, lineY-5f*s, streak.left+45f*s+(streak.width-90f*s)*progressFraction, lineY+5f*s), 5f*s, 5f*s, kit.paint)
    }

    fun actionAt(x: Float, y: Float): ScreenAction =
        hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawMissionIcon(canvas: Canvas, icon: MissionIcon, rect: UiRect, s: Float) {
        when (icon) {
            MissionIcon.BLOCKS -> {
                val colors = intArrayOf(0xFFF24D64.toInt(), 0xFF24A7FF.toInt(), 0xFFFFC72E.toInt())
                val centers = listOf(rect.centerX to rect.centerY-30f*s, rect.centerX-35f*s to rect.centerY+25f*s, rect.centerX+35f*s to rect.centerY+25f*s)
                centers.forEachIndexed { i, p ->
                    kit.paint.color = colors[i]
                    canvas.drawRoundRect(RectF(p.first-28f*s, p.second-28f*s, p.first+28f*s, p.second+28f*s), 8f*s, 8f*s, kit.paint)
                }
            }
            MissionIcon.PERFECT -> {
                kit.drawOutlinedTitle(canvas, "★", rect.centerX, rect.centerY+30f*s, 90f*s, ReferenceDesignTokens.GOLD)
                kit.drawOutlinedTitle(canvas, "PERFECT", rect.centerX, rect.bottom-12f*s, 19f*s, ReferenceDesignTokens.ORANGE)
            }
            MissionIcon.TROPHY -> kit.drawTrophy(canvas, rect.centerX, rect.centerY, 72f*s)
        }
    }

    private enum class MissionIcon { BLOCKS, PERFECT, TROPHY }
    private data class MissionUi(val mission: DailyMission, val title: String, val value: Int, val target: Int, val reward: Int, val icon: MissionIcon)
    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
