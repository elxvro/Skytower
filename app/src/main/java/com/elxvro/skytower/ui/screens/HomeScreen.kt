package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.elxvro.skytower.game.LevelRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.AppScreen
import com.elxvro.skytower.ui.ReferenceDesignTokens
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.UiRect

class HomeScreen(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())

        val level = LevelRules.levelForXp(progress.totalXp)
        val coinRect = l.referenceRect(32f, 42f, 285f, 82f)
        val levelRect = l.referenceRect(760f, 42f, 288f, 82f)
        kit.drawCoinCapsule(canvas, coinRect.rf(), progress.coins)
        kit.drawLevelCapsule(canvas, levelRect.rf(), level.level, level.xpIntoLevel.toFloat() / level.xpRequired.toFloat())

        kit.drawLogo(canvas, canvas.width.toFloat(), 205f * s)

        val daily = l.referenceRect(45f, 330f, 235f, 170f)
        val best = l.referenceRect(800f, 330f, 235f, 170f)
        kit.drawBlueCard(canvas, daily.rf(), 0xFF1773C7.toInt())
        kit.drawBlueCard(canvas, best.rf(), 0xFF1773C7.toInt())
        kit.drawGift(canvas, RectF(daily.left + 70f*s, daily.top + 15f*s, daily.right - 70f*s, daily.top + 95f*s))
        kit.drawTitle(canvas, "GÜNLÜK", daily.centerX, daily.bottom - 48f*s, 27f*s, Color.WHITE)
        kit.drawTitle(canvas, "GÖREV", daily.centerX, daily.bottom - 16f*s, 27f*s, Color.WHITE)
        kit.drawTrophy(canvas, best.centerX, best.top + 65f*s, 62f*s)
        kit.drawTitle(canvas, "EN İYİ SKOR", best.centerX, best.bottom - 47f*s, 24f*s, Color.WHITE)
        kit.drawTitle(canvas, progress.bestScore.toString(), best.centerX, best.bottom - 12f*s, 30f*s, ReferenceDesignTokens.GOLD)
        hits += daily to ScreenAction.Open(AppScreen.DAILY_MISSIONS)

        drawTowerPreview(canvas, l)

        val play = l.referenceRect(205f, 1035f, 670f, 155f)
        kit.drawButton(canvas, play.rf(), "OYNA", green = true)
        kit.drawPlay(canvas, play.left + 100f*s, play.centerY, 58f*s)
        hits += play to ScreenAction.Open(AppScreen.GAMEPLAY)

        val tiles = listOf(
            MenuTile("TEMALAR", AppScreen.THEME_SHOP, ReferenceDesignTokens.PURPLE, "blocks"),
            MenuTile("GÖREVLER", AppScreen.DAILY_MISSIONS, ReferenceDesignTokens.ORANGE, "tasks"),
            MenuTile("SEVİYE", AppScreen.LEVELS, ReferenceDesignTokens.BLUE, "level"),
            MenuTile("SKİNLER", AppScreen.BLOCK_SKINS, ReferenceDesignTokens.PINK, "shirt"),
            MenuTile("GÜÇLENDİRME", AppScreen.POWER_UPS, ReferenceDesignTokens.CYAN, "bolt"),
            MenuTile("AYARLAR", AppScreen.SETTINGS, 0xFF7682A6.toInt(), "gear"),
        )

        tiles.forEachIndexed { index, tile ->
            val col = index % 3
            val row = index / 3
            val x = 58f + col * 338f
            val y = 1230f + row * 205f
            val rect = l.referenceRect(x, y, 300f, 170f)
            kit.drawBlueCard(canvas, rect.rf(), tile.color)
            drawMenuIcon(canvas, rect, tile.icon, s)
            kit.drawOutlinedTitle(canvas, tile.label, rect.centerX, rect.bottom - 20f*s, 25f*s, Color.WHITE)
            hits += rect to ScreenAction.Open(tile.screen)
        }

        kit.drawTitle(canvas, "v0.5 • GELİŞTİRME", canvas.width*.5f, 1685f*s, 22f*s, 0xDDFFFFFF.toInt())
    }

    fun actionAt(x: Float, y: Float): ScreenAction =
        hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawTowerPreview(canvas: Canvas, l: ScreenLayout) {
        val s = l.scale
        val island = l.referenceRect(270f, 560f, 540f, 390f)
        kit.paint.color = 0xFF6D748A.toInt()
        canvas.drawOval(RectF(island.left, island.bottom - 95f*s, island.right, island.bottom), kit.paint)
        kit.paint.color = 0xFF56D367.toInt()
        canvas.drawRoundRect(RectF(island.left - 10f*s, island.bottom - 108f*s, island.right + 10f*s, island.bottom - 65f*s), 25f*s, 25f*s, kit.paint)
        val colors = intArrayOf(
            0xFF2D9EF2.toInt(), 0xFF8B532D.toInt(), 0xFF58C936.toInt(),
            0xFFFFC62D.toInt(), 0xFFF24C70.toInt(), 0xFFF1EEE9.toInt(),
        )
        val baseLeft = island.centerX - 150f*s
        val baseRight = island.centerX + 150f*s
        val blockH = 62f*s
        for (i in 0 until 6) {
            val bottom = island.bottom - 100f*s - i*blockH
            val r = RectF(baseLeft + i*3f*s, bottom - blockH + 3f*s, baseRight - i*3f*s, bottom)
            kit.paint.color = colors[i]
            canvas.drawRoundRect(r, 12f*s, 12f*s, kit.paint)
            kit.paint.style = android.graphics.Paint.Style.STROKE
            kit.paint.strokeWidth = 3f*s
            kit.paint.color = 0x55294A75
            canvas.drawRoundRect(r, 12f*s, 12f*s, kit.paint)
            kit.paint.style = android.graphics.Paint.Style.FILL
        }
    }

    private fun drawMenuIcon(canvas: Canvas, rect: UiRect, icon: String, s: Float) {
        val cx = rect.centerX
        val cy = rect.top + 66f*s
        when (icon) {
            "gear" -> kit.drawGear(canvas, cx, cy, 32f*s)
            "bolt" -> kit.drawBolt(canvas, cx, cy, 54f*s)
            "level" -> kit.drawTrophy(canvas, cx, cy, 50f*s)
            "tasks" -> {
                kit.paint.color = Color.WHITE
                canvas.drawRoundRect(RectF(cx-34f*s, cy-42f*s, cx+34f*s, cy+42f*s), 9f*s, 9f*s, kit.paint)
                kit.drawCheck(canvas, cx, cy+3f*s, 36f*s, ReferenceDesignTokens.TEXT)
            }
            "shirt" -> {
                val p = android.graphics.Path().apply {
                    moveTo(cx-48f*s, cy-30f*s); lineTo(cx-22f*s, cy-45f*s); lineTo(cx-10f*s, cy-25f*s)
                    lineTo(cx+10f*s, cy-25f*s); lineTo(cx+22f*s, cy-45f*s); lineTo(cx+48f*s, cy-30f*s)
                    lineTo(cx+34f*s, cy-5f*s); lineTo(cx+22f*s, cy-12f*s); lineTo(cx+22f*s, cy+42f*s)
                    lineTo(cx-22f*s, cy+42f*s); lineTo(cx-22f*s, cy-12f*s); lineTo(cx-34f*s, cy-5f*s); close()
                }
                kit.paint.color = Color.WHITE; canvas.drawPath(p, kit.paint)
            }
            else -> {
                val colors = intArrayOf(0xFF22B8FF.toInt(), 0xFFFFD136.toInt(), 0xFFF2498D.toInt())
                for (i in 0..2) {
                    kit.paint.color = colors[i]
                    val dx = (i-1)*30f*s
                    canvas.drawRoundRect(RectF(cx+dx-20f*s, cy+(i%2)*16f*s-20f*s, cx+dx+20f*s, cy+(i%2)*16f*s+20f*s), 7f*s, 7f*s, kit.paint)
                }
            }
        }
    }

    private data class MenuTile(val label: String, val screen: AppScreen, val color: Int, val icon: String)
    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
