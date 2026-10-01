package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.RectF
import com.elxvro.skytower.game.DailyMission
import com.elxvro.skytower.game.DailyMissionRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.ItemActionState
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
        kit.drawLogo(canvas, canvas.width.toFloat(), 145f * s)
        val back = l.referenceRect(45f, 60f, 110f, 90f)
        kit.drawBack(canvas, back.rf()); hits += back to ScreenAction.Back
        kit.drawCoinCapsule(canvas, l.referenceRect(815f, 65f, 220f, 82f).rf(), progress.coins)
        kit.drawRibbon(canvas, l.referenceRect(250f, 285f, 580f, 105f).rf(), "GÜNLÜK GÖREVLER")

        val panel = l.referenceRect(70f, 420f, 940f, 1290f)
        kit.drawPanel(canvas, panel.rf())
        kit.drawTitle(canvas, "BUGÜNÜN GÖREVLERİ", canvas.width * .5f, 490f * s, 38f * s)

        val missions = listOf(
            MissionUi(DailyMission.BLOCKS, "10 BLOK YERLEŞTİR", "Kuleni yükselt", progress.dailyState.placements, 10, 20),
            MissionUi(DailyMission.PERFECTS, "3 PERFECT YAP", "Kusursuz hizala", progress.dailyState.perfects, 3, 25),
            MissionUi(DailyMission.SCORE, "50 SKORA ULAŞ", "Bu koşuda hedefe ulaş", progress.dailyState.bestScore, 50, 40),
        )
        missions.forEachIndexed { index, item ->
            val y = 535f + index * 260f
            val card = l.referenceRect(115f, y, 850f, 225f)
            kit.drawBlueCard(canvas, card.rf())
            kit.drawTitle(canvas, item.title, card.left + 250f * s, card.top + 60f * s, 32f * s, 0xFFFFFFFF.toInt())
            kit.drawTitle(canvas, item.subtitle, card.left + 250f * s, card.top + 102f * s, 24f * s, 0xFFD8EDFF.toInt())
            val progressRect = RectF(card.left + 55f * s, card.top + 130f * s, card.left + 545f * s, card.top + 168f * s)
            kit.drawProgress(canvas, progressRect, item.value.toFloat() / item.target.toFloat())
            kit.drawTitle(canvas, "${item.value.coerceAtMost(item.target)}/${item.target}", progressRect.centerX(), progressRect.centerY() + 10f * s, 22f * s, 0xFF123B75.toInt())
            val button = UiRect(card.right - 245f * s, card.top + 120f * s, card.right - 35f * s, card.bottom - 30f * s)
            when (ScreenInteractionState.daily(progress.dailyState, item.mission)) {
                ItemActionState.CLAIM -> kit.drawButton(canvas, button.rf(), "TOPLA\n+${item.reward}", green = true)
                ItemActionState.CLAIMED -> kit.drawButton(canvas, button.rf(), "ALINDI", enabled = false, green = true)
                else -> kit.drawButton(canvas, button.rf(), "+${item.reward}", enabled = false, green = false)
            }
            hits += button to ScreenAction.ClaimDaily(item.mission)
        }

        val streakCard = l.referenceRect(115f, 1325f, 850f, 150f)
        kit.drawBlueCard(canvas, streakCard.rf(), 0xFF315FC1.toInt())
        kit.drawTitle(canvas, "5 GÜNLÜK SERİ", streakCard.left + 175f * s, streakCard.top + 46f * s, 29f * s, 0xFFFFFFFF.toInt())
        for (day in 1..5) {
            val cx = streakCard.left + (110f + (day - 1) * 145f) * s
            val active = day <= progress.dailyState.streakCount
            kit.paint.color = if (active) 0xFFFFCF3E.toInt() else 0xFF8296BD.toInt()
            canvas.drawCircle(cx, streakCard.top + 98f * s, 33f * s, kit.paint)
            kit.drawTitle(canvas, day.toString(), cx, streakCard.top + 108f * s, 24f * s, 0xFF17345F.toInt())
        }

        val all = l.referenceRect(115f, 1505f, 850f, 145f)
        kit.drawBlueCard(canvas, all.rf(), 0xFF6B4FD0.toInt())
        kit.drawTitle(canvas, "TÜM GÖREVLERİ TAMAMLA", all.left + 320f * s, all.top + 58f * s, 28f * s, 0xFFFFFFFF.toInt())
        kit.drawTitle(canvas, "+100 COIN", all.left + 320f * s, all.top + 105f * s, 27f * s, 0xFFFFD54A.toInt())
        val allButton = UiRect(all.right - 230f * s, all.top + 35f * s, all.right - 35f * s, all.bottom - 30f * s)
        val allReady = progress.dailyState.claimedMask and DailyMissionRules.ALL_MISSIONS_MASK == DailyMissionRules.ALL_MISSIONS_MASK
        kit.drawButton(canvas, allButton.rf(), if (progress.dailyState.allCompleteClaimed) "ALINDI" else "TOPLA", enabled = allReady && !progress.dailyState.allCompleteClaimed, green = true)
        hits += allButton to ScreenAction.ClaimDailyAll
    }

    fun actionAt(x: Float, y: Float): ScreenAction = hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private data class MissionUi(val mission: DailyMission, val title: String, val subtitle: String, val value: Int, val target: Int, val reward: Int)
    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
